package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Fluids;

public abstract class GrowingPlantHeadBlock extends GrowingPlantBlock {
    public static final IntegerProperty AGE = BlockStateProperties.AGE_25;
    private final double growPerTickProbability;

    protected GrowingPlantHeadBlock(Block.Properties param0, Direction param1, boolean param2, double param3) {
        super(param0, param1, param2);
        this.growPerTickProbability = param3;
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, Integer.valueOf(0)));
    }

    public BlockState getStateForPlacement(LevelAccessor param0) {
        return this.defaultBlockState().setValue(AGE, Integer.valueOf(param0.getRandom().nextInt(25)));
    }

    @Override
    public void tick(BlockState param0, ServerLevel param1, BlockPos param2, Random param3) {
        if (!param0.canSurvive(param1, param2)) {
            param1.destroyBlock(param2, true);
        } else {
            if (param0.getValue(AGE) < 25 && param3.nextDouble() < this.growPerTickProbability) {
                BlockPos var0 = param2.relative(this.growthDirection);
                if (this.canGrowInto(param1.getBlockState(var0))) {
                    param1.setBlockAndUpdate(var0, param0.cycle(AGE));
                }
            }

        }
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        if (param1 == this.growthDirection.getOpposite() && !param0.canSurvive(param3, param4)) {
            param3.getBlockTicks().scheduleTick(param4, this, 1);
        }

        if (param1 == this.growthDirection && param2.getBlock() == this) {
            return this.getBodyBlock().defaultBlockState();
        } else {
            if (this.scheduleFluidTicks) {
                param3.getLiquidTicks().scheduleTick(param4, Fluids.WATER, Fluids.WATER.getTickDelay(param3));
            }

            return super.updateShape(param0, param1, param2, param3, param4, param5);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(AGE);
    }

    protected abstract boolean canGrowInto(BlockState var1);

    @Override
    protected GrowingPlantHeadBlock getHeadBlock() {
        return this;
    }
}
