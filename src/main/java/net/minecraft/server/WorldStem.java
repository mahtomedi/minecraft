package net.minecraft.server;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.world.level.storage.WorldData;

public record WorldStem(
    CloseableResourceManager resourceManager, ReloadableServerResources dataPackResources, RegistryAccess.Frozen registryAccess, WorldData worldData
) implements AutoCloseable {
    public static CompletableFuture<WorldStem> load(
        WorldLoader.InitConfig param0, WorldLoader.WorldDataSupplier<WorldData> param1, Executor param2, Executor param3
    ) {
        return WorldLoader.load(param0, param1, WorldStem::new, param2, param3);
    }

    @Override
    public void close() {
        this.resourceManager.close();
    }
}
