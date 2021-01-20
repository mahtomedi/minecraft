package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TallSeagrassBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;

public class SeagrassFeature extends Feature<ProbabilityFeatureConfiguration> {
    public SeagrassFeature(Codec<ProbabilityFeatureConfiguration> param0) {
        super(param0);
    }

    public boolean place(WorldGenLevel param0, ChunkGenerator param1, Random param2, BlockPos param3, ProbabilityFeatureConfiguration param4) {
        boolean var0 = false;
        int var1 = param2.nextInt(8) - param2.nextInt(8);
        int var2 = param2.nextInt(8) - param2.nextInt(8);
        int var3 = param0.getHeight(Heightmap.Types.OCEAN_FLOOR, param3.getX() + var1, param3.getZ() + var2);
        BlockPos var4 = new BlockPos(param3.getX() + var1, var3, param3.getZ() + var2);
        if (param0.getBlockState(var4).is(Blocks.WATER)) {
            boolean var5 = param2.nextDouble() < (double)param4.probability;
            BlockState var6 = var5 ? Blocks.TALL_SEAGRASS.defaultBlockState() : Blocks.SEAGRASS.defaultBlockState();
            if (var6.canSurvive(param0, var4)) {
                if (var5) {
                    BlockState var7 = var6.setValue(TallSeagrassBlock.HALF, DoubleBlockHalf.UPPER);
                    BlockPos var8 = var4.above();
                    if (param0.getBlockState(var8).is(Blocks.WATER)) {
                        param0.setBlock(var4, var6, 2);
                        param0.setBlock(var8, var7, 2);
                    }
                } else {
                    param0.setBlock(var4, var6, 2);
                }

                var0 = true;
            }
        }

        return var0;
    }
}
