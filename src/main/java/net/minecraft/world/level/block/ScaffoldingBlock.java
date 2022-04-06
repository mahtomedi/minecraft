package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
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
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ScaffoldingBlock extends Block implements SimpleWaterloggedBlock {
    private static final int TICK_DELAY = 1;
    private static final VoxelShape STABLE_SHAPE;
    private static final VoxelShape UNSTABLE_SHAPE;
    private static final VoxelShape UNSTABLE_SHAPE_BOTTOM = Block.box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0);
    private static final VoxelShape BELOW_BLOCK = Shapes.block().move(0.0, -1.0, 0.0);
    public static final int STABILITY_MAX_DISTANCE = 7;
    public static final IntegerProperty DISTANCE = BlockStateProperties.STABILITY_DISTANCE;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty BOTTOM = BlockStateProperties.BOTTOM;

    protected ScaffoldingBlock(BlockBehaviour.Properties param0) {
        super(param0);
        this.registerDefaultState(
            this.stateDefinition
                .any()
                .setValue(DISTANCE, Integer.valueOf(7))
                .setValue(WATERLOGGED, Boolean.valueOf(false))
                .setValue(BOTTOM, Boolean.valueOf(false))
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(DISTANCE, WATERLOGGED, BOTTOM);
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        if (!param3.isHoldingItem(param0.getBlock().asItem())) {
            return param0.getValue(BOTTOM) ? UNSTABLE_SHAPE : STABLE_SHAPE;
        } else {
            return Shapes.block();
        }
    }

    @Override
    public VoxelShape getInteractionShape(BlockState param0, BlockGetter param1, BlockPos param2) {
        return Shapes.block();
    }

    @Override
    public boolean canBeReplaced(BlockState param0, BlockPlaceContext param1) {
        return param1.getItemInHand().is(this.asItem());
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        BlockPos var0 = param0.getClickedPos();
        Level var1 = param0.getLevel();
        int var2 = getDistance(var1, var0);
        return this.defaultBlockState()
            .setValue(WATERLOGGED, Boolean.valueOf(var1.getFluidState(var0).getType() == Fluids.WATER))
            .setValue(DISTANCE, Integer.valueOf(var2))
            .setValue(BOTTOM, Boolean.valueOf(this.isBottom(var1, var0, var2)));
    }

    @Override
    public void onPlace(BlockState param0, Level param1, BlockPos param2, BlockState param3, boolean param4) {
        if (!param1.isClientSide) {
            param1.scheduleTick(param2, this, 1);
        }

    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        if (param0.getValue(WATERLOGGED)) {
            param3.scheduleTick(param4, Fluids.WATER, Fluids.WATER.getTickDelay(param3));
        }

        if (!param3.isClientSide()) {
            param3.scheduleTick(param4, this, 1);
        }

        return param0;
    }

    @Override
    public void tick(BlockState param0, ServerLevel param1, BlockPos param2, RandomSource param3) {
        int var0 = getDistance(param1, param2);
        BlockState var1 = param0.setValue(DISTANCE, Integer.valueOf(var0)).setValue(BOTTOM, Boolean.valueOf(this.isBottom(param1, param2, var0)));
        if (var1.getValue(DISTANCE) == 7) {
            if (param0.getValue(DISTANCE) == 7) {
                FallingBlockEntity.fall(param1, param2, var1);
            } else {
                param1.destroyBlock(param2, true);
            }
        } else if (param0 != var1) {
            param1.setBlock(param2, var1, 3);
        }

    }

    @Override
    public boolean canSurvive(BlockState param0, LevelReader param1, BlockPos param2) {
        return getDistance(param1, param2) < 7;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        if (param3.isAbove(Shapes.block(), param2, true) && !param3.isDescending()) {
            return STABLE_SHAPE;
        } else {
            return param0.getValue(DISTANCE) != 0 && param0.getValue(BOTTOM) && param3.isAbove(BELOW_BLOCK, param2, true)
                ? UNSTABLE_SHAPE_BOTTOM
                : Shapes.empty();
        }
    }

    @Override
    public FluidState getFluidState(BlockState param0) {
        return param0.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(param0);
    }

    private boolean isBottom(BlockGetter param0, BlockPos param1, int param2) {
        return param2 > 0 && !param0.getBlockState(param1.below()).is(this);
    }

    public static int getDistance(BlockGetter param0, BlockPos param1) {
        BlockPos.MutableBlockPos var0 = param1.mutable().move(Direction.DOWN);
        BlockState var1 = param0.getBlockState(var0);
        int var2 = 7;
        if (var1.is(Blocks.SCAFFOLDING)) {
            var2 = var1.getValue(DISTANCE);
        } else if (var1.isFaceSturdy(param0, var0, Direction.UP)) {
            return 0;
        }

        for(Direction var3 : Direction.Plane.HORIZONTAL) {
            BlockState var4 = param0.getBlockState(var0.setWithOffset(param1, var3));
            if (var4.is(Blocks.SCAFFOLDING)) {
                var2 = Math.min(var2, var4.getValue(DISTANCE) + 1);
                if (var2 == 1) {
                    break;
                }
            }
        }

        return var2;
    }

    static {
        VoxelShape var0 = Block.box(0.0, 14.0, 0.0, 16.0, 16.0, 16.0);
        VoxelShape var1 = Block.box(0.0, 0.0, 0.0, 2.0, 16.0, 2.0);
        VoxelShape var2 = Block.box(14.0, 0.0, 0.0, 16.0, 16.0, 2.0);
        VoxelShape var3 = Block.box(0.0, 0.0, 14.0, 2.0, 16.0, 16.0);
        VoxelShape var4 = Block.box(14.0, 0.0, 14.0, 16.0, 16.0, 16.0);
        STABLE_SHAPE = Shapes.or(var0, var1, var2, var3, var4);
        VoxelShape var5 = Block.box(0.0, 0.0, 0.0, 2.0, 2.0, 16.0);
        VoxelShape var6 = Block.box(14.0, 0.0, 0.0, 16.0, 2.0, 16.0);
        VoxelShape var7 = Block.box(0.0, 0.0, 14.0, 16.0, 2.0, 16.0);
        VoxelShape var8 = Block.box(0.0, 0.0, 0.0, 16.0, 2.0, 2.0);
        UNSTABLE_SHAPE = Shapes.or(ScaffoldingBlock.UNSTABLE_SHAPE_BOTTOM, STABLE_SHAPE, var6, var5, var8, var7);
    }
}
