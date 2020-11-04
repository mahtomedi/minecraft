package net.minecraft.world.entity.ai.util;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.Vec3;

public class AirRandomPos {
    @Nullable
    public static Vec3 getPosTowards(PathfinderMob param0, int param1, int param2, int param3, Vec3 param4, double param5) {
        Vec3 var0 = param4.subtract(param0.getX(), param0.getY(), param0.getZ());
        boolean var1 = GoalUtils.mobRestricted(param0, param1);
        return RandomPos.generateRandomPos(param0, () -> {
            BlockPos var0x = AirAndWaterRandomPos.generateRandomPos(param0, param1, param2, param3, var0.x, var0.z, param5, var1);
            return var0x != null && !GoalUtils.isWater(param0, var0x) ? var0x : null;
        });
    }
}
