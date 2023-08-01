package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
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
    private static final VoxelShape FULL_UPPER_SHAPE = Block.box(3.0, 0.0, 3.0, 13.0, 15.0, 13.0);
    private static final VoxelShape FULL_LOWER_SHAPE = Block.box(3.0, -1.0, 3.0, 13.0, 16.0, 13.0);
    private static final VoxelShape COLLISION_SHAPE_BULB = Block.box(5.0, -1.0, 5.0, 11.0, 3.0, 11.0);
    private static final VoxelShape COLLISION_SHAPE_CROP = Block.box(3.0, -1.0, 3.0, 13.0, 5.0, 13.0);
    private static final VoxelShape[] UPPER_SHAPE_BY_AGE = new VoxelShape[]{Block.box(3.0, 0.0, 3.0, 13.0, 11.0, 13.0), FULL_UPPER_SHAPE};
    private static final VoxelShape[] LOWER_SHAPE_BY_AGE = new VoxelShape[]{
        COLLISION_SHAPE_BULB, Block.box(3.0, -1.0, 3.0, 13.0, 14.0, 13.0), FULL_LOWER_SHAPE, FULL_LOWER_SHAPE, FULL_LOWER_SHAPE
    };

    public PitcherCropBlock(BlockBehaviour.Properties param0) {
        super(param0);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        return this.defaultBlockState();
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return param0.getValue(HALF) == DoubleBlockHalf.UPPER
            ? UPPER_SHAPE_BY_AGE[Math.min(Math.abs(4 - (param0.getValue(AGE) + 1)), UPPER_SHAPE_BY_AGE.length - 1)]
            : LOWER_SHAPE_BY_AGE[param0.getValue(AGE)];
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
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        if (isDouble(param0.getValue(AGE))) {
            return super.updateShape(param0, param1, param2, param3, param4, param5);
        } else {
            return param0.canSurvive(param3, param4) ? param0 : Blocks.AIR.defaultBlockState();
        }
    }

    @Override
    public boolean canSurvive(BlockState param0, LevelReader param1, BlockPos param2) {
        return isLower(param0) && !sufficientLight(param1, param2) ? false : super.canSurvive(param0, param1, param2);
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
    public void entityInside(BlockState param0, Level param1, BlockPos param2, Entity param3) {
        if (param3 instanceof Ravager && param1.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            param1.destroyBlock(param2, true, param3);
        }

        super.entityInside(param0, param1, param2, param3);
    }

    @Override
    public boolean canBeReplaced(BlockState param0, BlockPlaceContext param1) {
        return false;
    }

    @Override
    public void setPlacedBy(Level param0, BlockPos param1, BlockState param2, LivingEntity param3, ItemStack param4) {
    }

    @Override
    public boolean isRandomlyTicking(BlockState param0) {
        return param0.getValue(HALF) == DoubleBlockHalf.LOWER && !this.isMaxAge(param0);
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
        if (this.canGrow(param0, param2, param1, var0)) {
            BlockState var1 = param1.setValue(AGE, Integer.valueOf(var0));
            param0.setBlock(param2, var1, 2);
            if (isDouble(var0)) {
                param0.setBlock(param2.above(), var1.setValue(HALF, DoubleBlockHalf.UPPER), 3);
            }

        }
    }

    private static boolean canGrowInto(LevelReader param0, BlockPos param1) {
        BlockState var0 = param0.getBlockState(param1);
        return var0.isAir() || var0.is(Blocks.PITCHER_CROP);
    }

    private static boolean sufficientLight(LevelReader param0, BlockPos param1) {
        return CropBlock.hasSufficientLight(param0, param1);
    }

    private static boolean isLower(BlockState param0) {
        return param0.is(Blocks.PITCHER_CROP) && param0.getValue(HALF) == DoubleBlockHalf.LOWER;
    }

    private static boolean isDouble(int param0) {
        return param0 >= 3;
    }

    private boolean canGrow(LevelReader param0, BlockPos param1, BlockState param2, int param3) {
        return !this.isMaxAge(param2) && sufficientLight(param0, param1) && (!isDouble(param3) || canGrowInto(param0, param1.above()));
    }

    private boolean isMaxAge(BlockState param0) {
        return param0.getValue(AGE) >= 4;
    }

    @Nullable
    private PitcherCropBlock.PosAndState getLowerHalf(LevelReader param0, BlockPos param1, BlockState param2) {
        if (isLower(param2)) {
            return new PitcherCropBlock.PosAndState(param1, param2);
        } else {
            BlockPos var0 = param1.below();
            BlockState var1 = param0.getBlockState(var0);
            return isLower(var1) ? new PitcherCropBlock.PosAndState(var0, var1) : null;
        }
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader param0, BlockPos param1, BlockState param2) {
        PitcherCropBlock.PosAndState var0 = this.getLowerHalf(param0, param1, param2);
        return var0 == null ? false : this.canGrow(param0, var0.pos, var0.state, var0.state.getValue(AGE) + 1);
    }

    @Override
    public boolean isBonemealSuccess(Level param0, RandomSource param1, BlockPos param2, BlockState param3) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel param0, RandomSource param1, BlockPos param2, BlockState param3) {
        PitcherCropBlock.PosAndState var0 = this.getLowerHalf(param0, param2, param3);
        if (var0 != null) {
            this.grow(param0, var0.state, var0.pos, 1);
        }
    }

    static record PosAndState(BlockPos pos, BlockState state) {
    }
}
