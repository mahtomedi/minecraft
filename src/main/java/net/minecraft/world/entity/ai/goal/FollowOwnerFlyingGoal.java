package net.minecraft.world.entity.ai.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.block.state.BlockState;

public class FollowOwnerFlyingGoal extends FollowOwnerGoal {
    public FollowOwnerFlyingGoal(TamableAnimal param0, double param1, float param2, float param3) {
        super(param0, param1, param2, param3);
    }

    @Override
    protected boolean isTeleportFriendlyBlock(BlockPos param0) {
        BlockState var0 = this.level.getBlockState(param0);
        return (var0.entityCanStandOn(this.level, param0, this.tamable) || var0.is(BlockTags.LEAVES))
            && this.level.isEmptyBlock(param0.above())
            && this.level.isEmptyBlock(param0.above(2));
    }
}
