package net.minecraft.world.entity.ai.behavior;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.Vec3;

public class RandomSwim extends RandomStroll {
    public static final int[][] XY_DISTANCE_TIERS = new int[][]{{1, 1}, {3, 3}, {5, 5}, {6, 5}, {7, 7}, {10, 7}};

    public RandomSwim(float param0) {
        super(param0);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel param0, PathfinderMob param1) {
        return param1.isInWaterOrBubble();
    }

    @Nullable
    @Override
    protected Vec3 getTargetPos(PathfinderMob param0) {
        Vec3 var0 = null;
        Vec3 var1 = null;

        for(int[] var2 : XY_DISTANCE_TIERS) {
            if (var0 == null) {
                var1 = BehaviorUtils.getRandomSwimmablePos(param0, var2[0], var2[1]);
            } else {
                var1 = param0.position().add(param0.position().vectorTo(var0).normalize().multiply((double)var2[0], (double)var2[1], (double)var2[0]));
            }

            if (var1 == null || param0.level.getFluidState(new BlockPos(var1)).isEmpty()) {
                return var0;
            }

            var0 = var1;
        }

        return var1;
    }
}
