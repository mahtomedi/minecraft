package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class LavaCauldronBlock extends AbstractCauldronBlock {
    public LavaCauldronBlock(BlockBehaviour.Properties param0) {
        super(param0, CauldronInteraction.LAVA);
    }

    @Override
    protected double getContentHeight(BlockState param0) {
        return 0.9375;
    }

    @Override
    public void entityInside(BlockState param0, Level param1, BlockPos param2, Entity param3) {
        if (this.isEntityInsideContent(param0, param2, param3)) {
            param3.lavaHurt();
        }

    }

    @Override
    public int getAnalogOutputSignal(BlockState param0, Level param1, BlockPos param2) {
        return 1;
    }
}
