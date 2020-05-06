package net.minecraft.util.thread;

import it.unimi.dsi.fastutil.ints.Int2BooleanFunction;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.SharedConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ProcessorMailbox<T> implements ProcessorHandle<T>, AutoCloseable, Runnable {
    private static final Logger LOGGER = LogManager.getLogger();
    private final AtomicInteger status = new AtomicInteger(0);
    public final StrictQueue<? super T, ? extends Runnable> queue;
    private final Executor dispatcher;
    private final String name;

    public static ProcessorMailbox<Runnable> create(Executor param0, String param1) {
        return new ProcessorMailbox<>(new StrictQueue.QueueStrictQueue<>(new ConcurrentLinkedQueue<>()), param0, param1);
    }

    public ProcessorMailbox(StrictQueue<? super T, ? extends Runnable> param0, Executor param1, String param2) {
        this.dispatcher = param1;
        this.queue = param0;
        this.name = param2;
    }

    private boolean setAsScheduled() {
        int var0;
        do {
            var0 = this.status.get();
            if ((var0 & 3) != 0) {
                return false;
            }
        } while(!this.status.compareAndSet(var0, var0 | 2));

        return true;
    }

    private void setAsIdle() {
        int var0;
        do {
            var0 = this.status.get();
        } while(!this.status.compareAndSet(var0, var0 & -3));

    }

    private boolean canBeScheduled() {
        if ((this.status.get() & 1) != 0) {
            return false;
        } else {
            return !this.queue.isEmpty();
        }
    }

    @Override
    public void close() {
        int var0;
        do {
            var0 = this.status.get();
        } while(!this.status.compareAndSet(var0, var0 | 1));

    }

    private boolean shouldProcess() {
        return (this.status.get() & 2) != 0;
    }

    private boolean pollTask() {
        if (!this.shouldProcess()) {
            return false;
        } else {
            Runnable var0 = this.queue.pop();
            if (var0 == null) {
                return false;
            } else {
                String var2;
                Thread var1;
                if (SharedConstants.IS_RUNNING_IN_IDE) {
                    var1 = Thread.currentThread();
                    var2 = var1.getName();
                    var1.setName(this.name);
                } else {
                    var1 = null;
                    var2 = null;
                }

                var0.run();
                if (var1 != null) {
                    var1.setName(var2);
                }

                return true;
            }
        }
    }

    @Override
    public void run() {
        try {
            this.pollUntil(param0 -> param0 == 0);
        } finally {
            this.setAsIdle();
            this.registerForExecution();
        }

    }

    @Override
    public void tell(T param0) {
        this.queue.push(param0);
        this.registerForExecution();
    }

    private void registerForExecution() {
        if (this.canBeScheduled() && this.setAsScheduled()) {
            try {
                this.dispatcher.execute(this);
            } catch (RejectedExecutionException var4) {
                try {
                    this.dispatcher.execute(this);
                } catch (RejectedExecutionException var3) {
                    LOGGER.error("Cound not schedule mailbox", (Throwable)var3);
                }
            }
        }

    }

    private int pollUntil(Int2BooleanFunction param0) {
        int var0 = 0;

        while(param0.get(var0) && this.pollTask()) {
            ++var0;
        }

        return var0;
    }

    @Override
    public String toString() {
        return this.name + " " + this.status.get() + " " + this.queue.isEmpty();
    }

    @Override
    public String name() {
        return this.name;
    }
}
