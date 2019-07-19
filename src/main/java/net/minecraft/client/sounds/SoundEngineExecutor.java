package net.minecraft.client.sounds;

import java.util.concurrent.locks.LockSupport;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SoundEngineExecutor extends BlockableEventLoop<Runnable> {
    private Thread thread = this.createThread();
    private volatile boolean shutdown;

    public SoundEngineExecutor() {
        super("Sound executor");
    }

    private Thread createThread() {
        Thread var0 = new Thread(this::run);
        var0.setDaemon(true);
        var0.setName("Sound engine");
        var0.start();
        return var0;
    }

    @Override
    protected Runnable wrapRunnable(Runnable param0) {
        return param0;
    }

    @Override
    protected boolean shouldRun(Runnable param0) {
        return !this.shutdown;
    }

    @Override
    protected Thread getRunningThread() {
        return this.thread;
    }

    private void run() {
        while(!this.shutdown) {
            this.managedBlock(() -> this.shutdown);
        }

    }

    @Override
    protected void waitForTasks() {
        LockSupport.park("waiting for tasks");
    }

    public void flush() {
        this.shutdown = true;
        this.thread.interrupt();

        try {
            this.thread.join();
        } catch (InterruptedException var2) {
            Thread.currentThread().interrupt();
        }

        this.dropAllTasks();
        this.shutdown = false;
        this.thread = this.createThread();
    }
}
