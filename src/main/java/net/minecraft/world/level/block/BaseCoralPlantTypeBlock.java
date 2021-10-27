package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
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

public class BaseCoralPlantTypeBlock extends Block implements SimpleWaterloggedBlock {
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private static final VoxelShape AABB = Block.box(2.0, 0.0, 2.0, 14.0, 4.0, 14.0);

    protected BaseCoralPlantTypeBlock(BlockBehaviour.Properties param0) {
        super(param0);
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, Boolean.valueOf(true)));
    }

    protected void tryScheduleDieTick(BlockState param0, LevelAccessor param1, BlockPos param2) {
        if (!scanForWater(param0, param1, param2)) {
            param1.scheduleTick(param2, this, 60 + param1.getRandom().nextInt(40));
        }

    }

    protected static boolean scanForWater(BlockState param0, BlockGetter param1, BlockPos param2) {
        if (param0.getValue(WATERLOGGED)) {
            return true;
        } else {
            for(Direction var0 : Direction.values()) {
                if (param1.getFluidState(param2.relative(var0)).is(FluidTags.WATER)) {
                    return true;
                }
            }

            return false;
        }
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        FluidState var0 = param0.getLevel().getFluidState(param0.getClickedPos());
        return this.defaultBlockState().setValue(WATERLOGGED, Boolean.valueOf(var0.is(FluidTags.WATER) && var0.getAmount() == 8));
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return AABB;
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        if (param0.getValue(WATERLOGGED)) {
            param3.scheduleTick(param4, Fluids.WATER, Fluids.WATER.getTickDelay(param3));
        }

        return param1 == Direction.DOWN && !this.canSurvive(param0, param3, param4)
            ? Blocks.AIR.defaultBlockState()
            : super.updateShape(param0, param1, param2, param3, param4, param5);
    }

    @Override
    public boolean canSurvive(BlockState param0, LevelReader param1, BlockPos param2) {
        BlockPos var0 = param2.below();
        return param1.getBlockState(var0).isFaceSturdy(param1, var0, Direction.UP);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(WATERLOGGED);
    }

    @Override
    public FluidState getFluidState(BlockState param0) {
        return param0.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(param0);
    }
}
