package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class SoulFireBlock extends BaseFireBlock {
    public SoulFireBlock(BlockBehaviour.Properties param0) {
        super(param0, 2.0F);
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        return this.canSurvive(param0, param3, param4) ? this.defaultBlockState() : Blocks.AIR.defaultBlockState();
    }

    @Override
    public boolean canSurvive(BlockState param0, LevelReader param1, BlockPos param2) {
        return canSurviveOnBlock(param1.getBlockState(param2.below()).getBlock());
    }

    public static boolean canSurviveOnBlock(Block param0) {
        return param0.is(BlockTags.SOUL_FIRE_BASE_BLOCKS);
    }

    @Override
    protected boolean canBurn(BlockState param0) {
        return true;
    }
}
