package net.minecraft.server;

public class TickTask implements Runnable {
    private final int tick;
    private final Runnable runnable;

    public TickTask(int param0, Runnable param1) {
        this.tick = param0;
        this.runnable = param1;
    }

    public int getTick() {
        return this.tick;
    }

    @Override
    public void run() {
        this.runnable.run();
    }
}
