package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.BaseCoralWallFanBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public abstract class CoralFeature extends Feature<NoneFeatureConfiguration> {
    public CoralFeature(Codec<NoneFeatureConfiguration> param0) {
        super(param0);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> param0) {
        Random var0 = param0.random();
        WorldGenLevel var1 = param0.level();
        BlockPos var2 = param0.origin();
        Optional<Block> var3 = Registry.BLOCK.getTag(BlockTags.CORAL_BLOCKS).flatMap(param1 -> param1.getRandomElement(var0)).map(Holder::value);
        return var3.isEmpty() ? false : this.placeFeature(var1, var0, var2, var3.get().defaultBlockState());
    }

    protected abstract boolean placeFeature(LevelAccessor var1, Random var2, BlockPos var3, BlockState var4);

    protected boolean placeCoralBlock(LevelAccessor param0, Random param1, BlockPos param2, BlockState param3) {
        BlockPos var0 = param2.above();
        BlockState var1 = param0.getBlockState(param2);
        if ((var1.is(Blocks.WATER) || var1.is(BlockTags.CORALS)) && param0.getBlockState(var0).is(Blocks.WATER)) {
            param0.setBlock(param2, param3, 3);
            if (param1.nextFloat() < 0.25F) {
                Registry.BLOCK
                    .getTag(BlockTags.CORALS)
                    .flatMap(param1x -> param1x.getRandomElement(param1))
                    .map(Holder::value)
                    .ifPresent(param2x -> param0.setBlock(var0, param2x.defaultBlockState(), 2));
            } else if (param1.nextFloat() < 0.05F) {
                param0.setBlock(var0, Blocks.SEA_PICKLE.defaultBlockState().setValue(SeaPickleBlock.PICKLES, Integer.valueOf(param1.nextInt(4) + 1)), 2);
            }

            for(Direction var2 : Direction.Plane.HORIZONTAL) {
                if (param1.nextFloat() < 0.2F) {
                    BlockPos var3 = param2.relative(var2);
                    if (param0.getBlockState(var3).is(Blocks.WATER)) {
                        Registry.BLOCK
                            .getTag(BlockTags.WALL_CORALS)
                            .flatMap(param1x -> param1x.getRandomElement(param1))
                            .map(Holder::value)
                            .ifPresent(param3x -> {
                                BlockState var0x = param3x.defaultBlockState();
                                if (var0x.hasProperty(BaseCoralWallFanBlock.FACING)) {
                                    var0x = var0x.setValue(BaseCoralWallFanBlock.FACING, var2);
                                }
    
                                param0.setBlock(var3, var0x, 2);
                            });
                    }
                }
            }

            return true;
        } else {
            return false;
        }
    }
}
