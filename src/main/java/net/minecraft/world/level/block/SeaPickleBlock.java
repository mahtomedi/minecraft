package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
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
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SeaPickleBlock extends BushBlock implements BonemealableBlock, SimpleWaterloggedBlock {
    public static final int MAX_PICKLES = 4;
    public static final IntegerProperty PICKLES = BlockStateProperties.PICKLES;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    protected static final VoxelShape ONE_AABB = Block.box(6.0, 0.0, 6.0, 10.0, 6.0, 10.0);
    protected static final VoxelShape TWO_AABB = Block.box(3.0, 0.0, 3.0, 13.0, 6.0, 13.0);
    protected static final VoxelShape THREE_AABB = Block.box(2.0, 0.0, 2.0, 14.0, 6.0, 14.0);
    protected static final VoxelShape FOUR_AABB = Block.box(2.0, 0.0, 2.0, 14.0, 7.0, 14.0);

    protected SeaPickleBlock(BlockBehaviour.Properties param0) {
        super(param0);
        this.registerDefaultState(this.stateDefinition.any().setValue(PICKLES, Integer.valueOf(1)).setValue(WATERLOGGED, Boolean.valueOf(true)));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        BlockState var0 = param0.getLevel().getBlockState(param0.getClickedPos());
        if (var0.is(this)) {
            return var0.setValue(PICKLES, Integer.valueOf(Math.min(4, var0.getValue(PICKLES) + 1)));
        } else {
            FluidState var1 = param0.getLevel().getFluidState(param0.getClickedPos());
            boolean var2 = var1.getType() == Fluids.WATER;
            return super.getStateForPlacement(param0).setValue(WATERLOGGED, Boolean.valueOf(var2));
        }
    }

    public static boolean isDead(BlockState param0) {
        return !param0.getValue(WATERLOGGED);
    }

    @Override
    protected boolean mayPlaceOn(BlockState param0, BlockGetter param1, BlockPos param2) {
        return !param0.getCollisionShape(param1, param2).getFaceShape(Direction.UP).isEmpty() || param0.isFaceSturdy(param1, param2, Direction.UP);
    }

    @Override
    public boolean canSurvive(BlockState param0, LevelReader param1, BlockPos param2) {
        BlockPos var0 = param2.below();
        return this.mayPlaceOn(param1.getBlockState(var0), param1, var0);
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        if (!param0.canSurvive(param3, param4)) {
            return Blocks.AIR.defaultBlockState();
        } else {
            if (param0.getValue(WATERLOGGED)) {
                param3.scheduleTick(param4, Fluids.WATER, Fluids.WATER.getTickDelay(param3));
            }

            return super.updateShape(param0, param1, param2, param3, param4, param5);
        }
    }

    @Override
    public boolean canBeReplaced(BlockState param0, BlockPlaceContext param1) {
        return !param1.isSecondaryUseActive() && param1.getItemInHand().is(this.asItem()) && param0.getValue(PICKLES) < 4
            ? true
            : super.canBeReplaced(param0, param1);
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        switch(param0.getValue(PICKLES)) {
            case 1:
            default:
                return ONE_AABB;
            case 2:
                return TWO_AABB;
            case 3:
                return THREE_AABB;
            case 4:
                return FOUR_AABB;
        }
    }

    @Override
    public FluidState getFluidState(BlockState param0) {
        return param0.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(param0);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(PICKLES, WATERLOGGED);
    }

    @Override
    public boolean isValidBonemealTarget(BlockGetter param0, BlockPos param1, BlockState param2, boolean param3) {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(Level param0, RandomSource param1, BlockPos param2, BlockState param3) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel param0, RandomSource param1, BlockPos param2, BlockState param3) {
        if (!isDead(param3) && param0.getBlockState(param2.below()).is(BlockTags.CORAL_BLOCKS)) {
            int var0 = 5;
            int var1 = 1;
            int var2 = 2;
            int var3 = 0;
            int var4 = param2.getX() - 2;
            int var5 = 0;

            for(int var6 = 0; var6 < 5; ++var6) {
                for(int var7 = 0; var7 < var1; ++var7) {
                    int var8 = 2 + param2.getY() - 1;

                    for(int var9 = var8 - 2; var9 < var8; ++var9) {
                        BlockPos var10 = new BlockPos(var4 + var6, var9, param2.getZ() - var5 + var7);
                        if (var10 != param2 && param1.nextInt(6) == 0 && param0.getBlockState(var10).is(Blocks.WATER)) {
                            BlockState var11 = param0.getBlockState(var10.below());
                            if (var11.is(BlockTags.CORAL_BLOCKS)) {
                                param0.setBlock(var10, Blocks.SEA_PICKLE.defaultBlockState().setValue(PICKLES, Integer.valueOf(param1.nextInt(4) + 1)), 3);
                            }
                        }
                    }
                }

                if (var3 < 2) {
                    var1 += 2;
                    ++var5;
                } else {
                    var1 -= 2;
                    --var5;
                }

                ++var3;
            }

            param0.setBlock(param2, param3.setValue(PICKLES, Integer.valueOf(4)), 2);
        }

    }

    @Override
    public boolean isPathfindable(BlockState param0, BlockGetter param1, BlockPos param2, PathComputationType param3) {
        return false;
    }
}
