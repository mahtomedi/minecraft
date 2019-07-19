package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.BlockLayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TrapDoorBlock extends HorizontalDirectionalBlock implements SimpleWaterloggedBlock {
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    public static final EnumProperty<Half> HALF = BlockStateProperties.HALF;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    protected static final VoxelShape EAST_OPEN_AABB = Block.box(0.0, 0.0, 0.0, 3.0, 16.0, 16.0);
    protected static final VoxelShape WEST_OPEN_AABB = Block.box(13.0, 0.0, 0.0, 16.0, 16.0, 16.0);
    protected static final VoxelShape SOUTH_OPEN_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 3.0);
    protected static final VoxelShape NORTH_OPEN_AABB = Block.box(0.0, 0.0, 13.0, 16.0, 16.0, 16.0);
    protected static final VoxelShape BOTTOM_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 3.0, 16.0);
    protected static final VoxelShape TOP_AABB = Block.box(0.0, 13.0, 0.0, 16.0, 16.0, 16.0);

    protected TrapDoorBlock(Block.Properties param0) {
        super(param0);
        this.registerDefaultState(
            this.stateDefinition
                .any()
                .setValue(FACING, Direction.NORTH)
                .setValue(OPEN, Boolean.valueOf(false))
                .setValue(HALF, Half.BOTTOM)
                .setValue(POWERED, Boolean.valueOf(false))
                .setValue(WATERLOGGED, Boolean.valueOf(false))
        );
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        if (!param0.getValue(OPEN)) {
            return param0.getValue(HALF) == Half.TOP ? TOP_AABB : BOTTOM_AABB;
        } else {
            switch((Direction)param0.getValue(FACING)) {
                case NORTH:
                default:
                    return NORTH_OPEN_AABB;
                case SOUTH:
                    return SOUTH_OPEN_AABB;
                case WEST:
                    return WEST_OPEN_AABB;
                case EAST:
                    return EAST_OPEN_AABB;
            }
        }
    }

    @Override
    public boolean isPathfindable(BlockState param0, BlockGetter param1, BlockPos param2, PathComputationType param3) {
        switch(param3) {
            case LAND:
                return param0.getValue(OPEN);
            case WATER:
                return param0.getValue(WATERLOGGED);
            case AIR:
                return param0.getValue(OPEN);
            default:
                return false;
        }
    }

    @Override
    public boolean use(BlockState param0, Level param1, BlockPos param2, Player param3, InteractionHand param4, BlockHitResult param5) {
        if (this.material == Material.METAL) {
            return false;
        } else {
            param0 = param0.cycle(OPEN);
            param1.setBlock(param2, param0, 2);
            if (param0.getValue(WATERLOGGED)) {
                param1.getLiquidTicks().scheduleTick(param2, Fluids.WATER, Fluids.WATER.getTickDelay(param1));
            }

            this.playSound(param3, param1, param2, param0.getValue(OPEN));
            return true;
        }
    }

    protected void playSound(@Nullable Player param0, Level param1, BlockPos param2, boolean param3) {
        if (param3) {
            int var0 = this.material == Material.METAL ? 1037 : 1007;
            param1.levelEvent(param0, var0, param2, 0);
        } else {
            int var1 = this.material == Material.METAL ? 1036 : 1013;
            param1.levelEvent(param0, var1, param2, 0);
        }

    }

    @Override
    public void neighborChanged(BlockState param0, Level param1, BlockPos param2, Block param3, BlockPos param4, boolean param5) {
        if (!param1.isClientSide) {
            boolean var0 = param1.hasNeighborSignal(param2);
            if (var0 != param0.getValue(POWERED)) {
                if (param0.getValue(OPEN) != var0) {
                    param0 = param0.setValue(OPEN, Boolean.valueOf(var0));
                    this.playSound(null, param1, param2, var0);
                }

                param1.setBlock(param2, param0.setValue(POWERED, Boolean.valueOf(var0)), 2);
                if (param0.getValue(WATERLOGGED)) {
                    param1.getLiquidTicks().scheduleTick(param2, Fluids.WATER, Fluids.WATER.getTickDelay(param1));
                }
            }

        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        BlockState var0 = this.defaultBlockState();
        FluidState var1 = param0.getLevel().getFluidState(param0.getClickedPos());
        Direction var2 = param0.getClickedFace();
        if (!param0.replacingClickedOnBlock() && var2.getAxis().isHorizontal()) {
            var0 = var0.setValue(FACING, var2)
                .setValue(HALF, param0.getClickLocation().y - (double)param0.getClickedPos().getY() > 0.5 ? Half.TOP : Half.BOTTOM);
        } else {
            var0 = var0.setValue(FACING, param0.getHorizontalDirection().getOpposite()).setValue(HALF, var2 == Direction.UP ? Half.BOTTOM : Half.TOP);
        }

        if (param0.getLevel().hasNeighborSignal(param0.getClickedPos())) {
            var0 = var0.setValue(OPEN, Boolean.valueOf(true)).setValue(POWERED, Boolean.valueOf(true));
        }

        return var0.setValue(WATERLOGGED, Boolean.valueOf(var1.getType() == Fluids.WATER));
    }

    @Override
    public BlockLayer getRenderLayer() {
        return BlockLayer.CUTOUT;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(FACING, OPEN, HALF, POWERED, WATERLOGGED);
    }

    @Override
    public FluidState getFluidState(BlockState param0) {
        return param0.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(param0);
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        if (param0.getValue(WATERLOGGED)) {
            param3.getLiquidTicks().scheduleTick(param4, Fluids.WATER, Fluids.WATER.getTickDelay(param3));
        }

        return super.updateShape(param0, param1, param2, param3, param4, param5);
    }

    @Override
    public boolean isValidSpawn(BlockState param0, BlockGetter param1, BlockPos param2, EntityType<?> param3) {
        return false;
    }
}
