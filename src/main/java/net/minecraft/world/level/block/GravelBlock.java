package net.minecraft.world.level.block;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class GravelBlock extends FallingBlock {
    public GravelBlock(Block.Properties param0) {
        super(param0);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public int getDustColor(BlockState param0) {
        return -8356741;
    }
}
