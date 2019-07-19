package net.minecraft.world.entity.ai.memory;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.behavior.BlockPosWrapper;
import net.minecraft.world.entity.ai.behavior.PositionWrapper;
import net.minecraft.world.phys.Vec3;

public class WalkTarget {
    private final PositionWrapper target;
    private final float speed;
    private final int closeEnoughDist;

    public WalkTarget(BlockPos param0, float param1, int param2) {
        this(new BlockPosWrapper(param0), param1, param2);
    }

    public WalkTarget(Vec3 param0, float param1, int param2) {
        this(new BlockPosWrapper(new BlockPos(param0)), param1, param2);
    }

    public WalkTarget(PositionWrapper param0, float param1, int param2) {
        this.target = param0;
        this.speed = param1;
        this.closeEnoughDist = param2;
    }

    public PositionWrapper getTarget() {
        return this.target;
    }

    public float getSpeed() {
        return this.speed;
    }

    public int getCloseEnoughDist() {
        return this.closeEnoughDist;
    }
}
