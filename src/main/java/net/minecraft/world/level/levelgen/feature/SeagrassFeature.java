package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TallSeagrassBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;

public class SeagrassFeature extends Feature<ProbabilityFeatureConfiguration> {
    public SeagrassFeature(Codec<ProbabilityFeatureConfiguration> param0) {
        super(param0);
    }

    @Override
    public boolean place(FeaturePlaceContext<ProbabilityFeatureConfiguration> param0) {
        boolean var0 = false;
        RandomSource var1 = param0.random();
        WorldGenLevel var2 = param0.level();
        BlockPos var3 = param0.origin();
        ProbabilityFeatureConfiguration var4 = param0.config();
        int var5 = var1.nextInt(8) - var1.nextInt(8);
        int var6 = var1.nextInt(8) - var1.nextInt(8);
        int var7 = var2.getHeight(Heightmap.Types.OCEAN_FLOOR, var3.getX() + var5, var3.getZ() + var6);
        BlockPos var8 = new BlockPos(var3.getX() + var5, var7, var3.getZ() + var6);
        if (var2.getBlockState(var8).is(Blocks.WATER)) {
            boolean var9 = var1.nextDouble() < (double)var4.probability;
            BlockState var10 = var9 ? Blocks.TALL_SEAGRASS.defaultBlockState() : Blocks.SEAGRASS.defaultBlockState();
            if (var10.canSurvive(var2, var8)) {
                if (var9) {
                    BlockState var11 = var10.setValue(TallSeagrassBlock.HALF, DoubleBlockHalf.UPPER);
                    BlockPos var12 = var8.above();
                    if (var2.getBlockState(var12).is(Blocks.WATER)) {
                        var2.setBlock(var8, var10, 2);
                        var2.setBlock(var12, var11, 2);
                    }
                } else {
                    var2.setBlock(var8, var10, 2);
                }

                var0 = true;
            }
        }

        return var0;
    }
}
