package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TorchflowerCropBlock extends CropBlock {
    public static final int MAX_AGE = 2;
    public static final IntegerProperty AGE = BlockStateProperties.AGE_1;
    private static final float AABB_OFFSET = 3.0F;
    private static final VoxelShape[] SHAPE_BY_AGE = new VoxelShape[]{Block.box(5.0, 0.0, 5.0, 11.0, 10.0, 11.0), Block.box(5.0, 0.0, 5.0, 11.0, 10.0, 11.0)};

    public TorchflowerCropBlock(BlockBehaviour.Properties param0) {
        super(param0);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(AGE);
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return SHAPE_BY_AGE[param0.getValue(this.getAgeProperty())];
    }

    @Override
    public IntegerProperty getAgeProperty() {
        return AGE;
    }

    @Override
    public int getMaxAge() {
        return 2;
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return Items.TORCHFLOWER_SEEDS;
    }

    @Override
    public BlockState getStateForAge(int param0) {
        return param0 == 2 ? Blocks.TORCHFLOWER.defaultBlockState() : super.getStateForAge(param0);
    }

    @Override
    public void randomTick(BlockState param0, ServerLevel param1, BlockPos param2, RandomSource param3) {
        if (param3.nextInt(3) != 0) {
            super.randomTick(param0, param1, param2, param3);
        }

    }

    @Override
    protected int getBonemealAgeIncrease(Level param0) {
        return super.getBonemealAgeIncrease(param0) / 3;
    }
}
