package net.minecraft.server.packs.resources;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.util.profiling.ProfilerFiller;

public abstract class SimplePreparableReloadListener<T> implements PreparableReloadListener {
    @Override
    public final CompletableFuture<Void> reload(
        PreparableReloadListener.PreparationBarrier param0,
        ResourceManager param1,
        ProfilerFiller param2,
        ProfilerFiller param3,
        Executor param4,
        Executor param5
    ) {
        return CompletableFuture.<T>supplyAsync(() -> this.prepare(param1, param2), param4)
            .thenCompose(param0::wait)
            .thenAcceptAsync(param2x -> this.apply(param2x, param1, param3), param5);
    }

    protected abstract T prepare(ResourceManager var1, ProfilerFiller var2);

    protected abstract void apply(T var1, ResourceManager var2, ProfilerFiller var3);
}
