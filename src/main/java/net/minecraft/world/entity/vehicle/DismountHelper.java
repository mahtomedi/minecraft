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
        return param0.getBlockCollisions(param1, param2).allMatch(VoxelShape::isEmpty);
    }

    @Nullable
    public static Vec3 findDismountLocation(CollisionGetter param0, double param1, double param2, double param3, LivingEntity param4, Pose param5) {
        if (isBlockFloorValid(param2)) {
            Vec3 var0 = new Vec3(param1, param2, param3);
            if (canDismountTo(param0, param4, param4.getLocalBoundsForPose(param5).move(var0))) {
                return var0;
            }
        }

        return null;
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
                return param1.getBlockCollisions(null, param0.getDimensions().makeBoundingBox(var1)).allMatch(VoxelShape::isEmpty) ? var1 : null;
            }
        }
    }
}
