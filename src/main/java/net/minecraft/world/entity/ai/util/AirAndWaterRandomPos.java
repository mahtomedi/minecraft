package net.minecraft.world.entity.ai.util;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.Vec3;

public class AirAndWaterRandomPos {
    @Nullable
    public static Vec3 getPos(PathfinderMob param0, int param1, int param2, int param3, double param4, double param5, double param6) {
        boolean var0 = GoalUtils.mobRestricted(param0, param1);
        return RandomPos.generateRandomPos(param0, () -> generateRandomPos(param0, param1, param2, param3, param4, param5, param6, var0));
    }

    @Nullable
    public static BlockPos generateRandomPos(
        PathfinderMob param0, int param1, int param2, int param3, double param4, double param5, double param6, boolean param7
    ) {
        BlockPos var0 = RandomPos.generateRandomDirectionWithinRadians(param0.getRandom(), param1, param2, param3, param4, param5, param6);
        if (var0 == null) {
            return null;
        } else {
            BlockPos var1 = RandomPos.generateRandomPosTowardDirection(param0, param1, param0.getRandom(), var0);
            if (!GoalUtils.isOutsideLimits(var1, param0) && !GoalUtils.isRestricted(param7, param0, var1)) {
                var1 = RandomPos.moveUpOutOfSolid(var1, param0.level().getMaxBuildHeight(), param1x -> GoalUtils.isSolid(param0, param1x));
                return GoalUtils.hasMalus(param0, var1) ? null : var1;
            } else {
                return null;
            }
        }
    }
}
