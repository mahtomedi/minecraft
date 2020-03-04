package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public class KelpPlantBlock extends GrowingPlantBodyBlock implements LiquidBlockContainer {
    protected KelpPlantBlock(Block.Properties param0) {
        super(param0, Direction.UP, true);
    }

    @Override
    protected GrowingPlantHeadBlock getHeadBlock() {
        return (GrowingPlantHeadBlock)Blocks.KELP;
    }

    @Override
    public FluidState getFluidState(BlockState param0) {
        return Fluids.WATER.getSource(false);
    }

    @Override
    public boolean canPlaceLiquid(BlockGetter param0, BlockPos param1, BlockState param2, Fluid param3) {
        return false;
    }

    @Override
    public boolean placeLiquid(LevelAccessor param0, BlockPos param1, BlockState param2, FluidState param3) {
        return false;
    }
}
