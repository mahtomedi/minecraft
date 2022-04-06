package net.minecraft.server;

import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.commands.Commands;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.DataPackConfig;

public class WorldLoader {
    public static <D, R> CompletableFuture<R> load(
        WorldLoader.InitConfig param0, WorldLoader.WorldDataSupplier<D> param1, WorldLoader.ResultFactory<D, R> param2, Executor param3, Executor param4
    ) {
        try {
            Pair<DataPackConfig, CloseableResourceManager> var0 = param0.packConfig.createResourceManager();
            CloseableResourceManager var1 = var0.getSecond();
            Pair<D, RegistryAccess.Frozen> var2 = param1.get(var1, var0.getFirst());
            D var3 = var2.getFirst();
            RegistryAccess.Frozen var4 = var2.getSecond();
            return ReloadableServerResources.loadResources(var1, var4, param0.commandSelection(), param0.functionCompilationLevel(), param3, param4)
                .whenComplete((param1x, param2x) -> {
                    if (param2x != null) {
                        var1.close();
                    }
    
                })
                .thenApplyAsync(param4x -> {
                    param4x.updateRegistryTags(var4);
                    return param2.create(var1, param4x, var4, var3);
                }, param4);
        } catch (Exception var10) {
            return CompletableFuture.failedFuture(var10);
        }
    }

    public static record InitConfig(WorldLoader.PackConfig packConfig, Commands.CommandSelection commandSelection, int functionCompilationLevel) {
    }

    public static record PackConfig(PackRepository packRepository, DataPackConfig initialDataPacks, boolean safeMode) {
        public Pair<DataPackConfig, CloseableResourceManager> createResourceManager() {
            DataPackConfig var0 = MinecraftServer.configurePackRepository(this.packRepository, this.initialDataPacks, this.safeMode);
            List<PackResources> var1 = this.packRepository.openAllSelected();
            CloseableResourceManager var2 = new MultiPackResourceManager(PackType.SERVER_DATA, var1);
            return Pair.of(var0, var2);
        }
    }

    @FunctionalInterface
    public interface ResultFactory<D, R> {
        R create(CloseableResourceManager var1, ReloadableServerResources var2, RegistryAccess.Frozen var3, D var4);
    }

    @FunctionalInterface
    public interface WorldDataSupplier<D> {
        Pair<D, RegistryAccess.Frozen> get(ResourceManager var1, DataPackConfig var2);
    }
}
