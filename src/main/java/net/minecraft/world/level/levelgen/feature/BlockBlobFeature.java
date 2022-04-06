package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;

public class BlockBlobFeature extends Feature<BlockStateConfiguration> {
    public BlockBlobFeature(Codec<BlockStateConfiguration> param0) {
        super(param0);
    }

    @Override
    public boolean place(FeaturePlaceContext<BlockStateConfiguration> param0) {
        BlockPos var0 = param0.origin();
        WorldGenLevel var1 = param0.level();
        RandomSource var2 = param0.random();

        BlockStateConfiguration var3;
        for(var3 = param0.config(); var0.getY() > var1.getMinBuildHeight() + 3; var0 = var0.below()) {
            if (!var1.isEmptyBlock(var0.below())) {
                BlockState var4 = var1.getBlockState(var0.below());
                if (isDirt(var4) || isStone(var4)) {
                    break;
                }
            }
        }

        if (var0.getY() <= var1.getMinBuildHeight() + 3) {
            return false;
        } else {
            for(int var5 = 0; var5 < 3; ++var5) {
                int var6 = var2.nextInt(2);
                int var7 = var2.nextInt(2);
                int var8 = var2.nextInt(2);
                float var9 = (float)(var6 + var7 + var8) * 0.333F + 0.5F;

                for(BlockPos var10 : BlockPos.betweenClosed(var0.offset(-var6, -var7, -var8), var0.offset(var6, var7, var8))) {
                    if (var10.distSqr(var0) <= (double)(var9 * var9)) {
                        var1.setBlock(var10, var3.state, 4);
                    }
                }

                var0 = var0.offset(-1 + var2.nextInt(2), -var2.nextInt(2), -1 + var2.nextInt(2));
            }

            return true;
        }
    }
}
