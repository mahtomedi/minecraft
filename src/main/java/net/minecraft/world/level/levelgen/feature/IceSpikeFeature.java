package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class IceSpikeFeature extends Feature<NoneFeatureConfiguration> {
    public IceSpikeFeature(Codec<NoneFeatureConfiguration> param0) {
        super(param0);
    }

    public boolean place(WorldGenLevel param0, ChunkGenerator param1, Random param2, BlockPos param3, NoneFeatureConfiguration param4) {
        while(param0.isEmptyBlock(param3) && param3.getY() > param0.getMinBuildHeight() + 2) {
            param3 = param3.below();
        }

        if (!param0.getBlockState(param3).is(Blocks.SNOW_BLOCK)) {
            return false;
        } else {
            param3 = param3.above(param2.nextInt(4));
            int var0 = param2.nextInt(4) + 7;
            int var1 = var0 / 4 + param2.nextInt(2);
            if (var1 > 1 && param2.nextInt(60) == 0) {
                param3 = param3.above(10 + param2.nextInt(30));
            }

            for(int var2 = 0; var2 < var0; ++var2) {
                float var3 = (1.0F - (float)var2 / (float)var0) * (float)var1;
                int var4 = Mth.ceil(var3);

                for(int var5 = -var4; var5 <= var4; ++var5) {
                    float var6 = (float)Mth.abs(var5) - 0.25F;

                    for(int var7 = -var4; var7 <= var4; ++var7) {
                        float var8 = (float)Mth.abs(var7) - 0.25F;
                        if ((var5 == 0 && var7 == 0 || !(var6 * var6 + var8 * var8 > var3 * var3))
                            && (var5 != -var4 && var5 != var4 && var7 != -var4 && var7 != var4 || !(param2.nextFloat() > 0.75F))) {
                            BlockState var9 = param0.getBlockState(param3.offset(var5, var2, var7));
                            if (var9.isAir() || isDirt(var9) || var9.is(Blocks.SNOW_BLOCK) || var9.is(Blocks.ICE)) {
                                this.setBlock(param0, param3.offset(var5, var2, var7), Blocks.PACKED_ICE.defaultBlockState());
                            }

                            if (var2 != 0 && var4 > 1) {
                                var9 = param0.getBlockState(param3.offset(var5, -var2, var7));
                                if (var9.isAir() || isDirt(var9) || var9.is(Blocks.SNOW_BLOCK) || var9.is(Blocks.ICE)) {
                                    this.setBlock(param0, param3.offset(var5, -var2, var7), Blocks.PACKED_ICE.defaultBlockState());
                                }
                            }
                        }
                    }
                }
            }

            int var10 = var1 - 1;
            if (var10 < 0) {
                var10 = 0;
            } else if (var10 > 1) {
                var10 = 1;
            }

            for(int var11 = -var10; var11 <= var10; ++var11) {
                for(int var12 = -var10; var12 <= var10; ++var12) {
                    BlockPos var13 = param3.offset(var11, -1, var12);
                    int var14 = 50;
                    if (Math.abs(var11) == 1 && Math.abs(var12) == 1) {
                        var14 = param2.nextInt(5);
                    }

                    while(var13.getY() > 50) {
                        BlockState var15 = param0.getBlockState(var13);
                        if (!var15.isAir() && !isDirt(var15) && !var15.is(Blocks.SNOW_BLOCK) && !var15.is(Blocks.ICE) && !var15.is(Blocks.PACKED_ICE)) {
                            break;
                        }

                        this.setBlock(param0, var13, Blocks.PACKED_ICE.defaultBlockState());
                        var13 = var13.below();
                        if (--var14 <= 0) {
                            var13 = var13.below(param2.nextInt(5) + 1);
                            var14 = param2.nextInt(5);
                        }
                    }
                }
            }

            return true;
        }
    }
}
