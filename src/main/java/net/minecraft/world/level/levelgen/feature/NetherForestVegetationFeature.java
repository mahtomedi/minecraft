package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.NetherForestVegetationConfig;

public class NetherForestVegetationFeature extends Feature<NetherForestVegetationConfig> {
    public NetherForestVegetationFeature(Codec<NetherForestVegetationConfig> param0) {
        super(param0);
    }

    @Override
    public boolean place(FeaturePlaceContext<NetherForestVegetationConfig> param0) {
        WorldGenLevel var0 = param0.level();
        BlockPos var1 = param0.origin();
        BlockState var2 = var0.getBlockState(var1.below());
        NetherForestVegetationConfig var3 = param0.config();
        RandomSource var4 = param0.random();
        if (!var2.is(BlockTags.NYLIUM)) {
            return false;
        } else {
            int var5 = var1.getY();
            if (var5 >= var0.getMinBuildHeight() + 1 && var5 + 1 < var0.getMaxBuildHeight()) {
                int var6 = 0;

                for(int var7 = 0; var7 < var3.spreadWidth * var3.spreadWidth; ++var7) {
                    BlockPos var8 = var1.offset(
                        var4.nextInt(var3.spreadWidth) - var4.nextInt(var3.spreadWidth),
                        var4.nextInt(var3.spreadHeight) - var4.nextInt(var3.spreadHeight),
                        var4.nextInt(var3.spreadWidth) - var4.nextInt(var3.spreadWidth)
                    );
                    BlockState var9 = var3.stateProvider.getState(var4, var8);
                    if (var0.isEmptyBlock(var8) && var8.getY() > var0.getMinBuildHeight() && var9.canSurvive(var0, var8)) {
                        var0.setBlock(var8, var9, 2);
                        ++var6;
                    }
                }

                return var6 > 0;
            } else {
                return false;
            }
        }
    }
}
