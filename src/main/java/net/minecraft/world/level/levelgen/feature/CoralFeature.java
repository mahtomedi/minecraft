package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.BaseCoralWallFanBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public abstract class CoralFeature extends Feature<NoneFeatureConfiguration> {
    public CoralFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> param0) {
        super(param0);
    }

    public boolean place(
        WorldGenLevel param0, StructureFeatureManager param1, ChunkGenerator param2, Random param3, BlockPos param4, NoneFeatureConfiguration param5
    ) {
        BlockState var0 = BlockTags.CORAL_BLOCKS.getRandomElement(param3).defaultBlockState();
        return this.placeFeature(param0, param3, param4, var0);
    }

    protected abstract boolean placeFeature(LevelAccessor var1, Random var2, BlockPos var3, BlockState var4);

    protected boolean placeCoralBlock(LevelAccessor param0, Random param1, BlockPos param2, BlockState param3) {
        BlockPos var0 = param2.above();
        BlockState var1 = param0.getBlockState(param2);
        if ((var1.is(Blocks.WATER) || var1.is(BlockTags.CORALS)) && param0.getBlockState(var0).is(Blocks.WATER)) {
            param0.setBlock(param2, param3, 3);
            if (param1.nextFloat() < 0.25F) {
                param0.setBlock(var0, BlockTags.CORALS.getRandomElement(param1).defaultBlockState(), 2);
            } else if (param1.nextFloat() < 0.05F) {
                param0.setBlock(var0, Blocks.SEA_PICKLE.defaultBlockState().setValue(SeaPickleBlock.PICKLES, Integer.valueOf(param1.nextInt(4) + 1)), 2);
            }

            for(Direction var2 : Direction.Plane.HORIZONTAL) {
                if (param1.nextFloat() < 0.2F) {
                    BlockPos var3 = param2.relative(var2);
                    if (param0.getBlockState(var3).is(Blocks.WATER)) {
                        BlockState var4 = BlockTags.WALL_CORALS.getRandomElement(param1).defaultBlockState().setValue(BaseCoralWallFanBlock.FACING, var2);
                        param0.setBlock(var3, var4, 2);
                    }
                }
            }

            return true;
        } else {
            return false;
        }
    }
}
