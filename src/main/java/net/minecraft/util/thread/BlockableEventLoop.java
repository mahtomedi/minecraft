package net.minecraft.util.thread;

import com.google.common.collect.Queues;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class BlockableEventLoop<R extends Runnable> implements ProcessorHandle<R>, Executor {
    private final String name;
    private static final Logger LOGGER = LogManager.getLogger();
    private final Queue<R> pendingRunnables = Queues.newConcurrentLinkedQueue();
    private int blockingCount;

    protected BlockableEventLoop(String param0) {
        this.name = param0;
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

    @OnlyIn(Dist.CLIENT)
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

    @OnlyIn(Dist.CLIENT)
    protected void dropAllTasks() {
        this.pendingRunnables.clear();
    }

    public void runAllTasks() {
        while(this.pollTask()) {
        }

    }

    protected boolean pollTask() {
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
            LOGGER.fatal("Error executing task on {}", this.name(), var3);
        }

    }
}
