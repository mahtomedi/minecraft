package net.minecraft.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class DirectionalPlaceContext extends BlockPlaceContext {
    private final Direction direction;

    public DirectionalPlaceContext(Level param0, BlockPos param1, Direction param2, ItemStack param3, Direction param4) {
        super(
            param0,
            null,
            InteractionHand.MAIN_HAND,
            param3,
            new BlockHitResult(new Vec3((double)param1.getX() + 0.5, (double)param1.getY(), (double)param1.getZ() + 0.5), param4, param1, false)
        );
        this.direction = param2;
    }

    @Override
    public BlockPos getClickedPos() {
        return this.hitResult.getBlockPos();
    }

    @Override
    public boolean canPlace() {
        return this.level.getBlockState(this.hitResult.getBlockPos()).canBeReplaced(this);
    }

    @Override
    public boolean replacingClickedOnBlock() {
        return this.canPlace();
    }

    @Override
    public Direction getNearestLookingDirection() {
        return Direction.DOWN;
    }

    @Override
    public Direction[] getNearestLookingDirections() {
        switch(this.direction) {
            case DOWN:
            default:
                return new Direction[]{Direction.DOWN, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP};
            case UP:
                return new Direction[]{Direction.DOWN, Direction.UP, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
            case NORTH:
                return new Direction[]{Direction.DOWN, Direction.NORTH, Direction.EAST, Direction.WEST, Direction.UP, Direction.SOUTH};
            case SOUTH:
                return new Direction[]{Direction.DOWN, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.UP, Direction.NORTH};
            case WEST:
                return new Direction[]{Direction.DOWN, Direction.WEST, Direction.SOUTH, Direction.UP, Direction.NORTH, Direction.EAST};
            case EAST:
                return new Direction[]{Direction.DOWN, Direction.EAST, Direction.SOUTH, Direction.UP, Direction.NORTH, Direction.WEST};
        }
    }

    @Override
    public Direction getHorizontalDirection() {
        return this.direction.getAxis() == Direction.Axis.Y ? Direction.NORTH : this.direction;
    }

    @Override
    public boolean isSneaking() {
        return false;
    }

    @Override
    public float getRotation() {
        return (float)(this.direction.get2DDataValue() * 90);
    }
}
