package net.minecraft.util.thread;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Queues;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import net.minecraft.util.profiling.metrics.MetricCategory;
import net.minecraft.util.profiling.metrics.MetricSampler;
import net.minecraft.util.profiling.metrics.MetricsRegistry;
import net.minecraft.util.profiling.metrics.ProfilerMeasured;
import org.slf4j.Logger;

public abstract class BlockableEventLoop<R extends Runnable> implements ProfilerMeasured, ProcessorHandle<R>, Executor {
    private final String name;
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Queue<R> pendingRunnables = Queues.newConcurrentLinkedQueue();
    private int blockingCount;

    protected BlockableEventLoop(String param0) {
        this.name = param0;
        MetricsRegistry.INSTANCE.add(this);
    }

    protected abstract R wrapRunnable(Runnable var1);

    protected abstract boolean shouldRun(R var1);

    public boolean isSameThread() {
        return Thread.currentThread() == this.getRunningThread();
    }

    protected abstract Thread getRunningThread();

    protected boolean scheduleExecutables() {
        return !this.isSameThread();
    }

    public int getPendingTasksCount() {
        return this.pendingRunnables.size();
    }

    @Override
    public String name() {
        return this.name;
    }

    public <V> CompletableFuture<V> submit(Supplier<V> param0) {
        return this.scheduleExecutables() ? CompletableFuture.supplyAsync(param0, this) : CompletableFuture.completedFuture(param0.get());
    }

    private CompletableFuture<Void> submitAsync(Runnable param0) {
        return CompletableFuture.supplyAsync(() -> {
            param0.run();
            return null;
        }, this);
    }

    public CompletableFuture<Void> submit(Runnable param0) {
        if (this.scheduleExecutables()) {
            return this.submitAsync(param0);
        } else {
            param0.run();
            return CompletableFuture.completedFuture(null);
        }
    }

    public void executeBlocking(Runnable param0) {
        if (!this.isSameThread()) {
            this.submitAsync(param0).join();
        } else {
            param0.run();
        }

    }

    public void tell(R param0) {
        this.pendingRunnables.add(param0);
        LockSupport.unpark(this.getRunningThread());
    }

    @Override
    public void execute(Runnable param0) {
        if (this.scheduleExecutables()) {
            this.tell(this.wrapRunnable(param0));
        } else {
            param0.run();
        }

    }

    public void executeIfPossible(Runnable param0) {
        this.execute(param0);
    }

    protected void dropAllTasks() {
        this.pendingRunnables.clear();
    }

    protected void runAllTasks() {
        while(this.pollTask()) {
        }

    }

    public boolean pollTask() {
        R var0 = this.pendingRunnables.peek();
        if (var0 == null) {
            return false;
        } else if (this.blockingCount == 0 && !this.shouldRun(var0)) {
            return false;
        } else {
            this.doRunTask(this.pendingRunnables.remove());
            return true;
        }
    }

    public void managedBlock(BooleanSupplier param0) {
        ++this.blockingCount;

        try {
            while(!param0.getAsBoolean()) {
                if (!this.pollTask()) {
                    this.waitForTasks();
                }
            }
        } finally {
            --this.blockingCount;
        }

    }

    protected void waitForTasks() {
        Thread.yield();
        LockSupport.parkNanos("waiting for tasks", 100000L);
    }

    protected void doRunTask(R param0) {
        try {
            param0.run();
        } catch (Exception var3) {
            LOGGER.error(LogUtils.FATAL_MARKER, "Error executing task on {}", this.name(), var3);
            throw var3;
        }
    }

    @Override
    public List<MetricSampler> profiledMetrics() {
        return ImmutableList.of(MetricSampler.create(this.name + "-pending-tasks", MetricCategory.EVENT_LOOPS, this::getPendingTasksCount));
    }
}
