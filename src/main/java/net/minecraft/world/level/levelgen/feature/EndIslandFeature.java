package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class EndIslandFeature extends Feature<NoneFeatureConfiguration> {
    public EndIslandFeature(Codec<NoneFeatureConfiguration> param0) {
        super(param0);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> param0) {
        WorldGenLevel var0 = param0.level();
        Random var1 = param0.random();
        BlockPos var2 = param0.origin();
        float var3 = (float)var1.nextInt(3) + 4.0F;

        for(int var4 = 0; var3 > 0.5F; --var4) {
            for(int var5 = Mth.floor(-var3); var5 <= Mth.ceil(var3); ++var5) {
                for(int var6 = Mth.floor(-var3); var6 <= Mth.ceil(var3); ++var6) {
                    if ((float)(var5 * var5 + var6 * var6) <= (var3 + 1.0F) * (var3 + 1.0F)) {
                        this.setBlock(var0, var2.offset(var5, var4, var6), Blocks.END_STONE.defaultBlockState());
                    }
                }
            }

            var3 -= (float)var1.nextInt(2) + 0.5F;
        }

        return true;
    }
}
