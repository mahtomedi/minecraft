package net.minecraft.world.level.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.grower.OakTreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MangrovePropaguleBlock extends SaplingBlock implements SimpleWaterloggedBlock {
    public static final IntegerProperty AGE = BlockStateProperties.AGE_4;
    public static final int MAX_AGE = 4;
    private static final VoxelShape[] SHAPE_PER_AGE = new VoxelShape[]{
        Block.box(7.0, 13.0, 7.0, 9.0, 16.0, 9.0),
        Block.box(7.0, 10.0, 7.0, 9.0, 16.0, 9.0),
        Block.box(7.0, 7.0, 7.0, 9.0, 16.0, 9.0),
        Block.box(7.0, 3.0, 7.0, 9.0, 16.0, 9.0),
        Block.box(7.0, 0.0, 7.0, 9.0, 16.0, 9.0)
    };
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty HANGING = BlockStateProperties.HANGING;

    public MangrovePropaguleBlock(BlockBehaviour.Properties param0) {
        super(new OakTreeGrower(), param0);
        this.registerDefaultState(
            this.stateDefinition
                .any()
                .setValue(STAGE, Integer.valueOf(0))
                .setValue(AGE, Integer.valueOf(0))
                .setValue(WATERLOGGED, Boolean.valueOf(false))
                .setValue(HANGING, Boolean.valueOf(false))
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(STAGE).add(AGE).add(WATERLOGGED).add(HANGING);
    }

    @Override
    protected boolean mayPlaceOn(BlockState param0, BlockGetter param1, BlockPos param2) {
        return param0.is(BlockTags.DIRT) || param0.is(Blocks.FARMLAND) || param0.is(Blocks.CLAY) || param0.is(Blocks.MUD);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        FluidState var0 = param0.getLevel().getFluidState(param0.getClickedPos());
        boolean var1 = var0.getType() == Fluids.WATER;
        return super.getStateForPlacement(param0).setValue(WATERLOGGED, Boolean.valueOf(var1)).setValue(AGE, Integer.valueOf(4));
    }

    @Override
    public InteractionResult use(BlockState param0, Level param1, BlockPos param2, Player param3, InteractionHand param4, BlockHitResult param5) {
        return super.use(param0, param1, param2, param3, param4, param5);
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        Vec3 var0 = param0.getOffset(param1, param2);
        VoxelShape var1;
        if (!param0.getValue(HANGING)) {
            var1 = SHAPE_PER_AGE[4];
        } else {
            var1 = SHAPE_PER_AGE[param0.getValue(AGE)];
        }

        return var1.move(var0.x, var0.y, var0.z);
    }

    @Override
    public BlockBehaviour.OffsetType getOffsetType() {
        return BlockBehaviour.OffsetType.XZ;
    }

    @Override
    public boolean canSurvive(BlockState param0, LevelReader param1, BlockPos param2) {
        return isHanging(param0) ? param1.getBlockState(param2.above()).is(Blocks.MANGROVE_LEAVES) : super.canSurvive(param0, param1, param2);
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        if (param0.getValue(WATERLOGGED)) {
            param3.scheduleTick(param4, Fluids.WATER, Fluids.WATER.getTickDelay(param3));
        }

        return param1 == Direction.UP && !param0.canSurvive(param3, param4)
            ? Blocks.AIR.defaultBlockState()
            : super.updateShape(param0, param1, param2, param3, param4, param5);
    }

    @Override
    public FluidState getFluidState(BlockState param0) {
        return param0.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(param0);
    }

    @Override
    public void randomTick(BlockState param0, ServerLevel param1, BlockPos param2, Random param3) {
        if (!isHanging(param0)) {
            if (param3.nextInt(7) == 0) {
                this.advanceTree(param1, param2, param0, param3);
            }

        } else {
            if (!isFullyGrown(param0)) {
                param1.setBlock(param2, param0.cycle(AGE), 2);
            }

        }
    }

    @Override
    public boolean isValidBonemealTarget(BlockGetter param0, BlockPos param1, BlockState param2, boolean param3) {
        return !isHanging(param2) || !isFullyGrown(param2);
    }

    @Override
    public boolean isBonemealSuccess(Level param0, Random param1, BlockPos param2, BlockState param3) {
        return isHanging(param3) ? !isFullyGrown(param3) : super.isBonemealSuccess(param0, param1, param2, param3);
    }

    @Override
    public void performBonemeal(ServerLevel param0, Random param1, BlockPos param2, BlockState param3) {
        if (isHanging(param3) && !isFullyGrown(param3)) {
            param0.setBlock(param2, param3.cycle(AGE), 2);
        } else {
            super.performBonemeal(param0, param1, param2, param3);
        }

    }

    private static boolean isHanging(BlockState param0) {
        return param0.getValue(HANGING);
    }

    private static boolean isFullyGrown(BlockState param0) {
        return param0.getValue(AGE) == 4;
    }

    public static BlockState createNewHangingPropagule() {
        return Blocks.MANGROVE_PROPAGULE.defaultBlockState().setValue(HANGING, Boolean.valueOf(true)).setValue(AGE, Integer.valueOf(0));
    }
}
