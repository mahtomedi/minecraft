package net.minecraft.server.packs.resources;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.server.packs.Pack;
import net.minecraft.util.Unit;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface ReloadableResourceManager extends ResourceManager {
    CompletableFuture<Unit> reload(Executor var1, Executor var2, List<Pack> var3, CompletableFuture<Unit> var4);

    @OnlyIn(Dist.CLIENT)
    ReloadInstance createFullReload(Executor var1, Executor var2, CompletableFuture<Unit> var3, List<Pack> var4);

    void registerReloadListener(PreparableReloadListener var1);
}
