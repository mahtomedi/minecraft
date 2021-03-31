package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class BarrierBlock extends Block {
    protected BarrierBlock(BlockBehaviour.Properties param0) {
        super(param0);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState param0, BlockGetter param1, BlockPos param2) {
        return true;
    }

    @Override
    public RenderShape getRenderShape(BlockState param0) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public float getShadeBrightness(BlockState param0, BlockGetter param1, BlockPos param2) {
        return 1.0F;
    }
}
