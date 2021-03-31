package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class SandBlock extends FallingBlock {
    private final int dustColor;

    public SandBlock(int param0, BlockBehaviour.Properties param1) {
        super(param1);
        this.dustColor = param0;
    }

    @Override
    public int getDustColor(BlockState param0, BlockGetter param1, BlockPos param2) {
        return this.dustColor;
    }
}
