package net.minecraft.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;

public class ShulkerSharedHelper {
    public static AABB openBoundingBox(BlockPos param0, Direction param1) {
        return Shapes.block()
            .bounds()
            .expandTowards((double)(0.5F * (float)param1.getStepX()), (double)(0.5F * (float)param1.getStepY()), (double)(0.5F * (float)param1.getStepZ()))
            .contract((double)param1.getStepX(), (double)param1.getStepY(), (double)param1.getStepZ())
            .move(param0.relative(param1));
    }
}
