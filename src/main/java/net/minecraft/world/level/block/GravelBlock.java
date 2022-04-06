package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class GravelBlock extends FallingBlock {
    public GravelBlock(BlockBehaviour.Properties param0) {
        super(param0);
    }

    @Override
    public int getDustColor(BlockState param0, BlockGetter param1, BlockPos param2) {
        return -8356741;
    }
}
