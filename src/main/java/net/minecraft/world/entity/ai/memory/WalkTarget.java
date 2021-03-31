package net.minecraft.world.entity.ai.memory;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.phys.Vec3;

public class WalkTarget {
    private final PositionTracker target;
    private final float speedModifier;
    private final int closeEnoughDist;

    public WalkTarget(BlockPos param0, float param1, int param2) {
        this(new BlockPosTracker(param0), param1, param2);
    }

    public WalkTarget(Vec3 param0, float param1, int param2) {
        this(new BlockPosTracker(new BlockPos(param0)), param1, param2);
    }

    public WalkTarget(Entity param0, float param1, int param2) {
        this(new EntityTracker(param0, false), param1, param2);
    }

    public WalkTarget(PositionTracker param0, float param1, int param2) {
        this.target = param0;
        this.speedModifier = param1;
        this.closeEnoughDist = param2;
    }

    public PositionTracker getTarget() {
        return this.target;
    }

    public float getSpeedModifier() {
        return this.speedModifier;
    }

    public int getCloseEnoughDist() {
        return this.closeEnoughDist;
    }
}
