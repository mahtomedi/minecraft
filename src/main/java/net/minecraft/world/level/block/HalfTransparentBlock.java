package net.minecraft.world.level.block;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class HalfTransparentBlock extends Block {
    protected HalfTransparentBlock(Block.Properties param0) {
        super(param0);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean skipRendering(BlockState param0, BlockState param1, Direction param2) {
        return param1.getBlock() == this ? true : super.skipRendering(param0, param1, param2);
    }
}
