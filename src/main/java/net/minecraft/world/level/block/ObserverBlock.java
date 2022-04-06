package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class ObserverBlock extends DirectionalBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public ObserverBlock(BlockBehaviour.Properties param0) {
        super(param0);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.SOUTH).setValue(POWERED, Boolean.valueOf(false)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(FACING, POWERED);
    }

    @Override
    public BlockState rotate(BlockState param0, Rotation param1) {
        return param0.setValue(FACING, param1.rotate(param0.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState param0, Mirror param1) {
        return param0.rotate(param1.getRotation(param0.getValue(FACING)));
    }

    @Override
    public void tick(BlockState param0, ServerLevel param1, BlockPos param2, RandomSource param3) {
        if (param0.getValue(POWERED)) {
            param1.setBlock(param2, param0.setValue(POWERED, Boolean.valueOf(false)), 2);
        } else {
            param1.setBlock(param2, param0.setValue(POWERED, Boolean.valueOf(true)), 2);
            param1.scheduleTick(param2, this, 2);
        }

        this.updateNeighborsInFront(param1, param2, param0);
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        if (param0.getValue(FACING) == param1 && !param0.getValue(POWERED)) {
            this.startSignal(param3, param4);
        }

        return super.updateShape(param0, param1, param2, param3, param4, param5);
    }

    private void startSignal(LevelAccessor param0, BlockPos param1) {
        if (!param0.isClientSide() && !param0.getBlockTicks().hasScheduledTick(param1, this)) {
            param0.scheduleTick(param1, this, 2);
        }

    }

    protected void updateNeighborsInFront(Level param0, BlockPos param1, BlockState param2) {
        Direction var0 = param2.getValue(FACING);
        BlockPos var1 = param1.relative(var0.getOpposite());
        param0.neighborChanged(var1, this, param1);
        param0.updateNeighborsAtExceptFromFacing(var1, this, var0);
    }

    @Override
    public boolean isSignalSource(BlockState param0) {
        return true;
    }

    @Override
    public int getDirectSignal(BlockState param0, BlockGetter param1, BlockPos param2, Direction param3) {
        return param0.getSignal(param1, param2, param3);
    }

    @Override
    public int getSignal(BlockState param0, BlockGetter param1, BlockPos param2, Direction param3) {
        return param0.getValue(POWERED) && param0.getValue(FACING) == param3 ? 15 : 0;
    }

    @Override
    public void onPlace(BlockState param0, Level param1, BlockPos param2, BlockState param3, boolean param4) {
        if (!param0.is(param3.getBlock())) {
            if (!param1.isClientSide() && param0.getValue(POWERED) && !param1.getBlockTicks().hasScheduledTick(param2, this)) {
                BlockState var0 = param0.setValue(POWERED, Boolean.valueOf(false));
                param1.setBlock(param2, var0, 18);
                this.updateNeighborsInFront(param1, param2, var0);
            }

        }
    }

    @Override
    public void onRemove(BlockState param0, Level param1, BlockPos param2, BlockState param3, boolean param4) {
        if (!param0.is(param3.getBlock())) {
            if (!param1.isClientSide && param0.getValue(POWERED) && param1.getBlockTicks().hasScheduledTick(param2, this)) {
                this.updateNeighborsInFront(param1, param2, param0.setValue(POWERED, Boolean.valueOf(false)));
            }

        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        return this.defaultBlockState().setValue(FACING, param0.getNearestLookingDirection().getOpposite().getOpposite());
    }
}
