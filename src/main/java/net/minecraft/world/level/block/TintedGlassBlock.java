package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class TintedGlassBlock extends AbstractGlassBlock {
    public TintedGlassBlock(BlockBehaviour.Properties param0) {
        super(param0);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState param0, BlockGetter param1, BlockPos param2) {
        return false;
    }

    @Override
    public int getLightBlock(BlockState param0, BlockGetter param1, BlockPos param2) {
        return param1.getMaxLightLevel();
    }
}
