package net.minecraft.util.thread;

public abstract class ReentrantBlockableEventLoop<R extends Runnable> extends BlockableEventLoop<R> {
    private int reentrantCount;

    public ReentrantBlockableEventLoop(String param0) {
        super(param0);
    }

    @Override
    protected boolean scheduleExecutables() {
        return this.runningTask() || super.scheduleExecutables();
    }

    protected boolean runningTask() {
        return this.reentrantCount != 0;
    }

    @Override
    protected void doRunTask(R param0) {
        ++this.reentrantCount;

        try {
            super.doRunTask(param0);
        } finally {
            --this.reentrantCount;
        }

    }
}
