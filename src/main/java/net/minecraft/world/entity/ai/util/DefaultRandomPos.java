package net.minecraft.world.entity.ai.util;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.Vec3;

public class DefaultRandomPos {
    @Nullable
    public static Vec3 getPos(PathfinderMob param0, int param1, int param2) {
        boolean var0 = GoalUtils.mobRestricted(param0, param1);
        return RandomPos.generateRandomPos(param0, () -> {
            BlockPos var0x = RandomPos.generateRandomDirection(param0.getRandom(), param1, param2);
            return generateRandomPosTowardDirection(param0, param1, var0, var0x);
        });
    }

    @Nullable
    public static Vec3 getPosTowards(PathfinderMob param0, int param1, int param2, Vec3 param3, double param4) {
        Vec3 var0 = param3.subtract(param0.getX(), param0.getY(), param0.getZ());
        boolean var1 = GoalUtils.mobRestricted(param0, param1);
        return RandomPos.generateRandomPos(param0, () -> {
            BlockPos var0x = RandomPos.generateRandomDirectionWithinRadians(param0.getRandom(), param1, param2, 0, var0.x, var0.z, param4);
            return var0x == null ? null : generateRandomPosTowardDirection(param0, param1, var1, var0x);
        });
    }

    @Nullable
    public static Vec3 getPosAway(PathfinderMob param0, int param1, int param2, Vec3 param3) {
        Vec3 var0 = param0.position().subtract(param3);
        boolean var1 = GoalUtils.mobRestricted(param0, param1);
        return RandomPos.generateRandomPos(param0, () -> {
            BlockPos var0x = RandomPos.generateRandomDirectionWithinRadians(param0.getRandom(), param1, param2, 0, var0.x, var0.z, (float) (Math.PI / 2));
            return var0x == null ? null : generateRandomPosTowardDirection(param0, param1, var1, var0x);
        });
    }

    @Nullable
    private static BlockPos generateRandomPosTowardDirection(PathfinderMob param0, int param1, boolean param2, BlockPos param3) {
        BlockPos var0 = RandomPos.generateRandomPosTowardDirection(param0, param1, param0.getRandom(), param3);
        return !GoalUtils.isOutsideLimits(var0, param0)
                && !GoalUtils.isRestricted(param2, param0, var0)
                && !GoalUtils.isNotStable(param0.getNavigation(), var0)
                && !GoalUtils.hasMalus(param0, var0)
            ? var0
            : null;
    }
}
