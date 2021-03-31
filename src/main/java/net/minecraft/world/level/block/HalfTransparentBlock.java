package net.minecraft.world.level.block;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class HalfTransparentBlock extends Block {
    protected HalfTransparentBlock(BlockBehaviour.Properties param0) {
        super(param0);
    }

    @Override
    public boolean skipRendering(BlockState param0, BlockState param1, Direction param2) {
        return param1.is(this) ? true : super.skipRendering(param0, param1, param2);
    }
}
