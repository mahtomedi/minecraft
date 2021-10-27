package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class GrowingPlantHeadBlock extends GrowingPlantBlock implements BonemealableBlock {
    public static final IntegerProperty AGE = BlockStateProperties.AGE_25;
    public static final int MAX_AGE = 25;
    private final double growPerTickProbability;

    protected GrowingPlantHeadBlock(BlockBehaviour.Properties param0, Direction param1, VoxelShape param2, boolean param3, double param4) {
        super(param0, param1, param2, param3);
        this.growPerTickProbability = param4;
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, Integer.valueOf(0)));
    }

    @Override
    public BlockState getStateForPlacement(LevelAccessor param0) {
        return this.defaultBlockState().setValue(AGE, Integer.valueOf(param0.getRandom().nextInt(25)));
    }

    @Override
    public boolean isRandomlyTicking(BlockState param0) {
        return param0.getValue(AGE) < 25;
    }

    @Override
    public void randomTick(BlockState param0, ServerLevel param1, BlockPos param2, Random param3) {
        if (param0.getValue(AGE) < 25 && param3.nextDouble() < this.growPerTickProbability) {
            BlockPos var0 = param2.relative(this.growthDirection);
            if (this.canGrowInto(param1.getBlockState(var0))) {
                param1.setBlockAndUpdate(var0, this.getGrowIntoState(param0, param1.random));
            }
        }

    }

    protected BlockState getGrowIntoState(BlockState param0, Random param1) {
        return param0.cycle(AGE);
    }

    public BlockState getMaxAgeState(BlockState param0) {
        return param0.setValue(AGE, Integer.valueOf(25));
    }

    public boolean isMaxAge(BlockState param0) {
        return param0.getValue(AGE) == 25;
    }

    protected BlockState updateBodyAfterConvertedFromHead(BlockState param0, BlockState param1) {
        return param1;
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        if (param1 == this.growthDirection.getOpposite() && !param0.canSurvive(param3, param4)) {
            param3.scheduleTick(param4, this, 1);
        }

        if (param1 != this.growthDirection || !param2.is(this) && !param2.is(this.getBodyBlock())) {
            if (this.scheduleFluidTicks) {
                param3.scheduleTick(param4, Fluids.WATER, Fluids.WATER.getTickDelay(param3));
            }

            return super.updateShape(param0, param1, param2, param3, param4, param5);
        } else {
            return this.updateBodyAfterConvertedFromHead(param0, this.getBodyBlock().defaultBlockState());
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(AGE);
    }

    @Override
    public boolean isValidBonemealTarget(BlockGetter param0, BlockPos param1, BlockState param2, boolean param3) {
        return this.canGrowInto(param0.getBlockState(param1.relative(this.growthDirection)));
    }

    @Override
    public boolean isBonemealSuccess(Level param0, Random param1, BlockPos param2, BlockState param3) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel param0, Random param1, BlockPos param2, BlockState param3) {
        BlockPos var0 = param2.relative(this.growthDirection);
        int var1 = Math.min(param3.getValue(AGE) + 1, 25);
        int var2 = this.getBlocksToGrowWhenBonemealed(param1);

        for(int var3 = 0; var3 < var2 && this.canGrowInto(param0.getBlockState(var0)); ++var3) {
            param0.setBlockAndUpdate(var0, param3.setValue(AGE, Integer.valueOf(var1)));
            var0 = var0.relative(this.growthDirection);
            var1 = Math.min(var1 + 1, 25);
        }

    }

    protected abstract int getBlocksToGrowWhenBonemealed(Random var1);

    protected abstract boolean canGrowInto(BlockState var1);

    @Override
    protected GrowingPlantHeadBlock getHeadBlock() {
        return this;
    }
}
