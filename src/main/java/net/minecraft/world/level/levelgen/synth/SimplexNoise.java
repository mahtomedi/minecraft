package net.minecraft.world.level.levelgen.synth;

import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.RandomSource;

public class SimplexNoise {
    protected static final int[][] GRADIENT = new int[][]{
        {1, 1, 0},
        {-1, 1, 0},
        {1, -1, 0},
        {-1, -1, 0},
        {1, 0, 1},
        {-1, 0, 1},
        {1, 0, -1},
        {-1, 0, -1},
        {0, 1, 1},
        {0, -1, 1},
        {0, 1, -1},
        {0, -1, -1},
        {1, 1, 0},
        {0, -1, 1},
        {-1, 1, 0},
        {0, -1, -1}
    };
    private static final double SQRT_3 = Math.sqrt(3.0);
    private static final double F2 = 0.5 * (SQRT_3 - 1.0);
    private static final double G2 = (3.0 - SQRT_3) / 6.0;
    private final int[] p = new int[512];
    public final double xo;
    public final double yo;
    public final double zo;

    public SimplexNoise(RandomSource param0) {
        this.xo = param0.nextDouble() * 256.0;
        this.yo = param0.nextDouble() * 256.0;
        this.zo = param0.nextDouble() * 256.0;
        int var0 = 0;

        while(var0 < 256) {
            this.p[var0] = var0++;
        }

        for(int var1 = 0; var1 < 256; ++var1) {
            int var2 = param0.nextInt(256 - var1);
            int var3 = this.p[var1];
            this.p[var1] = this.p[var2 + var1];
            this.p[var2 + var1] = var3;
        }

    }

    private int p(int param0) {
        return this.p[param0 & 0xFF];
    }

    protected static double dot(int[] param0, double param1, double param2, double param3) {
        return (double)param0[0] * param1 + (double)param0[1] * param2 + (double)param0[2] * param3;
    }

    private double getCornerNoise3D(int param0, double param1, double param2, double param3, double param4) {
        double var0 = param4 - param1 * param1 - param2 * param2 - param3 * param3;
        double var1;
        if (var0 < 0.0) {
            var1 = 0.0;
        } else {
            var0 *= var0;
            var1 = var0 * var0 * dot(GRADIENT[param0], param1, param2, param3);
        }

        return var1;
    }

    public double getValue(double param0, double param1) {
        double var0 = (param0 + param1) * F2;
        int var1 = Mth.floor(param0 + var0);
        int var2 = Mth.floor(param1 + var0);
        double var3 = (double)(var1 + var2) * G2;
        double var4 = (double)var1 - var3;
        double var5 = (double)var2 - var3;
        double var6 = param0 - var4;
        double var7 = param1 - var5;
        int var8;
        int var9;
        if (var6 > var7) {
            var8 = 1;
            var9 = 0;
        } else {
            var8 = 0;
            var9 = 1;
        }

        double var12 = var6 - (double)var8 + G2;
        double var13 = var7 - (double)var9 + G2;
        double var14 = var6 - 1.0 + 2.0 * G2;
        double var15 = var7 - 1.0 + 2.0 * G2;
        int var16 = var1 & 0xFF;
        int var17 = var2 & 0xFF;
        int var18 = this.p(var16 + this.p(var17)) % 12;
        int var19 = this.p(var16 + var8 + this.p(var17 + var9)) % 12;
        int var20 = this.p(var16 + 1 + this.p(var17 + 1)) % 12;
        double var21 = this.getCornerNoise3D(var18, var6, var7, 0.0, 0.5);
        double var22 = this.getCornerNoise3D(var19, var12, var13, 0.0, 0.5);
        double var23 = this.getCornerNoise3D(var20, var14, var15, 0.0, 0.5);
        return 70.0 * (var21 + var22 + var23);
    }

    public double getValue(double param0, double param1, double param2) {
        double var0 = 0.3333333333333333;
        double var1 = (param0 + param1 + param2) * 0.3333333333333333;
        int var2 = Mth.floor(param0 + var1);
        int var3 = Mth.floor(param1 + var1);
        int var4 = Mth.floor(param2 + var1);
        double var5 = 0.16666666666666666;
        double var6 = (double)(var2 + var3 + var4) * 0.16666666666666666;
        double var7 = (double)var2 - var6;
        double var8 = (double)var3 - var6;
        double var9 = (double)var4 - var6;
        double var10 = param0 - var7;
        double var11 = param1 - var8;
        double var12 = param2 - var9;
        int var13;
        int var14;
        int var15;
        int var16;
        int var17;
        int var18;
        if (var10 >= var11) {
            if (var11 >= var12) {
                var13 = 1;
                var14 = 0;
                var15 = 0;
                var16 = 1;
                var17 = 1;
                var18 = 0;
            } else if (var10 >= var12) {
                var13 = 1;
                var14 = 0;
                var15 = 0;
                var16 = 1;
                var17 = 0;
                var18 = 1;
            } else {
                var13 = 0;
                var14 = 0;
                var15 = 1;
                var16 = 1;
                var17 = 0;
                var18 = 1;
            }
        } else if (var11 < var12) {
            var13 = 0;
            var14 = 0;
            var15 = 1;
            var16 = 0;
            var17 = 1;
            var18 = 1;
        } else if (var10 < var12) {
            var13 = 0;
            var14 = 1;
            var15 = 0;
            var16 = 0;
            var17 = 1;
            var18 = 1;
        } else {
            var13 = 0;
            var14 = 1;
            var15 = 0;
            var16 = 1;
            var17 = 1;
            var18 = 0;
        }

        double var49 = var10 - (double)var13 + 0.16666666666666666;
        double var50 = var11 - (double)var14 + 0.16666666666666666;
        double var51 = var12 - (double)var15 + 0.16666666666666666;
        double var52 = var10 - (double)var16 + 0.3333333333333333;
        double var53 = var11 - (double)var17 + 0.3333333333333333;
        double var54 = var12 - (double)var18 + 0.3333333333333333;
        double var55 = var10 - 1.0 + 0.5;
        double var56 = var11 - 1.0 + 0.5;
        double var57 = var12 - 1.0 + 0.5;
        int var58 = var2 & 0xFF;
        int var59 = var3 & 0xFF;
        int var60 = var4 & 0xFF;
        int var61 = this.p(var58 + this.p(var59 + this.p(var60))) % 12;
        int var62 = this.p(var58 + var13 + this.p(var59 + var14 + this.p(var60 + var15))) % 12;
        int var63 = this.p(var58 + var16 + this.p(var59 + var17 + this.p(var60 + var18))) % 12;
        int var64 = this.p(var58 + 1 + this.p(var59 + 1 + this.p(var60 + 1))) % 12;
        double var65 = this.getCornerNoise3D(var61, var10, var11, var12, 0.6);
        double var66 = this.getCornerNoise3D(var62, var49, var50, var51, 0.6);
        double var67 = this.getCornerNoise3D(var63, var52, var53, var54, 0.6);
        double var68 = this.getCornerNoise3D(var64, var55, var56, var57, 0.6);
        return 32.0 * (var65 + var66 + var67 + var68);
    }
}
