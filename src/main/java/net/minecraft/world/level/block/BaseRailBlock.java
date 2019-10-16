package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class BaseRailBlock extends Block {
    protected static final VoxelShape FLAT_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0);
    protected static final VoxelShape HALF_BLOCK_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0);
    private final boolean isStraight;

    public static boolean isRail(Level param0, BlockPos param1) {
        return isRail(param0.getBlockState(param1));
    }

    public static boolean isRail(BlockState param0) {
        return param0.is(BlockTags.RAILS);
    }

    protected BaseRailBlock(boolean param0, Block.Properties param1) {
        super(param1);
        this.isStraight = param0;
    }

    public boolean isStraight() {
        return this.isStraight;
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        RailShape var0 = param0.getBlock() == this ? param0.getValue(this.getShapeProperty()) : null;
        return var0 != null && var0.isAscending() ? HALF_BLOCK_AABB : FLAT_AABB;
    }

    @Override
    public boolean canSurvive(BlockState param0, LevelReader param1, BlockPos param2) {
        return canSupportRigidBlock(param1, param2.below());
    }

    @Override
    public void onPlace(BlockState param0, Level param1, BlockPos param2, BlockState param3, boolean param4) {
        if (param3.getBlock() != param0.getBlock()) {
            param0 = this.updateDir(param1, param2, param0, true);
            if (this.isStraight) {
                param0.neighborChanged(param1, param2, this, param2, param4);
            }

        }
    }

    @Override
    public void neighborChanged(BlockState param0, Level param1, BlockPos param2, Block param3, BlockPos param4, boolean param5) {
        if (!param1.isClientSide) {
            RailShape var0 = param0.getValue(this.getShapeProperty());
            boolean var1 = false;
            BlockPos var2 = param2.below();
            if (!canSupportRigidBlock(param1, var2)) {
                var1 = true;
            }

            BlockPos var3 = param2.east();
            if (var0 == RailShape.ASCENDING_EAST && !canSupportRigidBlock(param1, var3)) {
                var1 = true;
            } else {
                BlockPos var4 = param2.west();
                if (var0 == RailShape.ASCENDING_WEST && !canSupportRigidBlock(param1, var4)) {
                    var1 = true;
                } else {
                    BlockPos var5 = param2.north();
                    if (var0 == RailShape.ASCENDING_NORTH && !canSupportRigidBlock(param1, var5)) {
                        var1 = true;
                    } else {
                        BlockPos var6 = param2.south();
                        if (var0 == RailShape.ASCENDING_SOUTH && !canSupportRigidBlock(param1, var6)) {
                            var1 = true;
                        }
                    }
                }
            }

            if (var1 && !param1.isEmptyBlock(param2)) {
                if (!param5) {
                    dropResources(param0, param1, param2);
                }

                param1.removeBlock(param2, param5);
            } else {
                this.updateState(param0, param1, param2, param3);
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
        BlockState var0 = super.defaultBlockState();
        Direction var1 = param0.getHorizontalDirection();
        boolean var2 = var1 == Direction.EAST || var1 == Direction.WEST;
        return var0.setValue(this.getShapeProperty(), var2 ? RailShape.EAST_WEST : RailShape.NORTH_SOUTH);
    }

    public abstract Property<RailShape> getShapeProperty();
}
