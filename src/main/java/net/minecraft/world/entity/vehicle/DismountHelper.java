package net.minecraft.world.entity.vehicle;

import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DismountHelper {
    public static int[][] offsetsForDirection(Direction param0) {
        Direction var0 = param0.getClockWise();
        Direction var1 = var0.getOpposite();
        Direction var2 = param0.getOpposite();
        return new int[][]{
            {var0.getStepX(), var0.getStepZ()},
            {var1.getStepX(), var1.getStepZ()},
            {var2.getStepX() + var0.getStepX(), var2.getStepZ() + var0.getStepZ()},
            {var2.getStepX() + var1.getStepX(), var2.getStepZ() + var1.getStepZ()},
            {param0.getStepX() + var0.getStepX(), param0.getStepZ() + var0.getStepZ()},
            {param0.getStepX() + var1.getStepX(), param0.getStepZ() + var1.getStepZ()},
            {var2.getStepX(), var2.getStepZ()},
            {param0.getStepX(), param0.getStepZ()}
        };
    }

    public static boolean isBlockFloorValid(double param0) {
        return !Double.isInfinite(param0) && param0 < 1.0;
    }

    public static boolean canDismountTo(CollisionGetter param0, LivingEntity param1, AABB param2) {
        for(VoxelShape var1 : param0.getBlockCollisions(param1, param2)) {
            if (!var1.isEmpty()) {
                return false;
            }
        }

        return param0.getWorldBorder().isWithinBounds(param2);
    }

    public static boolean canDismountTo(CollisionGetter param0, Vec3 param1, LivingEntity param2, Pose param3) {
        return canDismountTo(param0, param2, param2.getLocalBoundsForPose(param3).move(param1));
    }

    public static VoxelShape nonClimbableShape(BlockGetter param0, BlockPos param1) {
        BlockState var0 = param0.getBlockState(param1);
        return !var0.is(BlockTags.CLIMBABLE) && (!(var0.getBlock() instanceof TrapDoorBlock) || !var0.getValue(TrapDoorBlock.OPEN))
            ? var0.getCollisionShape(param0, param1)
            : Shapes.empty();
    }

    public static double findCeilingFrom(BlockPos param0, int param1, Function<BlockPos, VoxelShape> param2) {
        BlockPos.MutableBlockPos var0 = param0.mutable();
        int var1 = 0;

        while(var1 < param1) {
            VoxelShape var2 = param2.apply(var0);
            if (!var2.isEmpty()) {
                return (double)(param0.getY() + var1) + var2.min(Direction.Axis.Y);
            }

            ++var1;
            var0.move(Direction.UP);
        }

        return Double.POSITIVE_INFINITY;
    }

    @Nullable
    public static Vec3 findSafeDismountLocation(EntityType<?> param0, CollisionGetter param1, BlockPos param2, boolean param3) {
        if (param3 && param0.isBlockDangerous(param1.getBlockState(param2))) {
            return null;
        } else {
            double var0 = param1.getBlockFloorHeight(nonClimbableShape(param1, param2), () -> nonClimbableShape(param1, param2.below()));
            if (!isBlockFloorValid(var0)) {
                return null;
            } else if (param3 && var0 <= 0.0 && param0.isBlockDangerous(param1.getBlockState(param2.below()))) {
                return null;
            } else {
                Vec3 var1 = Vec3.upFromBottomCenterOf(param2, var0);
                AABB var2 = param0.getDimensions().makeBoundingBox(var1);

                for(VoxelShape var4 : param1.getBlockCollisions(null, var2)) {
                    if (!var4.isEmpty()) {
                        return null;
                    }
                }

                if (param0 != EntityType.PLAYER
                    || !param1.getBlockState(param2).is(BlockTags.INVALID_SPAWN_INSIDE)
                        && !param1.getBlockState(param2.above()).is(BlockTags.INVALID_SPAWN_INSIDE)) {
                    return !param1.getWorldBorder().isWithinBounds(var2) ? null : var1;
                } else {
                    return null;
                }
            }
        }
    }
}
