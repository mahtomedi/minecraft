package net.minecraft.world.entity;

import java.util.function.Consumer;
import net.minecraft.Util;

public class AnimationState {
    private static final long STOPPED = Long.MAX_VALUE;
    private long lastTime = Long.MAX_VALUE;
    private long accumulatedTime;

    public void start() {
        this.lastTime = Util.getMillis();
        this.accumulatedTime = 0L;
    }

    public void startIfStopped() {
        if (!this.isStarted()) {
            this.start();
        }

    }

    public void stop() {
        this.lastTime = Long.MAX_VALUE;
    }

    public void ifStarted(Consumer<AnimationState> param0) {
        if (this.isStarted()) {
            param0.accept(this);
        }

    }

    public void updateTime(boolean param0, float param1) {
        if (this.isStarted()) {
            long var0 = Util.getMillis();
            if (!param0) {
                this.accumulatedTime += (long)((float)(var0 - this.lastTime) * param1);
            }

            this.lastTime = var0;
        }
    }

    public long getAccumulatedTime() {
        return this.accumulatedTime;
    }

    public boolean isStarted() {
        return this.lastTime != Long.MAX_VALUE;
    }
}
