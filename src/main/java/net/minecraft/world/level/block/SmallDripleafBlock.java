package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SmallDripleafBlock extends DoublePlantBlock implements BonemealableBlock, SimpleWaterloggedBlock {
    private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    protected static final float AABB_OFFSET = 6.0F;
    protected static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 13.0, 14.0);

    public SmallDripleafBlock(BlockBehaviour.Properties param0) {
        super(param0);
        this.registerDefaultState(
            this.stateDefinition.any().setValue(HALF, DoubleBlockHalf.LOWER).setValue(WATERLOGGED, Boolean.valueOf(false)).setValue(FACING, Direction.NORTH)
        );
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return SHAPE;
    }

    @Override
    protected boolean mayPlaceOn(BlockState param0, BlockGetter param1, BlockPos param2) {
        return param0.is(BlockTags.SMALL_DRIPLEAF_PLACEABLE)
            || param1.getFluidState(param2.above()).isSourceOfType(Fluids.WATER) && super.mayPlaceOn(param0, param1, param2);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        BlockState var0 = super.getStateForPlacement(param0);
        return var0 != null
            ? copyWaterloggedFrom(param0.getLevel(), param0.getClickedPos(), var0.setValue(FACING, param0.getHorizontalDirection().getOpposite()))
            : null;
    }

    @Override
    public void setPlacedBy(Level param0, BlockPos param1, BlockState param2, LivingEntity param3, ItemStack param4) {
        if (!param0.isClientSide()) {
            BlockPos var0 = param1.above();
            BlockState var1 = DoublePlantBlock.copyWaterloggedFrom(
                param0, var0, this.defaultBlockState().setValue(HALF, DoubleBlockHalf.UPPER).setValue(FACING, param2.getValue(FACING))
            );
            param0.setBlock(var0, var1, 3);
        }

    }

    @Override
    public FluidState getFluidState(BlockState param0) {
        return param0.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(param0);
    }

    @Override
    public boolean canSurvive(BlockState param0, LevelReader param1, BlockPos param2) {
        if (param0.getValue(HALF) == DoubleBlockHalf.UPPER) {
            return super.canSurvive(param0, param1, param2);
        } else {
            BlockPos var0 = param2.below();
            BlockState var1 = param1.getBlockState(var0);
            return this.mayPlaceOn(var1, param1, var0);
        }
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        if (param0.getValue(WATERLOGGED)) {
            param3.scheduleTick(param4, Fluids.WATER, Fluids.WATER.getTickDelay(param3));
        }

        return super.updateShape(param0, param1, param2, param3, param4, param5);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(HALF, WATERLOGGED, FACING);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader param0, BlockPos param1, BlockState param2, boolean param3) {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(Level param0, RandomSource param1, BlockPos param2, BlockState param3) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel param0, RandomSource param1, BlockPos param2, BlockState param3) {
        if (param3.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.LOWER) {
            BlockPos var0 = param2.above();
            param0.setBlock(var0, param0.getFluidState(var0).createLegacyBlock(), 18);
            BigDripleafBlock.placeWithRandomHeight(param0, param1, param2, param3.getValue(FACING));
        } else {
            BlockPos var1 = param2.below();
            this.performBonemeal(param0, param1, var1, param0.getBlockState(var1));
        }

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
    public float getMaxVerticalOffset() {
        return 0.1F;
    }
}
