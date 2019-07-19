package net.minecraft.world.level.block;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SandBlock extends FallingBlock {
    private final int dustColor;

    public SandBlock(int param0, Block.Properties param1) {
        super(param1);
        this.dustColor = param0;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public int getDustColor(BlockState param0) {
        return this.dustColor;
    }
}
