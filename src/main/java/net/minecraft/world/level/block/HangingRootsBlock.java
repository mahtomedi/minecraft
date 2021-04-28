package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class HangingRootsBlock extends Block implements SimpleWaterloggedBlock {
    private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    protected static final VoxelShape SHAPE = Block.box(4.0, 9.0, 4.0, 12.0, 16.0, 12.0);

    protected HangingRootsBlock(BlockBehaviour.Properties param0) {
        super(param0);
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, Boolean.valueOf(false)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(WATERLOGGED);
    }

    @Override
    public FluidState getFluidState(BlockState param0) {
        return param0.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(param0);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        BlockState var0 = super.getStateForPlacement(param0);
        if (var0 != null) {
            FluidState var1 = param0.getLevel().getFluidState(param0.getClickedPos());
            return var0.setValue(WATERLOGGED, Boolean.valueOf(var1.getType() == Fluids.WATER));
        } else {
            return null;
        }
    }

    @Override
    public boolean canSurvive(BlockState param0, LevelReader param1, BlockPos param2) {
        BlockPos var0 = param2.above();
        BlockState var1 = param1.getBlockState(var0);
        return var1.isFaceSturdy(param1, var0, Direction.DOWN);
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return SHAPE;
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        if (param1 == Direction.UP && !this.canSurvive(param0, param3, param4)) {
            return Blocks.AIR.defaultBlockState();
        } else {
            if (param0.getValue(WATERLOGGED)) {
                param3.getLiquidTicks().scheduleTick(param4, Fluids.WATER, Fluids.WATER.getTickDelay(param3));
            }

            return super.updateShape(param0, param1, param2, param3, param4, param5);
        }
    }

    @Override
    public BlockBehaviour.OffsetType getOffsetType() {
        return BlockBehaviour.OffsetType.XZ;
    }
}
