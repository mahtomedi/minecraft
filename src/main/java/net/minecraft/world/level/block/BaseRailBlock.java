package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class BaseRailBlock extends Block implements SimpleWaterloggedBlock {
    protected static final VoxelShape FLAT_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0);
    protected static final VoxelShape HALF_BLOCK_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0);
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private final boolean isStraight;

    public static boolean isRail(Level param0, BlockPos param1) {
        return isRail(param0.getBlockState(param1));
    }

    public static boolean isRail(BlockState param0) {
        return param0.is(BlockTags.RAILS) && param0.getBlock() instanceof BaseRailBlock;
    }

    protected BaseRailBlock(boolean param0, BlockBehaviour.Properties param1) {
        super(param1);
        this.isStraight = param0;
    }

    public boolean isStraight() {
        return this.isStraight;
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        RailShape var0 = param0.is(this) ? param0.getValue(this.getShapeProperty()) : null;
        return var0 != null && var0.isAscending() ? HALF_BLOCK_AABB : FLAT_AABB;
    }

    @Override
    public boolean canSurvive(BlockState param0, LevelReader param1, BlockPos param2) {
        return canSupportRigidBlock(param1, param2.below());
    }

    @Override
    public void onPlace(BlockState param0, Level param1, BlockPos param2, BlockState param3, boolean param4) {
        if (!param3.is(param0.getBlock())) {
            this.updateState(param0, param1, param2, param4);
        }
    }

    protected BlockState updateState(BlockState param0, Level param1, BlockPos param2, boolean param3) {
        param0 = this.updateDir(param1, param2, param0, true);
        if (this.isStraight) {
            param0.neighborChanged(param1, param2, this, param2, param3);
        }

        return param0;
    }

    @Override
    public void neighborChanged(BlockState param0, Level param1, BlockPos param2, Block param3, BlockPos param4, boolean param5) {
        if (!param1.isClientSide && param1.getBlockState(param2).is(this)) {
            RailShape var0 = param0.getValue(this.getShapeProperty());
            if (shouldBeRemoved(param2, param1, var0)) {
                dropResources(param0, param1, param2);
                param1.removeBlock(param2, param5);
            } else {
                this.updateState(param0, param1, param2, param3);
            }

        }
    }

    private static boolean shouldBeRemoved(BlockPos param0, Level param1, RailShape param2) {
        if (!canSupportRigidBlock(param1, param0.below())) {
            return true;
        } else {
            switch(param2) {
                case ASCENDING_EAST:
                    return !canSupportRigidBlock(param1, param0.east());
                case ASCENDING_WEST:
                    return !canSupportRigidBlock(param1, param0.west());
                case ASCENDING_NORTH:
                    return !canSupportRigidBlock(param1, param0.north());
                case ASCENDING_SOUTH:
                    return !canSupportRigidBlock(param1, param0.south());
                default:
                    return false;
            }
        }
    }

    protected void updateState(BlockState param0, Level param1, BlockPos param2, Block param3) {
    }

    protected BlockState updateDir(Level param0, BlockPos param1, BlockState param2, boolean param3) {
        if (param0.isClientSide) {
            return param2;
        } else {
            RailShape var0 = param2.getValue(this.getShapeProperty());
            return new RailState(param0, param1, param2).place(param0.hasNeighborSignal(param1), param3, var0).getState();
        }
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState param0) {
        return PushReaction.NORMAL;
    }

    @Override
    public void onRemove(BlockState param0, Level param1, BlockPos param2, BlockState param3, boolean param4) {
        if (!param4) {
            super.onRemove(param0, param1, param2, param3, param4);
            if (param0.getValue(this.getShapeProperty()).isAscending()) {
                param1.updateNeighborsAt(param2.above(), this);
            }

            if (this.isStraight) {
                param1.updateNeighborsAt(param2, this);
                param1.updateNeighborsAt(param2.below(), this);
            }

        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        FluidState var0 = param0.getLevel().getFluidState(param0.getClickedPos());
        boolean var1 = var0.getType() == Fluids.WATER;
        BlockState var2 = super.defaultBlockState();
        Direction var3 = param0.getHorizontalDirection();
        boolean var4 = var3 == Direction.EAST || var3 == Direction.WEST;
        return var2.setValue(this.getShapeProperty(), var4 ? RailShape.EAST_WEST : RailShape.NORTH_SOUTH).setValue(WATERLOGGED, Boolean.valueOf(var1));
    }

    public abstract Property<RailShape> getShapeProperty();

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        if (param0.getValue(WATERLOGGED)) {
            param3.getLiquidTicks().scheduleTick(param4, Fluids.WATER, Fluids.WATER.getTickDelay(param3));
        }

        return super.updateShape(param0, param1, param2, param3, param4, param5);
    }

    @Override
    public FluidState getFluidState(BlockState param0) {
        return param0.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(param0);
    }
}
