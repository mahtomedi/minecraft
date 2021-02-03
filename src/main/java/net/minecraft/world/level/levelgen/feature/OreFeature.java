package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.BitSet;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;

public class OreFeature extends Feature<OreConfiguration> {
    public OreFeature(Codec<OreConfiguration> param0) {
        super(param0);
    }

    @Override
    public boolean place(FeaturePlaceContext<OreConfiguration> param0) {
        Random var0 = param0.random();
        BlockPos var1 = param0.origin();
        WorldGenLevel var2 = param0.level();
        OreConfiguration var3 = param0.config();
        float var4 = var0.nextFloat() * (float) Math.PI;
        float var5 = (float)var3.size / 8.0F;
        int var6 = Mth.ceil(((float)var3.size / 16.0F * 2.0F + 1.0F) / 2.0F);
        double var7 = (double)var1.getX() + Math.sin((double)var4) * (double)var5;
        double var8 = (double)var1.getX() - Math.sin((double)var4) * (double)var5;
        double var9 = (double)var1.getZ() + Math.cos((double)var4) * (double)var5;
        double var10 = (double)var1.getZ() - Math.cos((double)var4) * (double)var5;
        int var11 = 2;
        double var12 = (double)(var1.getY() + var0.nextInt(3) - 2);
        double var13 = (double)(var1.getY() + var0.nextInt(3) - 2);
        int var14 = var1.getX() - Mth.ceil(var5) - var6;
        int var15 = var1.getY() - 2 - var6;
        int var16 = var1.getZ() - Mth.ceil(var5) - var6;
        int var17 = 2 * (Mth.ceil(var5) + var6);
        int var18 = 2 * (2 + var6);

        for(int var19 = var14; var19 <= var14 + var17; ++var19) {
            for(int var20 = var16; var20 <= var16 + var17; ++var20) {
                if (var15 <= var2.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, var19, var20)) {
                    return this.doPlace(var2, var0, var3, var7, var8, var9, var10, var12, var13, var14, var15, var16, var17, var18);
                }
            }
        }

        return false;
    }

    protected boolean doPlace(
        LevelAccessor param0,
        Random param1,
        OreConfiguration param2,
        double param3,
        double param4,
        double param5,
        double param6,
        double param7,
        double param8,
        int param9,
        int param10,
        int param11,
        int param12,
        int param13
    ) {
        int var0 = 0;
        BitSet var1 = new BitSet(param12 * param13 * param12);
        BlockPos.MutableBlockPos var2 = new BlockPos.MutableBlockPos();
        int var3 = param2.size;
        double[] var4 = new double[var3 * 4];

        for(int var5 = 0; var5 < var3; ++var5) {
            float var6 = (float)var5 / (float)var3;
            double var7 = Mth.lerp((double)var6, param3, param4);
            double var8 = Mth.lerp((double)var6, param7, param8);
            double var9 = Mth.lerp((double)var6, param5, param6);
            double var10 = param1.nextDouble() * (double)var3 / 16.0;
            double var11 = ((double)(Mth.sin((float) Math.PI * var6) + 1.0F) * var10 + 1.0) / 2.0;
            var4[var5 * 4 + 0] = var7;
            var4[var5 * 4 + 1] = var8;
            var4[var5 * 4 + 2] = var9;
            var4[var5 * 4 + 3] = var11;
        }

        for(int var12 = 0; var12 < var3 - 1; ++var12) {
            if (!(var4[var12 * 4 + 3] <= 0.0)) {
                for(int var13 = var12 + 1; var13 < var3; ++var13) {
                    if (!(var4[var13 * 4 + 3] <= 0.0)) {
                        double var14 = var4[var12 * 4 + 0] - var4[var13 * 4 + 0];
                        double var15 = var4[var12 * 4 + 1] - var4[var13 * 4 + 1];
                        double var16 = var4[var12 * 4 + 2] - var4[var13 * 4 + 2];
                        double var17 = var4[var12 * 4 + 3] - var4[var13 * 4 + 3];
                        if (var17 * var17 > var14 * var14 + var15 * var15 + var16 * var16) {
                            if (var17 > 0.0) {
                                var4[var13 * 4 + 3] = -1.0;
                            } else {
                                var4[var12 * 4 + 3] = -1.0;
                            }
                        }
                    }
                }
            }
        }

        for(int var18 = 0; var18 < var3; ++var18) {
            double var19 = var4[var18 * 4 + 3];
            if (!(var19 < 0.0)) {
                double var20 = var4[var18 * 4 + 0];
                double var21 = var4[var18 * 4 + 1];
                double var22 = var4[var18 * 4 + 2];
                int var23 = Math.max(Mth.floor(var20 - var19), param9);
                int var24 = Math.max(Mth.floor(var21 - var19), param10);
                int var25 = Math.max(Mth.floor(var22 - var19), param11);
                int var26 = Math.max(Mth.floor(var20 + var19), var23);
                int var27 = Math.max(Mth.floor(var21 + var19), var24);
                int var28 = Math.max(Mth.floor(var22 + var19), var25);

                for(int var29 = var23; var29 <= var26; ++var29) {
                    double var30 = ((double)var29 + 0.5 - var20) / var19;
                    if (var30 * var30 < 1.0) {
                        for(int var31 = var24; var31 <= var27; ++var31) {
                            double var32 = ((double)var31 + 0.5 - var21) / var19;
                            if (var30 * var30 + var32 * var32 < 1.0) {
                                for(int var33 = var25; var33 <= var28; ++var33) {
                                    double var34 = ((double)var33 + 0.5 - var22) / var19;
                                    if (var30 * var30 + var32 * var32 + var34 * var34 < 1.0) {
                                        int var35 = var29 - param9 + (var31 - param10) * param12 + (var33 - param11) * param12 * param13;
                                        if (!var1.get(var35)) {
                                            var1.set(var35);
                                            var2.set(var29, var31, var33);
                                            if (param2.target.test(param0.getBlockState(var2), param1)) {
                                                param0.setBlock(var2, param2.state, 2);
                                                ++var0;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return var0 > 0;
    }
}
