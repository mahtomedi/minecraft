package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
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

public class BigDripleafStemBlock extends HorizontalDirectionalBlock implements SimpleWaterloggedBlock {
    private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private static final VoxelShape NORTH_SHAPE = Block.box(5.0, 0.0, 8.0, 11.0, 16.0, 14.0);
    private static final VoxelShape SOUTH_SHAPE = Block.box(5.0, 0.0, 2.0, 11.0, 16.0, 8.0);
    private static final VoxelShape EAST_SHAPE = Block.box(2.0, 0.0, 5.0, 8.0, 16.0, 11.0);
    private static final VoxelShape WEST_SHAPE = Block.box(8.0, 0.0, 5.0, 14.0, 16.0, 11.0);

    protected BigDripleafStemBlock(BlockBehaviour.Properties param0) {
        super(param0);
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, Boolean.valueOf(false)).setValue(FACING, Direction.NORTH));
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        switch((Direction)param0.getValue(FACING)) {
            case SOUTH:
                return SOUTH_SHAPE;
            case NORTH:
            default:
                return NORTH_SHAPE;
            case WEST:
                return WEST_SHAPE;
            case EAST:
                return EAST_SHAPE;
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(WATERLOGGED, FACING);
    }

    @Override
    public FluidState getFluidState(BlockState param0) {
        return param0.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(param0);
    }

    @Override
    public boolean canSurvive(BlockState param0, LevelReader param1, BlockPos param2) {
        BlockPos var0 = param2.below();
        BlockState var1 = param1.getBlockState(var0);
        return var1.is(this) || var1.isFaceSturdy(param1, var0, Direction.UP);
    }

    protected static boolean place(LevelAccessor param0, BlockPos param1, FluidState param2, Direction param3) {
        BlockState var0 = Blocks.BIG_DRIPLEAF_STEM
            .defaultBlockState()
            .setValue(WATERLOGGED, Boolean.valueOf(param2.isSourceOfType(Fluids.WATER)))
            .setValue(FACING, param3);
        return param0.setBlock(param1, var0, 2);
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        if (param1 == Direction.DOWN && !param0.canSurvive(param3, param4)) {
            param3.getBlockTicks().scheduleTick(param4, this, 1);
        }

        if (param0.getValue(WATERLOGGED)) {
            param3.getLiquidTicks().scheduleTick(param4, Fluids.WATER, Fluids.WATER.getTickDelay(param3));
        }

        return super.updateShape(param0, param1, param2, param3, param4, param5);
    }

    @Override
    public void tick(BlockState param0, ServerLevel param1, BlockPos param2, Random param3) {
        if (!param0.canSurvive(param1, param2)) {
            param1.destroyBlock(param2, true);
        }

    }
}
