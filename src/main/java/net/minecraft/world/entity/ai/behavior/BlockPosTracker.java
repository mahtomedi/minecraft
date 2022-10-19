package net.minecraft.world.entity.ai.behavior;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class BlockPosTracker implements PositionTracker {
    private final BlockPos blockPos;
    private final Vec3 centerPosition;

    public BlockPosTracker(BlockPos param0) {
        this.blockPos = param0.immutable();
        this.centerPosition = Vec3.atCenterOf(param0);
    }

    public BlockPosTracker(Vec3 param0) {
        this.blockPos = new BlockPos(param0);
        this.centerPosition = param0;
    }

    @Override
    public Vec3 currentPosition() {
        return this.centerPosition;
    }

    @Override
    public BlockPos currentBlockPosition() {
        return this.blockPos;
    }

    @Override
    public boolean isVisibleBy(LivingEntity param0) {
        return true;
    }

    @Override
    public String toString() {
        return "BlockPosTracker{blockPos=" + this.blockPos + ", centerPosition=" + this.centerPosition + "}";
    }
}
