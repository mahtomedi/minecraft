package net.minecraft.world.entity.ai.util;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.Vec3;

public class HoverRandomPos {
    @Nullable
    public static Vec3 getPos(PathfinderMob param0, int param1, int param2, double param3, double param4, float param5, int param6, int param7) {
        boolean var0 = GoalUtils.mobRestricted(param0, param1);
        return RandomPos.generateRandomPos(
            param0,
            () -> {
                BlockPos var0x = RandomPos.generateRandomDirectionWithinRadians(param0.getRandom(), param1, param2, 0, param3, param4, (double)param5);
                if (var0x == null) {
                    return null;
                } else {
                    BlockPos var1x = LandRandomPos.generateRandomPosTowardDirection(param0, param1, var0, var0x);
                    if (var1x == null) {
                        return null;
                    } else {
                        var1x = RandomPos.moveUpToAboveSolid(
                            var1x,
                            param0.getRandom().nextInt(param6 - param7 + 1) + param7,
                            param0.level.getMaxBuildHeight(),
                            param1x -> GoalUtils.isSolid(param0, param1x)
                        );
                        return !GoalUtils.isWater(param0, var1x) && !GoalUtils.hasMalus(param0, var1x) ? var1x : null;
                    }
                }
            }
        );
    }
}
