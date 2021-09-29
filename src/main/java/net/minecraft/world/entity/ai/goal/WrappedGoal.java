package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import javax.annotation.Nullable;

public class WrappedGoal extends Goal {
    private final Goal goal;
    private final int priority;
    private boolean isRunning;

    public WrappedGoal(int param0, Goal param1) {
        this.priority = param0;
        this.goal = param1;
    }

    public boolean canBeReplacedBy(WrappedGoal param0) {
        return this.isInterruptable() && param0.getPriority() < this.getPriority();
    }

    @Override
    public boolean canUse() {
        return this.goal.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return this.goal.canContinueToUse();
    }

    @Override
    public boolean isInterruptable() {
        return this.goal.isInterruptable();
    }

    @Override
    public void start() {
        if (!this.isRunning) {
            this.isRunning = true;
            this.goal.start();
        }
    }

    @Override
    public void stop() {
        if (this.isRunning) {
            this.isRunning = false;
            this.goal.stop();
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return this.goal.requiresUpdateEveryTick();
    }

    @Override
    protected int adjustedTickDelay(int param0) {
        return this.goal.adjustedTickDelay(param0);
    }

    @Override
    public void tick() {
        this.goal.tick();
    }

    @Override
    public void setFlags(EnumSet<Goal.Flag> param0) {
        this.goal.setFlags(param0);
    }

    @Override
    public EnumSet<Goal.Flag> getFlags() {
        return this.goal.getFlags();
    }

    public boolean isRunning() {
        return this.isRunning;
    }

    public int getPriority() {
        return this.priority;
    }

    public Goal getGoal() {
        return this.goal;
    }

    @Override
    public boolean equals(@Nullable Object param0) {
        if (this == param0) {
            return true;
        } else {
            return param0 != null && this.getClass() == param0.getClass() ? this.goal.equals(((WrappedGoal)param0).goal) : false;
        }
    }

    @Override
    public int hashCode() {
        return this.goal.hashCode();
    }
}
