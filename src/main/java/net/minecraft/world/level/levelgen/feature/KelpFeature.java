package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.KelpBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class KelpFeature extends Feature<NoneFeatureConfiguration> {
    public KelpFeature(Codec<NoneFeatureConfiguration> param0) {
        super(param0);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> param0) {
        int var0 = 0;
        WorldGenLevel var1 = param0.level();
        BlockPos var2 = param0.origin();
        RandomSource var3 = param0.random();
        int var4 = var1.getHeight(Heightmap.Types.OCEAN_FLOOR, var2.getX(), var2.getZ());
        BlockPos var5 = new BlockPos(var2.getX(), var4, var2.getZ());
        if (var1.getBlockState(var5).is(Blocks.WATER)) {
            BlockState var6 = Blocks.KELP.defaultBlockState();
            BlockState var7 = Blocks.KELP_PLANT.defaultBlockState();
            int var8 = 1 + var3.nextInt(10);

            for(int var9 = 0; var9 <= var8; ++var9) {
                if (var1.getBlockState(var5).is(Blocks.WATER) && var1.getBlockState(var5.above()).is(Blocks.WATER) && var7.canSurvive(var1, var5)) {
                    if (var9 == var8) {
                        var1.setBlock(var5, var6.setValue(KelpBlock.AGE, Integer.valueOf(var3.nextInt(4) + 20)), 2);
                        ++var0;
                    } else {
                        var1.setBlock(var5, var7, 2);
                    }
                } else if (var9 > 0) {
                    BlockPos var10 = var5.below();
                    if (var6.canSurvive(var1, var10) && !var1.getBlockState(var10.below()).is(Blocks.KELP)) {
                        var1.setBlock(var10, var6.setValue(KelpBlock.AGE, Integer.valueOf(var3.nextInt(4) + 20)), 2);
                        ++var0;
                    }
                    break;
                }

                var5 = var5.above();
            }
        }

        return var0 > 0;
    }
}
