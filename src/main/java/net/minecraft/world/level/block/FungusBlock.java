package net.minecraft.world.level.block;

import java.util.Random;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.HugeFungusConfiguration;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FungusBlock extends BushBlock implements BonemealableBlock {
    protected static final VoxelShape SHAPE = Block.box(4.0, 0.0, 4.0, 12.0, 9.0, 12.0);
    private final Supplier<ConfiguredFeature<HugeFungusConfiguration, ?>> feature;

    protected FungusBlock(BlockBehaviour.Properties param0, Supplier<ConfiguredFeature<HugeFungusConfiguration, ?>> param1) {
        super(param0);
        this.feature = param1;
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return SHAPE;
    }

    @Override
    protected boolean mayPlaceOn(BlockState param0, BlockGetter param1, BlockPos param2) {
        Block var0 = param0.getBlock();
        return param0.is(BlockTags.NYLIUM) || var0 == Blocks.SOUL_SOIL || super.mayPlaceOn(param0, param1, param2);
    }

    @Override
    public boolean isValidBonemealTarget(BlockGetter param0, BlockPos param1, BlockState param2, boolean param3) {
        Block var0 = ((HugeFungusConfiguration)this.feature.get().config).validBaseState.getBlock();
        Block var1 = param0.getBlockState(param1.below()).getBlock();
        return var1 == var0;
    }

    @Override
    public boolean isBonemealSuccess(Level param0, Random param1, BlockPos param2, BlockState param3) {
        return (double)param1.nextFloat() < 0.4;
    }

    @Override
    public void performBonemeal(ServerLevel param0, Random param1, BlockPos param2, BlockState param3) {
        this.feature.get().place(param0, param0.getChunkSource().getGenerator(), param1, param2);
    }
}
