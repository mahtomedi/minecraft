package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PitcherCropBlock extends DoublePlantBlock implements BonemealableBlock {
    public static final IntegerProperty AGE = BlockStateProperties.AGE_4;
    public static final int MAX_AGE = 4;
    private static final int DOUBLE_PLANT_AGE_INTERSECTION = 3;
    private static final int BONEMEAL_INCREASE = 1;
    private static final VoxelShape UPPER_SHAPE = Block.box(3.0, 0.0, 3.0, 13.0, 15.0, 13.0);
    private static final VoxelShape LOWER_SHAPE = Block.box(3.0, -1.0, 3.0, 13.0, 16.0, 13.0);
    private static final VoxelShape COLLISION_SHAPE_BULB = Block.box(5.0, -1.0, 5.0, 11.0, 3.0, 11.0);
    private static final VoxelShape COLLISION_SHAPE_CROP = Block.box(3.0, -1.0, 3.0, 13.0, 5.0, 13.0);

    public PitcherCropBlock(BlockBehaviour.Properties param0) {
        super(param0);
    }

    private boolean isMaxAge(BlockState param0) {
        return param0.getValue(AGE) >= 4;
    }

    @Override
    public boolean isRandomlyTicking(BlockState param0) {
        return param0.getValue(HALF) == DoubleBlockHalf.LOWER && !this.isMaxAge(param0);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        return this.defaultBlockState();
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        return !param0.canSurvive(param3, param4) ? Blocks.AIR.defaultBlockState() : param0;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        if (param0.getValue(AGE) == 0) {
            return COLLISION_SHAPE_BULB;
        } else {
            return param0.getValue(HALF) == DoubleBlockHalf.LOWER ? COLLISION_SHAPE_CROP : super.getCollisionShape(param0, param1, param2, param3);
        }
    }

    @Override
    public boolean canSurvive(BlockState param0, LevelReader param1, BlockPos param2) {
        if (param0.getValue(HALF) == DoubleBlockHalf.LOWER && param0.getValue(AGE) >= 3) {
            BlockState var0 = param1.getBlockState(param2.above());
            return var0.is(this) && var0.getValue(HALF) == DoubleBlockHalf.UPPER && this.mayPlaceOn(param1.getBlockState(param2.below()), param1, param2);
        } else {
            return (param1.getRawBrightness(param2, 0) >= 8 || param1.canSeeSky(param2)) && super.canSurvive(param0, param1, param2);
        }
    }

    @Override
    protected boolean mayPlaceOn(BlockState param0, BlockGetter param1, BlockPos param2) {
        return param0.is(Blocks.FARMLAND);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(AGE);
        super.createBlockStateDefinition(param0);
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return param0.getValue(HALF) == DoubleBlockHalf.UPPER ? UPPER_SHAPE : LOWER_SHAPE;
    }

    @Override
    public boolean canBeReplaced(BlockState param0, BlockPlaceContext param1) {
        return false;
    }

    @Override
    public void setPlacedBy(Level param0, BlockPos param1, BlockState param2, LivingEntity param3, ItemStack param4) {
    }

    @Override
    public void randomTick(BlockState param0, ServerLevel param1, BlockPos param2, RandomSource param3) {
        float var0 = CropBlock.getGrowthSpeed(this, param1, param2);
        boolean var1 = param3.nextInt((int)(25.0F / var0) + 1) == 0;
        if (var1) {
            this.grow(param1, param0, param2, 1);
        }

    }

    private void grow(ServerLevel param0, BlockState param1, BlockPos param2, int param3) {
        int var0 = Math.min(param1.getValue(AGE) + param3, 4);
        if (var0 < 3 || canGrowInto(param0, param2.above())) {
            param0.setBlock(param2, param1.setValue(AGE, Integer.valueOf(var0)), 2);
            if (var0 >= 3) {
                DoubleBlockHalf var1 = param1.getValue(HALF);
                if (var1 == DoubleBlockHalf.LOWER) {
                    BlockPos var2 = param2.above();
                    param0.setBlock(
                        var2,
                        copyWaterloggedFrom(param0, param2, this.defaultBlockState().setValue(AGE, Integer.valueOf(var0)).setValue(HALF, DoubleBlockHalf.UPPER)),
                        3
                    );
                } else if (var1 == DoubleBlockHalf.UPPER) {
                    BlockPos var3 = param2.below();
                    param0.setBlock(
                        var3,
                        copyWaterloggedFrom(param0, param2, this.defaultBlockState().setValue(AGE, Integer.valueOf(var0)).setValue(HALF, DoubleBlockHalf.LOWER)),
                        3
                    );
                }

            }
        }
    }

    private static boolean canGrowInto(ServerLevel param0, BlockPos param1) {
        BlockState var0 = param0.getBlockState(param1);
        return var0.isAir() || var0.is(Blocks.PITCHER_CROP);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader param0, BlockPos param1, BlockState param2, boolean param3) {
        return !this.isMaxAge(param2);
    }

    @Override
    public boolean isBonemealSuccess(Level param0, RandomSource param1, BlockPos param2, BlockState param3) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel param0, RandomSource param1, BlockPos param2, BlockState param3) {
        this.grow(param0, param3, param2, 1);
    }
}
