package net.minecraft.world.entity.ai.behavior;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.IntRange;
import net.minecraft.world.entity.LivingEntity;

public class RunSometimes<E extends LivingEntity> extends Behavior<E> {
    private boolean resetTicks;
    private boolean wasRunning;
    private final IntRange interval;
    private final Behavior<? super E> wrappedBehavior;
    private int ticksUntilNextStart;

    public RunSometimes(Behavior<? super E> param0, IntRange param1) {
        this(param0, false, param1);
    }

    public RunSometimes(Behavior<? super E> param0, boolean param1, IntRange param2) {
        super(param0.entryCondition);
        this.wrappedBehavior = param0;
        this.resetTicks = !param1;
        this.interval = param2;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel param0, E param1) {
        if (!this.wrappedBehavior.checkExtraStartConditions(param0, param1)) {
            return false;
        } else {
            if (this.resetTicks) {
                this.resetTicksUntilNextStart(param0);
                this.resetTicks = false;
            }

            if (this.ticksUntilNextStart > 0) {
                --this.ticksUntilNextStart;
            }

            return !this.wasRunning && this.ticksUntilNextStart == 0;
        }
    }

    @Override
    protected void start(ServerLevel param0, E param1, long param2) {
        this.wrappedBehavior.start(param0, param1, param2);
    }

    @Override
    protected boolean canStillUse(ServerLevel param0, E param1, long param2) {
        return this.wrappedBehavior.canStillUse(param0, param1, param2);
    }

    @Override
    protected void tick(ServerLevel param0, E param1, long param2) {
        this.wrappedBehavior.tick(param0, param1, param2);
        this.wasRunning = this.wrappedBehavior.getStatus() == Behavior.Status.RUNNING;
    }

    @Override
    protected void stop(ServerLevel param0, E param1, long param2) {
        this.resetTicksUntilNextStart(param0);
        this.wrappedBehavior.stop(param0, param1, param2);
    }

    private void resetTicksUntilNextStart(ServerLevel param0) {
        this.ticksUntilNextStart = this.interval.randomValue(param0.random);
    }

    @Override
    protected boolean timedOut(long param0) {
        return false;
    }

    @Override
    public String toString() {
        return "RunSometimes: " + this.wrappedBehavior;
    }
}
