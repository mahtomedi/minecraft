package net.minecraft.server;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.commands.Commands;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.WorldDataConfiguration;
import org.slf4j.Logger;

public class WorldLoader {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static <D, R> CompletableFuture<R> load(
        WorldLoader.InitConfig param0, WorldLoader.WorldDataSupplier<D> param1, WorldLoader.ResultFactory<D, R> param2, Executor param3, Executor param4
    ) {
        try {
            Pair<WorldDataConfiguration, CloseableResourceManager> var0 = param0.packConfig.createResourceManager();
            CloseableResourceManager var1 = var0.getSecond();
            LayeredRegistryAccess<RegistryLayer> var2 = RegistryLayer.createRegistryAccess();
            LayeredRegistryAccess<RegistryLayer> var3 = loadAndReplaceLayer(var1, var2, RegistryLayer.WORLDGEN, RegistryDataLoader.WORLDGEN_REGISTRIES);
            RegistryAccess.Frozen var4 = var3.getAccessForLoading(RegistryLayer.DIMENSIONS);
            RegistryAccess.Frozen var5 = RegistryDataLoader.load(var1, var4, RegistryDataLoader.DIMENSION_REGISTRIES);
            WorldDataConfiguration var6 = var0.getFirst();
            WorldLoader.DataLoadOutput<D> var7 = param1.get(new WorldLoader.DataLoadContext(var1, var6, var4, var5));
            LayeredRegistryAccess<RegistryLayer> var8 = var3.replaceFrom(RegistryLayer.DIMENSIONS, var7.finalDimensions);
            RegistryAccess.Frozen var9 = var8.getAccessForLoading(RegistryLayer.RELOADABLE);
            return ReloadableServerResources.loadResources(
                    var1, var9, var6.enabledFeatures(), param0.commandSelection(), param0.functionCompilationLevel(), param3, param4
                )
                .whenComplete((param1x, param2x) -> {
                    if (param2x != null) {
                        var1.close();
                    }
    
                })
                .thenApplyAsync(param5 -> {
                    param5.updateRegistryTags(var9);
                    return param2.create(var1, param5, var8, var7.cookie);
                }, param4);
        } catch (Exception var15) {
            return CompletableFuture.failedFuture(var15);
        }
    }

    private static RegistryAccess.Frozen loadLayer(
        ResourceManager param0, LayeredRegistryAccess<RegistryLayer> param1, RegistryLayer param2, List<RegistryDataLoader.RegistryData<?>> param3
    ) {
        RegistryAccess.Frozen var0 = param1.getAccessForLoading(param2);
        return RegistryDataLoader.load(param0, var0, param3);
    }

    private static LayeredRegistryAccess<RegistryLayer> loadAndReplaceLayer(
        ResourceManager param0, LayeredRegistryAccess<RegistryLayer> param1, RegistryLayer param2, List<RegistryDataLoader.RegistryData<?>> param3
    ) {
        RegistryAccess.Frozen var0 = loadLayer(param0, param1, param2, param3);
        return param1.replaceFrom(param2, var0);
    }

    public static record DataLoadContext(
        ResourceManager resources, WorldDataConfiguration dataConfiguration, RegistryAccess.Frozen datapackWorldgen, RegistryAccess.Frozen datapackDimensions
    ) {
    }

    public static record DataLoadOutput<D>(D cookie, RegistryAccess.Frozen finalDimensions) {
    }

    public static record InitConfig(WorldLoader.PackConfig packConfig, Commands.CommandSelection commandSelection, int functionCompilationLevel) {
    }

    public static record PackConfig(PackRepository packRepository, WorldDataConfiguration initialDataConfig, boolean safeMode, boolean initMode) {
        public Pair<WorldDataConfiguration, CloseableResourceManager> createResourceManager() {
            FeatureFlagSet var0 = this.initMode ? FeatureFlags.REGISTRY.allFlags() : this.initialDataConfig.enabledFeatures();
            WorldDataConfiguration var1 = MinecraftServer.configurePackRepository(this.packRepository, this.initialDataConfig.dataPacks(), this.safeMode, var0);
            if (!this.initMode) {
                var1 = var1.expandFeatures(this.initialDataConfig.enabledFeatures());
            }

            List<PackResources> var2 = this.packRepository.openAllSelected();
            CloseableResourceManager var3 = new MultiPackResourceManager(PackType.SERVER_DATA, var2);
            return Pair.of(var1, var3);
        }
    }

    @FunctionalInterface
    public interface ResultFactory<D, R> {
        R create(CloseableResourceManager var1, ReloadableServerResources var2, LayeredRegistryAccess<RegistryLayer> var3, D var4);
    }

    @FunctionalInterface
    public interface WorldDataSupplier<D> {
        WorldLoader.DataLoadOutput<D> get(WorldLoader.DataLoadContext var1);
    }
}
