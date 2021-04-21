package net.minecraft.world.level.block;

import java.util.Random;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class StemBlock extends BushBlock implements BonemealableBlock {
    public static final int MAX_AGE = 7;
    public static final IntegerProperty AGE = BlockStateProperties.AGE_7;
    protected static final float AABB_OFFSET = 1.0F;
    protected static final VoxelShape[] SHAPE_BY_AGE = new VoxelShape[]{
        Block.box(7.0, 0.0, 7.0, 9.0, 2.0, 9.0),
        Block.box(7.0, 0.0, 7.0, 9.0, 4.0, 9.0),
        Block.box(7.0, 0.0, 7.0, 9.0, 6.0, 9.0),
        Block.box(7.0, 0.0, 7.0, 9.0, 8.0, 9.0),
        Block.box(7.0, 0.0, 7.0, 9.0, 10.0, 9.0),
        Block.box(7.0, 0.0, 7.0, 9.0, 12.0, 9.0),
        Block.box(7.0, 0.0, 7.0, 9.0, 14.0, 9.0),
        Block.box(7.0, 0.0, 7.0, 9.0, 16.0, 9.0)
    };
    private final StemGrownBlock fruit;
    private final Supplier<Item> seedSupplier;

    protected StemBlock(StemGrownBlock param0, Supplier<Item> param1, BlockBehaviour.Properties param2) {
        super(param2);
        this.fruit = param0;
        this.seedSupplier = param1;
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, Integer.valueOf(0)));
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return SHAPE_BY_AGE[param0.getValue(AGE)];
    }

    @Override
    protected boolean mayPlaceOn(BlockState param0, BlockGetter param1, BlockPos param2) {
        return param0.is(Blocks.FARMLAND);
    }

    @Override
    public void randomTick(BlockState param0, ServerLevel param1, BlockPos param2, Random param3) {
        if (param1.getRawBrightness(param2, 0) >= 9) {
            float var0 = CropBlock.getGrowthSpeed(this, param1, param2);
            if (param3.nextInt((int)(25.0F / var0) + 1) == 0) {
                int var1 = param0.getValue(AGE);
                if (var1 < 7) {
                    param0 = param0.setValue(AGE, Integer.valueOf(var1 + 1));
                    param1.setBlock(param2, param0, 2);
                } else {
                    Direction var2 = Direction.Plane.HORIZONTAL.getRandomDirection(param3);
                    BlockPos var3 = param2.relative(var2);
                    BlockState var4 = param1.getBlockState(var3.below());
                    if (param1.getBlockState(var3).isAir() && (var4.is(Blocks.FARMLAND) || var4.is(BlockTags.DIRT))) {
                        param1.setBlockAndUpdate(var3, this.fruit.defaultBlockState());
                        param1.setBlockAndUpdate(param2, this.fruit.getAttachedStem().defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, var2));
                    }
                }
            }

        }
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter param0, BlockPos param1, BlockState param2) {
        return new ItemStack(this.seedSupplier.get());
    }

    @Override
    public boolean isValidBonemealTarget(BlockGetter param0, BlockPos param1, BlockState param2, boolean param3) {
        return param2.getValue(AGE) != 7;
    }

    @Override
    public boolean isBonemealSuccess(Level param0, Random param1, BlockPos param2, BlockState param3) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel param0, Random param1, BlockPos param2, BlockState param3) {
        int var0 = Math.min(7, param3.getValue(AGE) + Mth.nextInt(param0.random, 2, 5));
        BlockState var1 = param3.setValue(AGE, Integer.valueOf(var0));
        param0.setBlock(param2, var1, 2);
        if (var0 == 7) {
            var1.randomTick(param0, param2, param0.random);
        }

    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(AGE);
    }

    public StemGrownBlock getFruit() {
        return this.fruit;
    }
}
