package net.minecraft.world.entity.vehicle;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
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

    public static boolean isFloorValid(double param0) {
        return !Double.isInfinite(param0) && param0 < 1.0;
    }

    public static boolean canDismountTo(Level param0, LivingEntity param1, AABB param2) {
        return param0.getBlockCollisions(param1, param2).allMatch(VoxelShape::isEmpty);
    }
}
