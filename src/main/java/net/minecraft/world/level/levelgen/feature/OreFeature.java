package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.BitSet;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;

public class OreFeature extends Feature<OreConfiguration> {
    public OreFeature(Function<Dynamic<?>, ? extends OreConfiguration> param0, Function<Random, ? extends OreConfiguration> param1) {
        super(param0, param1);
    }

    public boolean place(LevelAccessor param0, ChunkGenerator<? extends ChunkGeneratorSettings> param1, Random param2, BlockPos param3, OreConfiguration param4) {
        float var0 = param2.nextFloat() * (float) Math.PI;
        float var1 = (float)param4.size / 8.0F;
        int var2 = Mth.ceil(((float)param4.size / 16.0F * 2.0F + 1.0F) / 2.0F);
        double var3 = (double)((float)param3.getX() + Mth.sin(var0) * var1);
        double var4 = (double)((float)param3.getX() - Mth.sin(var0) * var1);
        double var5 = (double)((float)param3.getZ() + Mth.cos(var0) * var1);
        double var6 = (double)((float)param3.getZ() - Mth.cos(var0) * var1);
        int var7 = 2;
        double var8 = (double)(param3.getY() + param2.nextInt(3) - 2);
        double var9 = (double)(param3.getY() + param2.nextInt(3) - 2);
        int var10 = param3.getX() - Mth.ceil(var1) - var2;
        int var11 = param3.getY() - 2 - var2;
        int var12 = param3.getZ() - Mth.ceil(var1) - var2;
        int var13 = 2 * (Mth.ceil(var1) + var2);
        int var14 = 2 * (2 + var2);

        for(int var15 = var10; var15 <= var10 + var13; ++var15) {
            for(int var16 = var12; var16 <= var12 + var13; ++var16) {
                if (var11 <= param0.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, var15, var16)) {
                    return this.doPlace(param0, param2, param4, var3, var4, var5, var6, var8, var9, var10, var11, var12, var13, var14);
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
        double[] var3 = new double[param2.size * 4];

        for(int var4 = 0; var4 < param2.size; ++var4) {
            float var5 = (float)var4 / (float)param2.size;
            double var6 = Mth.lerp((double)var5, param3, param4);
            double var7 = Mth.lerp((double)var5, param7, param8);
            double var8 = Mth.lerp((double)var5, param5, param6);
            double var9 = param1.nextDouble() * (double)param2.size / 16.0;
            double var10 = ((double)(Mth.sin((float) Math.PI * var5) + 1.0F) * var9 + 1.0) / 2.0;
            var3[var4 * 4 + 0] = var6;
            var3[var4 * 4 + 1] = var7;
            var3[var4 * 4 + 2] = var8;
            var3[var4 * 4 + 3] = var10;
        }

        for(int var11 = 0; var11 < param2.size - 1; ++var11) {
            if (!(var3[var11 * 4 + 3] <= 0.0)) {
                for(int var12 = var11 + 1; var12 < param2.size; ++var12) {
                    if (!(var3[var12 * 4 + 3] <= 0.0)) {
                        double var13 = var3[var11 * 4 + 0] - var3[var12 * 4 + 0];
                        double var14 = var3[var11 * 4 + 1] - var3[var12 * 4 + 1];
                        double var15 = var3[var11 * 4 + 2] - var3[var12 * 4 + 2];
                        double var16 = var3[var11 * 4 + 3] - var3[var12 * 4 + 3];
                        if (var16 * var16 > var13 * var13 + var14 * var14 + var15 * var15) {
                            if (var16 > 0.0) {
                                var3[var12 * 4 + 3] = -1.0;
                            } else {
                                var3[var11 * 4 + 3] = -1.0;
                            }
                        }
                    }
                }
            }
        }

        for(int var17 = 0; var17 < param2.size; ++var17) {
            double var18 = var3[var17 * 4 + 3];
            if (!(var18 < 0.0)) {
                double var19 = var3[var17 * 4 + 0];
                double var20 = var3[var17 * 4 + 1];
                double var21 = var3[var17 * 4 + 2];
                int var22 = Math.max(Mth.floor(var19 - var18), param9);
                int var23 = Math.max(Mth.floor(var20 - var18), param10);
                int var24 = Math.max(Mth.floor(var21 - var18), param11);
                int var25 = Math.max(Mth.floor(var19 + var18), var22);
                int var26 = Math.max(Mth.floor(var20 + var18), var23);
                int var27 = Math.max(Mth.floor(var21 + var18), var24);

                for(int var28 = var22; var28 <= var25; ++var28) {
                    double var29 = ((double)var28 + 0.5 - var19) / var18;
                    if (var29 * var29 < 1.0) {
                        for(int var30 = var23; var30 <= var26; ++var30) {
                            double var31 = ((double)var30 + 0.5 - var20) / var18;
                            if (var29 * var29 + var31 * var31 < 1.0) {
                                for(int var32 = var24; var32 <= var27; ++var32) {
                                    double var33 = ((double)var32 + 0.5 - var21) / var18;
                                    if (var29 * var29 + var31 * var31 + var33 * var33 < 1.0) {
                                        int var34 = var28 - param9 + (var30 - param10) * param12 + (var32 - param11) * param12 * param13;
                                        if (!var1.get(var34)) {
                                            var1.set(var34);
                                            var2.set(var28, var30, var32);
                                            if (param2.target.getPredicate().test(param0.getBlockState(var2))) {
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
