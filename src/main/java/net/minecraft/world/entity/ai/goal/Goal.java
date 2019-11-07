package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;

public abstract class Goal {
    private final EnumSet<Goal.Flag> flags = EnumSet.noneOf(Goal.Flag.class);

    public abstract boolean canUse();

    public boolean canContinueToUse() {
        return this.canUse();
    }

    public boolean isInterruptable() {
        return true;
    }

    public void start() {
    }

    public void stop() {
    }

    public void tick() {
    }

    public void setFlags(EnumSet<Goal.Flag> param0) {
        this.flags.clear();
        this.flags.addAll(param0);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

    public EnumSet<Goal.Flag> getFlags() {
        return this.flags;
    }

    public static enum Flag {
        MOVE,
        LOOK,
        JUMP,
        TARGET;
    }
}
