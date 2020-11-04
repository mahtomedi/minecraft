package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class WaterCauldronBlock extends AbstractCauldronBlock {
    public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL_CAULDRON;

    public WaterCauldronBlock(BlockBehaviour.Properties param0) {
        super(param0, CauldronInteraction.WATER);
        this.registerDefaultState(this.stateDefinition.any().setValue(LEVEL, Integer.valueOf(1)));
    }

    @Override
    protected double getContentHeight(BlockState param0) {
        return (double)(6 + param0.getValue(LEVEL) * 3) / 16.0;
    }

    @Override
    public void entityInside(BlockState param0, Level param1, BlockPos param2, Entity param3) {
        if (!param1.isClientSide && param3.isOnFire() && this.isEntityInsideContent(param0, param2, param3)) {
            param3.clearFire();
            lowerWaterLevel(param0, param1, param2);
        }

    }

    public static void lowerWaterLevel(BlockState param0, Level param1, BlockPos param2) {
        int var0 = param0.getValue(LEVEL) - 1;
        param1.setBlockAndUpdate(param2, var0 == 0 ? Blocks.CAULDRON.defaultBlockState() : param0.setValue(LEVEL, Integer.valueOf(var0)));
    }

    @Override
    public void handleRain(BlockState param0, Level param1, BlockPos param2) {
        if (CauldronBlock.shouldHandleRain(param1, param2) && param0.getValue(LEVEL) != 3) {
            param1.setBlockAndUpdate(param2, param0.cycle(LEVEL));
        }
    }

    @Override
    public int getAnalogOutputSignal(BlockState param0, Level param1, BlockPos param2) {
        return param0.getValue(LEVEL);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(LEVEL);
    }
}
