package net.minecraft.world.phys;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public class BlockHitResult extends HitResult {
    private final Direction direction;
    private final BlockPos blockPos;
    private final boolean miss;
    private final boolean inside;

    public static BlockHitResult miss(Vec3 param0, Direction param1, BlockPos param2) {
        return new BlockHitResult(true, param0, param1, param2, false);
    }

    public BlockHitResult(Vec3 param0, Direction param1, BlockPos param2, boolean param3) {
        this(false, param0, param1, param2, param3);
    }

    private BlockHitResult(boolean param0, Vec3 param1, Direction param2, BlockPos param3, boolean param4) {
        super(param1);
        this.miss = param0;
        this.direction = param2;
        this.blockPos = param3;
        this.inside = param4;
    }

    public BlockHitResult withDirection(Direction param0) {
        return new BlockHitResult(this.miss, this.location, param0, this.blockPos, this.inside);
    }

    public BlockHitResult withPosition(BlockPos param0) {
        return new BlockHitResult(this.miss, this.location, this.direction, param0, this.inside);
    }

    public BlockPos getBlockPos() {
        return this.blockPos;
    }

    public Direction getDirection() {
        return this.direction;
    }

    @Override
    public HitResult.Type getType() {
        return this.miss ? HitResult.Type.MISS : HitResult.Type.BLOCK;
    }

    public boolean isInside() {
        return this.inside;
    }
}
