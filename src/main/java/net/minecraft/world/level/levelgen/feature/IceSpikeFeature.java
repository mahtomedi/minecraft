package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class IceSpikeFeature extends Feature<NoneFeatureConfiguration> {
    public IceSpikeFeature(Codec<NoneFeatureConfiguration> param0) {
        super(param0);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> param0) {
        BlockPos var0 = param0.origin();
        Random var1 = param0.random();
        WorldGenLevel var2 = param0.level();

        while(var2.isEmptyBlock(var0) && var0.getY() > var2.getMinBuildHeight() + 2) {
            var0 = var0.below();
        }

        if (!var2.getBlockState(var0).is(Blocks.SNOW_BLOCK)) {
            return false;
        } else {
            var0 = var0.above(var1.nextInt(4));
            int var3 = var1.nextInt(4) + 7;
            int var4 = var3 / 4 + var1.nextInt(2);
            if (var4 > 1 && var1.nextInt(60) == 0) {
                var0 = var0.above(10 + var1.nextInt(30));
            }

            for(int var5 = 0; var5 < var3; ++var5) {
                float var6 = (1.0F - (float)var5 / (float)var3) * (float)var4;
                int var7 = Mth.ceil(var6);

                for(int var8 = -var7; var8 <= var7; ++var8) {
                    float var9 = (float)Mth.abs(var8) - 0.25F;

                    for(int var10 = -var7; var10 <= var7; ++var10) {
                        float var11 = (float)Mth.abs(var10) - 0.25F;
                        if ((var8 == 0 && var10 == 0 || !(var9 * var9 + var11 * var11 > var6 * var6))
                            && (var8 != -var7 && var8 != var7 && var10 != -var7 && var10 != var7 || !(var1.nextFloat() > 0.75F))) {
                            BlockState var12 = var2.getBlockState(var0.offset(var8, var5, var10));
                            if (var12.isAir() || isDirt(var12) || var12.is(Blocks.SNOW_BLOCK) || var12.is(Blocks.ICE)) {
                                this.setBlock(var2, var0.offset(var8, var5, var10), Blocks.PACKED_ICE.defaultBlockState());
                            }

                            if (var5 != 0 && var7 > 1) {
                                var12 = var2.getBlockState(var0.offset(var8, -var5, var10));
                                if (var12.isAir() || isDirt(var12) || var12.is(Blocks.SNOW_BLOCK) || var12.is(Blocks.ICE)) {
                                    this.setBlock(var2, var0.offset(var8, -var5, var10), Blocks.PACKED_ICE.defaultBlockState());
                                }
                            }
                        }
                    }
                }
            }

            int var13 = var4 - 1;
            if (var13 < 0) {
                var13 = 0;
            } else if (var13 > 1) {
                var13 = 1;
            }

            for(int var14 = -var13; var14 <= var13; ++var14) {
                for(int var15 = -var13; var15 <= var13; ++var15) {
                    BlockPos var16 = var0.offset(var14, -1, var15);
                    int var17 = 50;
                    if (Math.abs(var14) == 1 && Math.abs(var15) == 1) {
                        var17 = var1.nextInt(5);
                    }

                    while(var16.getY() > 50) {
                        BlockState var18 = var2.getBlockState(var16);
                        if (!var18.isAir() && !isDirt(var18) && !var18.is(Blocks.SNOW_BLOCK) && !var18.is(Blocks.ICE) && !var18.is(Blocks.PACKED_ICE)) {
                            break;
                        }

                        this.setBlock(var2, var16, Blocks.PACKED_ICE.defaultBlockState());
                        var16 = var16.below();
                        if (--var17 <= 0) {
                            var16 = var16.below(var1.nextInt(5) + 1);
                            var17 = var1.nextInt(5);
                        }
                    }
                }
            }

            return true;
        }
    }
}
