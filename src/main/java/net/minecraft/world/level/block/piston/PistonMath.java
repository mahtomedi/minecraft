package net.minecraft.world.level.block.piston;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;

public class PistonMath {
    public static List<AABB> getEntityMovementAreas(boolean param0, AABB param1, Direction param2, double param3) {
        ArrayList<AABB> var0 = Lists.newArrayList(getEntityMovementAreaInFront(param1, param2, param3));
        if (param0 && param2.getAxis().isHorizontal()) {
            var0.add(getEntityMovementAreaOnTop(param1));
        }

        return var0;
    }

    public static AABB getEntityMovementAreaOnTop(AABB param0) {
        return new AABB(param0.minX, param0.maxY, param0.minZ, param0.maxX, param0.maxY + 1.0E-7, param0.maxZ);
    }

    public static AABB getEntityMovementAreaInFront(AABB param0, Direction param1, double param2) {
        double var0 = param2 * (double)param1.getAxisDirection().getStep();
        double var1 = Math.min(var0, 0.0);
        double var2 = Math.max(var0, 0.0);
        switch(param1) {
            case WEST:
                return new AABB(param0.minX + var1, param0.minY, param0.minZ, param0.minX + var2, param0.maxY, param0.maxZ);
            case EAST:
                return new AABB(param0.maxX + var1, param0.minY, param0.minZ, param0.maxX + var2, param0.maxY, param0.maxZ);
            case DOWN:
                return new AABB(param0.minX, param0.minY + var1, param0.minZ, param0.maxX, param0.minY + var2, param0.maxZ);
            case UP:
            default:
                return new AABB(param0.minX, param0.maxY + var1, param0.minZ, param0.maxX, param0.maxY + var2, param0.maxZ);
            case NORTH:
                return new AABB(param0.minX, param0.minY, param0.minZ + var1, param0.maxX, param0.maxY, param0.minZ + var2);
            case SOUTH:
                return new AABB(param0.minX, param0.minY, param0.maxZ + var1, param0.maxX, param0.maxY, param0.maxZ + var2);
        }
    }
}
