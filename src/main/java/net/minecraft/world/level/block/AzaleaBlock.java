package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class AzaleaBlock extends BushBlock {
    protected AzaleaBlock(BlockBehaviour.Properties param0) {
        super(param0);
    }

    @Override
    protected boolean mayPlaceOn(BlockState param0, BlockGetter param1, BlockPos param2) {
        return param0.is(Blocks.CLAY) || super.mayPlaceOn(param0, param1, param2);
    }
}
