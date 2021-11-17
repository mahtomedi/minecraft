package net.minecraft.world.level.levelgen.structure;

import com.google.common.collect.Multimap;
import com.mojang.datafixers.DataFixer;
import it.unimi.dsi.fastutil.longs.Long2BooleanMap;
import it.unimi.dsi.fastutil.longs.Long2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.visitors.CollectFields;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.storage.ChunkScanAccess;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StructureCheck {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final int NO_STRUCTURE = -1;
    private final ChunkScanAccess storageAccess;
    private final RegistryAccess registryAccess;
    private final Registry<Biome> biomes;
    private final StructureManager structureManager;
    private final ResourceKey<Level> dimension;
    private final ChunkGenerator chunkGenerator;
    private final LevelHeightAccessor heightAccessor;
    private final BiomeSource biomeSource;
    private final long seed;
    private final DataFixer fixerUpper;
    private final Long2ObjectMap<Object2IntMap<StructureFeature<?>>> loadedChunks = new Long2ObjectOpenHashMap<>();
    private final Map<StructureFeature<?>, Long2BooleanMap> featureChecks = new HashMap<>();

    public StructureCheck(
        ChunkScanAccess param0,
        RegistryAccess param1,
        StructureManager param2,
        ResourceKey<Level> param3,
        ChunkGenerator param4,
        LevelHeightAccessor param5,
        BiomeSource param6,
        long param7,
        DataFixer param8
    ) {
        this.storageAccess = param0;
        this.registryAccess = param1;
        this.structureManager = param2;
        this.dimension = param3;
        this.chunkGenerator = param4;
        this.heightAccessor = param5;
        this.biomeSource = param6;
        this.seed = param7;
        this.fixerUpper = param8;
        this.biomes = param1.ownedRegistryOrThrow(Registry.BIOME_REGISTRY);
    }

    public <F extends StructureFeature<?>> StructureCheckResult checkStart(ChunkPos param0, F param1, boolean param2) {
        long var0 = param0.toLong();
        Object2IntMap<StructureFeature<?>> var1 = this.loadedChunks.get(var0);
        if (var1 != null) {
            return this.checkStructureInfo(var1, param1, param2);
        } else {
            StructureCheckResult var2 = this.tryLoadFromStorage(param0, param1, param2, var0);
            if (var2 != null) {
                return var2;
            } else {
                boolean var3 = this.featureChecks.computeIfAbsent(param1, param0x -> new Long2BooleanOpenHashMap()).computeIfAbsent(var0, param2x -> {
                    Multimap<ConfiguredStructureFeature<?, ?>, ResourceKey<Biome>> var0x = this.chunkGenerator.getSettings().structures(param1);

                    for(Entry<ConfiguredStructureFeature<?, ?>, Collection<ResourceKey<Biome>>> var1x : var0x.asMap().entrySet()) {
                        if (this.canCreateStructure(param0, var1x.getKey(), var1x.getValue())) {
                            return true;
                        }
                    }

                    return false;
                });
                return !var3 ? StructureCheckResult.START_NOT_PRESENT : StructureCheckResult.CHUNK_LOAD_NEEDED;
            }
        }
    }

    private <FC extends FeatureConfiguration, F extends StructureFeature<FC>> boolean canCreateStructure(
        ChunkPos param0, ConfiguredStructureFeature<FC, F> param1, Collection<ResourceKey<Biome>> param2
    ) {
        Predicate<Biome> var0 = param1x -> this.biomes.getResourceKey(param1x).filter(param2::contains).isPresent();
        return param1.feature
            .canGenerate(
                this.registryAccess, this.chunkGenerator, this.biomeSource, this.structureManager, this.seed, param0, param1.config, this.heightAccessor, var0
            );
    }

    @Nullable
    private StructureCheckResult tryLoadFromStorage(ChunkPos param0, StructureFeature<?> param1, boolean param2, long param3) {
        CollectFields var0 = new CollectFields(
            new CollectFields.WantedField(IntTag.TYPE, "DataVersion"),
            new CollectFields.WantedField("Level", "Structures", CompoundTag.TYPE, "Starts"),
            new CollectFields.WantedField("structures", CompoundTag.TYPE, "starts")
        );

        try {
            this.storageAccess.scanChunk(param0, var0).join();
        } catch (Exception var13) {
            LOGGER.warn("Failed to read chunk {}", param0, var13);
            return StructureCheckResult.CHUNK_LOAD_NEEDED;
        }

        Tag var2 = var0.getResult();
        if (!(var2 instanceof CompoundTag)) {
            return null;
        } else {
            CompoundTag var3 = (CompoundTag)var2;
            int var4 = ChunkStorage.getVersion(var3);
            if (var4 <= 1493) {
                return StructureCheckResult.CHUNK_LOAD_NEEDED;
            } else {
                ChunkStorage.injectDatafixingContext(var3, this.dimension, this.chunkGenerator.getTypeNameForDataFixer());

                CompoundTag var5;
                try {
                    var5 = NbtUtils.update(this.fixerUpper, DataFixTypes.CHUNK, var3, var4);
                } catch (Exception var12) {
                    LOGGER.warn("Failed to partially datafix chunk {}", param0, var12);
                    return StructureCheckResult.CHUNK_LOAD_NEEDED;
                }

                Object2IntMap<StructureFeature<?>> var8 = this.loadStructures(var5);
                if (var8 == null) {
                    return null;
                } else {
                    this.storeFullResults(param3, var8);
                    return this.checkStructureInfo(var8, param1, param2);
                }
            }
        }
    }

    @Nullable
    private Object2IntMap<StructureFeature<?>> loadStructures(CompoundTag param0) {
        if (!param0.contains("structures", 10)) {
            return null;
        } else {
            CompoundTag var0 = param0.getCompound("structures");
            if (!var0.contains("starts", 10)) {
                return null;
            } else {
                CompoundTag var1 = var0.getCompound("starts");
                if (var1.isEmpty()) {
                    return Object2IntMaps.emptyMap();
                } else {
                    Object2IntMap<StructureFeature<?>> var2 = new Object2IntOpenHashMap<>();

                    for(String var3 : var1.getAllKeys()) {
                        String var4 = var3.toLowerCase(Locale.ROOT);
                        StructureFeature<?> var5 = StructureFeature.STRUCTURES_REGISTRY.get(var4);
                        if (var5 != null) {
                            CompoundTag var6 = var1.getCompound(var3);
                            if (!var6.isEmpty()) {
                                String var7 = var6.getString("id");
                                if (!"INVALID".equals(var7)) {
                                    int var8 = var6.getInt("references");
                                    var2.put(var5, var8);
                                }
                            }
                        }
                    }

                    return var2;
                }
            }
        }
    }

    private static Object2IntMap<StructureFeature<?>> deduplicateEmptyMap(Object2IntMap<StructureFeature<?>> param0) {
        return param0.isEmpty() ? Object2IntMaps.emptyMap() : param0;
    }

    private StructureCheckResult checkStructureInfo(Object2IntMap<StructureFeature<?>> param0, StructureFeature<?> param1, boolean param2) {
        int var0 = param0.getOrDefault(param1, -1);
        return var0 == -1 || param2 && var0 != 0 ? StructureCheckResult.START_NOT_PRESENT : StructureCheckResult.START_PRESENT;
    }

    public void onStructureLoad(ChunkPos param0, Map<StructureFeature<?>, StructureStart<?>> param1) {
        long var0 = param0.toLong();
        Object2IntMap<StructureFeature<?>> var1 = new Object2IntOpenHashMap<>();
        param1.forEach((param1x, param2) -> {
            if (param2.isValid()) {
                var1.put(param1x, param2.getReferences());
            }

        });
        this.storeFullResults(var0, var1);
    }

    private void storeFullResults(long param0, Object2IntMap<StructureFeature<?>> param1) {
        this.loadedChunks.put(param0, deduplicateEmptyMap(param1));
        this.featureChecks.values().forEach(param1x -> param1x.remove(param0));
    }

    public void incrementReference(ChunkPos param0, StructureFeature<?> param1) {
        this.loadedChunks
            .compute(
                param0.toLong(),
                (BiFunction<? super Long, ? super Object2IntMap<StructureFeature<?>>, ? extends Object2IntMap<StructureFeature<?>>>)((param1x, param2) -> {
                    if (param2 == null || param2.isEmpty()) {
                        param2 = new Object2IntOpenHashMap<>();
                    }
        
                    param2.computeInt(param1, (param0x, param1xx) -> param1xx == null ? 1 : param1xx + 1);
                    return param2;
                })
            );
    }
}