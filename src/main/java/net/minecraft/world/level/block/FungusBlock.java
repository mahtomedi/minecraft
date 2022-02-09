package net.minecraft.world.level.block;

import java.util.Random;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
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
    private static final double BONEMEAL_SUCCESS_PROBABILITY = 0.4;
    private final Supplier<Holder<ConfiguredFeature<HugeFungusConfiguration, ?>>> feature;

    protected FungusBlock(BlockBehaviour.Properties param0, Supplier<Holder<ConfiguredFeature<HugeFungusConfiguration, ?>>> param1) {
        super(param0);
        this.feature = param1;
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return SHAPE;
    }

    @Override
    protected boolean mayPlaceOn(BlockState param0, BlockGetter param1, BlockPos param2) {
        return param0.is(BlockTags.NYLIUM) || param0.is(Blocks.MYCELIUM) || param0.is(Blocks.SOUL_SOIL) || super.mayPlaceOn(param0, param1, param2);
    }

    @Override
    public boolean isValidBonemealTarget(BlockGetter param0, BlockPos param1, BlockState param2, boolean param3) {
        Block var0 = this.feature.get().value().config().validBaseState.getBlock();
        BlockState var1 = param0.getBlockState(param1.below());
        return var1.is(var0);
    }

    @Override
    public boolean isBonemealSuccess(Level param0, Random param1, BlockPos param2, BlockState param3) {
        return (double)param1.nextFloat() < 0.4;
    }

    @Override
    public void performBonemeal(ServerLevel param0, Random param1, BlockPos param2, BlockState param3) {
        this.feature.get().value().place(param0, param0.getChunkSource().getGenerator(), param1, param2);
    }
}
