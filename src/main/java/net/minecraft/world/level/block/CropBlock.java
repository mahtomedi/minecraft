package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CropBlock extends BushBlock implements BonemealableBlock, FarmableBlock {
    public static final int MAX_AGE = 7;
    public static final IntegerProperty AGE = BlockStateProperties.AGE_7;
    private static final VoxelShape[] SHAPE_BY_AGE = new VoxelShape[]{
        Block.box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0),
        Block.box(0.0, 0.0, 0.0, 16.0, 4.0, 16.0),
        Block.box(0.0, 0.0, 0.0, 16.0, 6.0, 16.0),
        Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0),
        Block.box(0.0, 0.0, 0.0, 16.0, 10.0, 16.0),
        Block.box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0),
        Block.box(0.0, 0.0, 0.0, 16.0, 14.0, 16.0),
        Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0)
    };

    protected CropBlock(BlockBehaviour.Properties param0) {
        super(param0);
        this.registerDefaultState(this.stateDefinition.any().setValue(this.getAgeProperty(), Integer.valueOf(0)));
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return SHAPE_BY_AGE[param0.getValue(this.getAgeProperty())];
    }

    @Override
    protected boolean mayPlaceOn(BlockState param0, BlockGetter param1, BlockPos param2) {
        return param0.is(Blocks.FARMLAND);
    }

    public IntegerProperty getAgeProperty() {
        return AGE;
    }

    public int getMaxAge() {
        return 7;
    }

    protected int getAge(BlockState param0) {
        return param0.getValue(this.getAgeProperty());
    }

    public BlockState getStateForAge(int param0) {
        return this.defaultBlockState().setValue(this.getAgeProperty(), Integer.valueOf(param0));
    }

    public boolean isMaxAge(BlockState param0) {
        return param0.getValue(this.getAgeProperty()) >= this.getMaxAge();
    }

    @Override
    public boolean isRandomlyTicking(BlockState param0) {
        return !this.isMaxAge(param0);
    }

    @Override
    public void randomTick(BlockState param0, ServerLevel param1, BlockPos param2, RandomSource param3) {
        if (param1.getRawBrightness(param2, 0) >= 9) {
            int var0 = this.getAge(param0);
            if (var0 < this.getMaxAge()) {
                float var1 = getGrowthSpeed(this, param1, param2);
                if (param3.nextInt((int)(25.0F / var1) + 1) == 0) {
                    param1.setBlock(param2, this.getStateForAge(var0 + 1), 2);
                }
            }
        }

    }

    public void growCrops(Level param0, BlockPos param1, BlockState param2) {
        int var0 = this.getAge(param2) + this.getBonemealAgeIncrease(param0);
        int var1 = this.getMaxAge();
        if (var0 > var1) {
            var0 = var1;
        }

        param0.setBlock(param1, this.getStateForAge(var0), 2);
    }

    protected int getBonemealAgeIncrease(Level param0) {
        return Mth.nextInt(param0.random, 2, 5);
    }

    protected static float getGrowthSpeed(Block param0, BlockGetter param1, BlockPos param2) {
        float var0 = 1.0F;
        BlockPos var1 = param2.below();

        for(int var2 = -1; var2 <= 1; ++var2) {
            for(int var3 = -1; var3 <= 1; ++var3) {
                float var4 = 0.0F;
                BlockState var5 = param1.getBlockState(var1.offset(var2, 0, var3));
                if (var5.is(Blocks.FARMLAND)) {
                    var4 = 1.0F;
                    if (var5.getValue(FarmBlock.MOISTURE) > 0) {
                        var4 = 3.0F;
                    }
                }

                if (var2 != 0 || var3 != 0) {
                    var4 /= 4.0F;
                }

                var0 += var4;
            }
        }

        BlockPos var6 = param2.north();
        BlockPos var7 = param2.south();
        BlockPos var8 = param2.west();
        BlockPos var9 = param2.east();
        boolean var10 = param1.getBlockState(var8).is(param0) || param1.getBlockState(var9).is(param0);
        boolean var11 = param1.getBlockState(var6).is(param0) || param1.getBlockState(var7).is(param0);
        if (var10 && var11) {
            var0 /= 2.0F;
        } else {
            boolean var12 = param1.getBlockState(var8.north()).is(param0)
                || param1.getBlockState(var9.north()).is(param0)
                || param1.getBlockState(var9.south()).is(param0)
                || param1.getBlockState(var8.south()).is(param0);
            if (var12) {
                var0 /= 2.0F;
            }
        }

        return var0;
    }

    @Override
    public boolean canSurvive(BlockState param0, LevelReader param1, BlockPos param2) {
        return (param1.getRawBrightness(param2, 0) >= 8 || param1.canSeeSky(param2)) && super.canSurvive(param0, param1, param2);
    }

    @Override
    public void entityInside(BlockState param0, Level param1, BlockPos param2, Entity param3) {
        if (param3 instanceof Ravager && param1.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            param1.destroyBlock(param2, true, param3);
        }

        super.entityInside(param0, param1, param2, param3);
    }

    protected ItemLike getBaseSeedId() {
        return Items.WHEAT_SEEDS;
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter param0, BlockPos param1, BlockState param2) {
        return new ItemStack(this.getBaseSeedId());
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
        this.growCrops(param0, param2, param3);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(AGE);
    }
}
