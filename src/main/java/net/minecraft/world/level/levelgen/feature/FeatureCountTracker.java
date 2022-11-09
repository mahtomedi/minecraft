package net.minecraft.world.level.levelgen.feature;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.apache.commons.lang3.mutable.MutableInt;
import org.slf4j.Logger;

public class FeatureCountTracker {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final LoadingCache<ServerLevel, FeatureCountTracker.LevelData> data = CacheBuilder.newBuilder()
        .weakKeys()
        .expireAfterAccess(5L, TimeUnit.MINUTES)
        .build(new CacheLoader<ServerLevel, FeatureCountTracker.LevelData>() {
            public FeatureCountTracker.LevelData load(ServerLevel param0) {
                return new FeatureCountTracker.LevelData(Object2IntMaps.synchronize(new Object2IntOpenHashMap<>()), new MutableInt(0));
            }
        });

    public static void chunkDecorated(ServerLevel param0) {
        try {
            data.get(param0).chunksWithFeatures().increment();
        } catch (Exception var2) {
            LOGGER.error("Failed to increment chunk count", (Throwable)var2);
        }

    }

    public static void featurePlaced(ServerLevel param0, ConfiguredFeature<?, ?> param1, Optional<PlacedFeature> param2) {
        try {
            data.get(param0)
                .featureData()
                .computeInt(new FeatureCountTracker.FeatureData(param1, param2), (param0x, param1x) -> param1x == null ? 1 : param1x + 1);
        } catch (Exception var4) {
            LOGGER.error("Failed to increment feature count", (Throwable)var4);
        }

    }

    public static void clearCounts() {
        data.invalidateAll();
        LOGGER.debug("Cleared feature counts");
    }

    public static void logCounts() {
        LOGGER.debug("Logging feature counts:");
        data.asMap()
            .forEach(
                (param0, param1) -> {
                    String var0 = param0.dimension().location().toString();
                    boolean var1 = param0.getServer().isRunning();
                    Registry<PlacedFeature> var2 = param0.registryAccess().registryOrThrow(Registries.PLACED_FEATURE);
                    String var3 = (var1 ? "running" : "dead") + " " + var0;
                    Integer var4 = param1.chunksWithFeatures().getValue();
                    LOGGER.debug(var3 + " total_chunks: " + var4);
                    param1.featureData()
                        .forEach(
                            (param3, param4) -> LOGGER.debug(
                                    var3
                                        + " "
                                        + String.format(Locale.ROOT, "%10d ", param4)
                                        + String.format(Locale.ROOT, "%10f ", (double)param4.intValue() / (double)var4.intValue())
                                        + param3.topFeature().flatMap(var2::getResourceKey).map(ResourceKey::location)
                                        + " "
                                        + param3.feature().feature()
                                        + " "
                                        + param3.feature()
                                )
                        );
                }
            );
    }

    static record FeatureData(ConfiguredFeature<?, ?> feature, Optional<PlacedFeature> topFeature) {
    }

    static record LevelData(Object2IntMap<FeatureCountTracker.FeatureData> featureData, MutableInt chunksWithFeatures) {
    }
}
