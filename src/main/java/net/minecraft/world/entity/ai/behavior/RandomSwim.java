package net.minecraft.world.entity.ai.behavior;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.Vec3;

public class RandomSwim extends RandomStroll {
    public RandomSwim(float param0) {
        super(param0);
    }

    protected boolean checkExtraStartConditions(ServerLevel param0, PathfinderMob param1) {
        return param1.isInWaterOrBubble();
    }

    @Override
    protected Vec3 getTargetPos(PathfinderMob param0) {
        Vec3 var0 = BehaviorUtils.getRandomSwimmablePos(param0, this.maxHorizontalDistance, this.maxVerticalDistance);
        return var0 != null && param0.level.getFluidState(new BlockPos(var0)).isEmpty() ? null : var0;
    }
}
