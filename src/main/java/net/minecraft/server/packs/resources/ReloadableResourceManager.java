package net.minecraft.server.packs.resources;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.server.packs.PackResources;
import net.minecraft.util.Unit;

public interface ReloadableResourceManager extends AutoCloseable, ResourceManager {
    default CompletableFuture<Unit> reload(Executor param0, Executor param1, List<PackResources> param2, CompletableFuture<Unit> param3) {
        return this.createFullReload(param0, param1, param3, param2).done();
    }

    ReloadInstance createFullReload(Executor var1, Executor var2, CompletableFuture<Unit> var3, List<PackResources> var4);

    void registerReloadListener(PreparableReloadListener var1);

    @Override
    void close();
}
