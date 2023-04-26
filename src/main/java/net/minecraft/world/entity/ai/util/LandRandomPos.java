package net.minecraft.world.entity.ai.util;

import java.util.function.ToDoubleFunction;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.Vec3;

public class LandRandomPos {
    @Nullable
    public static Vec3 getPos(PathfinderMob param0, int param1, int param2) {
        return getPos(param0, param1, param2, param0::getWalkTargetValue);
    }

    @Nullable
    public static Vec3 getPos(PathfinderMob param0, int param1, int param2, ToDoubleFunction<BlockPos> param3) {
        boolean var0 = GoalUtils.mobRestricted(param0, param1);
        return RandomPos.generateRandomPos(() -> {
            BlockPos var0x = RandomPos.generateRandomDirection(param0.getRandom(), param1, param2);
            BlockPos var1x = generateRandomPosTowardDirection(param0, param1, var0, var0x);
            return var1x == null ? null : movePosUpOutOfSolid(param0, var1x);
        }, param3);
    }

    @Nullable
    public static Vec3 getPosTowards(PathfinderMob param0, int param1, int param2, Vec3 param3) {
        Vec3 var0 = param3.subtract(param0.getX(), param0.getY(), param0.getZ());
        boolean var1 = GoalUtils.mobRestricted(param0, param1);
        return getPosInDirection(param0, param1, param2, var0, var1);
    }

    @Nullable
    public static Vec3 getPosAway(PathfinderMob param0, int param1, int param2, Vec3 param3) {
        Vec3 var0 = param0.position().subtract(param3);
        boolean var1 = GoalUtils.mobRestricted(param0, param1);
        return getPosInDirection(param0, param1, param2, var0, var1);
    }

    @Nullable
    private static Vec3 getPosInDirection(PathfinderMob param0, int param1, int param2, Vec3 param3, boolean param4) {
        return RandomPos.generateRandomPos(param0, () -> {
            BlockPos var0x = RandomPos.generateRandomDirectionWithinRadians(param0.getRandom(), param1, param2, 0, param3.x, param3.z, (float) (Math.PI / 2));
            if (var0x == null) {
                return null;
            } else {
                BlockPos var1x = generateRandomPosTowardDirection(param0, param1, param4, var0x);
                return var1x == null ? null : movePosUpOutOfSolid(param0, var1x);
            }
        });
    }

    @Nullable
    public static BlockPos movePosUpOutOfSolid(PathfinderMob param0, BlockPos param1) {
        param1 = RandomPos.moveUpOutOfSolid(param1, param0.level().getMaxBuildHeight(), param1x -> GoalUtils.isSolid(param0, param1x));
        return !GoalUtils.isWater(param0, param1) && !GoalUtils.hasMalus(param0, param1) ? param1 : null;
    }

    @Nullable
    public static BlockPos generateRandomPosTowardDirection(PathfinderMob param0, int param1, boolean param2, BlockPos param3) {
        BlockPos var0 = RandomPos.generateRandomPosTowardDirection(param0, param1, param0.getRandom(), param3);
        return !GoalUtils.isOutsideLimits(var0, param0)
                && !GoalUtils.isRestricted(param2, param0, var0)
                && !GoalUtils.isNotStable(param0.getNavigation(), var0)
            ? var0
            : null;
    }
}
