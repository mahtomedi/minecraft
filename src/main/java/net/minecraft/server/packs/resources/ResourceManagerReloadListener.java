package net.minecraft.server.packs.resources;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.ProfilerFiller;

public interface ResourceManagerReloadListener extends PreparableReloadListener {
    @Override
    default CompletableFuture<Void> reload(
        PreparableReloadListener.PreparationBarrier param0,
        ResourceManager param1,
        ProfilerFiller param2,
        ProfilerFiller param3,
        Executor param4,
        Executor param5
    ) {
        return param0.wait(Unit.INSTANCE).thenRunAsync(() -> {
            param3.startTick();
            param3.push("listener");
            this.onResourceManagerReload(param1);
            param3.pop();
            param3.endTick();
        }, param5);
    }

    void onResourceManagerReload(ResourceManager var1);
}
