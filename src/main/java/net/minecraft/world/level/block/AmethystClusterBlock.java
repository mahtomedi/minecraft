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
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class AmethystClusterBlock extends AmethystBlock implements SimpleWaterloggedBlock {
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    protected final VoxelShape northAabb;
    protected final VoxelShape southAabb;
    protected final VoxelShape eastAabb;
    protected final VoxelShape westAabb;
    protected final VoxelShape upAabb;
    protected final VoxelShape downAabb;

    public AmethystClusterBlock(int param0, int param1, BlockBehaviour.Properties param2) {
        super(param2);
        this.registerDefaultState(this.defaultBlockState().setValue(WATERLOGGED, Boolean.valueOf(false)).setValue(FACING, Direction.UP));
        this.upAabb = Block.box((double)param1, 0.0, (double)param1, (double)(16 - param1), (double)param0, (double)(16 - param1));
        this.downAabb = Block.box((double)param1, (double)(16 - param0), (double)param1, (double)(16 - param1), 16.0, (double)(16 - param1));
        this.northAabb = Block.box((double)param1, (double)param1, (double)(16 - param0), (double)(16 - param1), (double)(16 - param1), 16.0);
        this.southAabb = Block.box((double)param1, (double)param1, 0.0, (double)(16 - param1), (double)(16 - param1), (double)param0);
        this.eastAabb = Block.box(0.0, (double)param1, (double)param1, (double)param0, (double)(16 - param1), (double)(16 - param1));
        this.westAabb = Block.box((double)(16 - param0), (double)param1, (double)param1, 16.0, (double)(16 - param1), (double)(16 - param1));
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        Direction var0 = param0.getValue(FACING);
        switch(var0) {
            case NORTH:
                return this.northAabb;
            case SOUTH:
                return this.southAabb;
            case EAST:
                return this.eastAabb;
            case WEST:
                return this.westAabb;
            case DOWN:
                return this.downAabb;
            case UP:
            default:
                return this.upAabb;
        }
    }

    @Override
    public boolean canSurvive(BlockState param0, LevelReader param1, BlockPos param2) {
        Direction var0 = param0.getValue(FACING);
        BlockPos var1 = param2.relative(var0.getOpposite());
        return param1.getBlockState(var1).isFaceSturdy(param1, var1, var0);
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        if (param0.getValue(WATERLOGGED)) {
            param3.scheduleTick(param4, Fluids.WATER, Fluids.WATER.getTickDelay(param3));
        }

        return param1 == param0.getValue(FACING).getOpposite() && !param0.canSurvive(param3, param4)
            ? Blocks.AIR.defaultBlockState()
            : super.updateShape(param0, param1, param2, param3, param4, param5);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        LevelAccessor var0 = param0.getLevel();
        BlockPos var1 = param0.getClickedPos();
        return this.defaultBlockState()
            .setValue(WATERLOGGED, Boolean.valueOf(var0.getFluidState(var1).getType() == Fluids.WATER))
            .setValue(FACING, param0.getClickedFace());
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
    public FluidState getFluidState(BlockState param0) {
        return param0.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(param0);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(WATERLOGGED, FACING);
    }
}
