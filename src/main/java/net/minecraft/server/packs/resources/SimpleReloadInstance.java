package net.minecraft.server.packs.resources;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.Util;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.InactiveProfiler;

public class SimpleReloadInstance<S> implements ReloadInstance {
    private static final int PREPARATION_PROGRESS_WEIGHT = 2;
    private static final int EXTRA_RELOAD_PROGRESS_WEIGHT = 2;
    private static final int LISTENER_PROGRESS_WEIGHT = 1;
    protected final CompletableFuture<Unit> allPreparations = new CompletableFuture<>();
    protected CompletableFuture<List<S>> allDone;
    final Set<PreparableReloadListener> preparingListeners;
    private final int listenerCount;
    private int startedReloads;
    private int finishedReloads;
    private final AtomicInteger startedTaskCounter = new AtomicInteger();
    private final AtomicInteger doneTaskCounter = new AtomicInteger();

    public static SimpleReloadInstance<Void> of(
        ResourceManager param0, List<PreparableReloadListener> param1, Executor param2, Executor param3, CompletableFuture<Unit> param4
    ) {
        return new SimpleReloadInstance<>(
            param2,
            param3,
            param0,
            param1,
            (param1x, param2x, param3x, param4x, param5) -> param3x.reload(
                    param1x, param2x, InactiveProfiler.INSTANCE, InactiveProfiler.INSTANCE, param2, param5
                ),
            param4
        );
    }

    protected SimpleReloadInstance(
        Executor param0,
        final Executor param1,
        ResourceManager param2,
        List<PreparableReloadListener> param3,
        SimpleReloadInstance.StateFactory<S> param4,
        CompletableFuture<Unit> param5
    ) {
        this.listenerCount = param3.size();
        this.startedTaskCounter.incrementAndGet();
        param5.thenRun(this.doneTaskCounter::incrementAndGet);
        List<CompletableFuture<S>> var0 = Lists.newArrayList();
        CompletableFuture<?> var1 = param5;
        this.preparingListeners = Sets.newHashSet(param3);

        for(final PreparableReloadListener var2 : param3) {
            final CompletableFuture<?> var3 = var1;
            CompletableFuture<S> var4 = param4.create(new PreparableReloadListener.PreparationBarrier() {
                @Override
                public <T> CompletableFuture<T> wait(T param0) {
                    param1.execute(() -> {
                        SimpleReloadInstance.this.preparingListeners.remove(var2);
                        if (SimpleReloadInstance.this.preparingListeners.isEmpty()) {
                            SimpleReloadInstance.this.allPreparations.complete(Unit.INSTANCE);
                        }

                    });
                    return SimpleReloadInstance.this.allPreparations.thenCombine(var3, (param1xx, param2) -> param0);
                }
            }, param2, var2, param1x -> {
                this.startedTaskCounter.incrementAndGet();
                param0.execute(() -> {
                    param1x.run();
                    this.doneTaskCounter.incrementAndGet();
                });
            }, param1x -> {
                ++this.startedReloads;
                param1.execute(() -> {
                    param1x.run();
                    ++this.finishedReloads;
                });
            });
            var0.add(var4);
            var1 = var4;
        }

        this.allDone = Util.sequenceFailFast(var0);
    }

    @Override
    public CompletableFuture<?> done() {
        return this.allDone;
    }

    @Override
    public float getActualProgress() {
        int var0 = this.listenerCount - this.preparingListeners.size();
        float var1 = (float)(this.doneTaskCounter.get() * 2 + this.finishedReloads * 2 + var0 * 1);
        float var2 = (float)(this.startedTaskCounter.get() * 2 + this.startedReloads * 2 + this.listenerCount * 1);
        return var1 / var2;
    }

    public static ReloadInstance create(
        ResourceManager param0, List<PreparableReloadListener> param1, Executor param2, Executor param3, CompletableFuture<Unit> param4, boolean param5
    ) {
        return (ReloadInstance)(param5 ? new ProfiledReloadInstance(param0, param1, param2, param3, param4) : of(param0, param1, param2, param3, param4));
    }

    protected interface StateFactory<S> {
        CompletableFuture<S> create(
            PreparableReloadListener.PreparationBarrier var1, ResourceManager var2, PreparableReloadListener var3, Executor var4, Executor var5
        );
    }
}
