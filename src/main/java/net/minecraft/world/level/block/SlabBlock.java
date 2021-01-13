package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SlabBlock extends Block implements SimpleWaterloggedBlock {
    public static final EnumProperty<SlabType> TYPE = BlockStateProperties.SLAB_TYPE;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    protected static final VoxelShape BOTTOM_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0);
    protected static final VoxelShape TOP_AABB = Block.box(0.0, 8.0, 0.0, 16.0, 16.0, 16.0);

    public SlabBlock(BlockBehaviour.Properties param0) {
        super(param0);
        this.registerDefaultState(this.defaultBlockState().setValue(TYPE, SlabType.BOTTOM).setValue(WATERLOGGED, Boolean.valueOf(false)));
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState param0) {
        return param0.getValue(TYPE) != SlabType.DOUBLE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(TYPE, WATERLOGGED);
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        SlabType var0 = param0.getValue(TYPE);
        switch(var0) {
            case DOUBLE:
                return Shapes.block();
            case TOP:
                return TOP_AABB;
            default:
                return BOTTOM_AABB;
        }
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        BlockPos var0 = param0.getClickedPos();
        BlockState var1 = param0.getLevel().getBlockState(var0);
        if (var1.is(this)) {
            return var1.setValue(TYPE, SlabType.DOUBLE).setValue(WATERLOGGED, Boolean.valueOf(false));
        } else {
            FluidState var2 = param0.getLevel().getFluidState(var0);
            BlockState var3 = this.defaultBlockState().setValue(TYPE, SlabType.BOTTOM).setValue(WATERLOGGED, Boolean.valueOf(var2.getType() == Fluids.WATER));
            Direction var4 = param0.getClickedFace();
            return var4 != Direction.DOWN && (var4 == Direction.UP || !(param0.getClickLocation().y - (double)var0.getY() > 0.5))
                ? var3
                : var3.setValue(TYPE, SlabType.TOP);
        }
    }

    @Override
    public boolean canBeReplaced(BlockState param0, BlockPlaceContext param1) {
        ItemStack var0 = param1.getItemInHand();
        SlabType var1 = param0.getValue(TYPE);
        if (var1 == SlabType.DOUBLE || var0.getItem() != this.asItem()) {
            return false;
        } else if (param1.replacingClickedOnBlock()) {
            boolean var2 = param1.getClickLocation().y - (double)param1.getClickedPos().getY() > 0.5;
            Direction var3 = param1.getClickedFace();
            if (var1 == SlabType.BOTTOM) {
                return var3 == Direction.UP || var2 && var3.getAxis().isHorizontal();
            } else {
                return var3 == Direction.DOWN || !var2 && var3.getAxis().isHorizontal();
            }
        } else {
            return true;
        }
    }

    @Override
    public FluidState getFluidState(BlockState param0) {
        return param0.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(param0);
    }

    @Override
    public boolean placeLiquid(LevelAccessor param0, BlockPos param1, BlockState param2, FluidState param3) {
        return param2.getValue(TYPE) != SlabType.DOUBLE ? SimpleWaterloggedBlock.super.placeLiquid(param0, param1, param2, param3) : false;
    }

    @Override
    public boolean canPlaceLiquid(BlockGetter param0, BlockPos param1, BlockState param2, Fluid param3) {
        return param2.getValue(TYPE) != SlabType.DOUBLE ? SimpleWaterloggedBlock.super.canPlaceLiquid(param0, param1, param2, param3) : false;
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        if (param0.getValue(WATERLOGGED)) {
            param3.getLiquidTicks().scheduleTick(param4, Fluids.WATER, Fluids.WATER.getTickDelay(param3));
        }

        return super.updateShape(param0, param1, param2, param3, param4, param5);
    }

    @Override
    public boolean isPathfindable(BlockState param0, BlockGetter param1, BlockPos param2, PathComputationType param3) {
        switch(param3) {
            case LAND:
                return false;
            case WATER:
                return param1.getFluidState(param2).is(FluidTags.WATER);
            case AIR:
                return false;
            default:
                return false;
        }
    }
}
