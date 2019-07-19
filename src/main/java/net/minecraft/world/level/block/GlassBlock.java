package net.minecraft.world.level.block;

import net.minecraft.world.level.BlockLayer;

public class GlassBlock extends AbstractGlassBlock {
    public GlassBlock(Block.Properties param0) {
        super(param0);
    }

    @Override
    public BlockLayer getRenderLayer() {
        return BlockLayer.CUTOUT;
    }
}
