package net.minecraft.world.level.levelgen.synth;

import com.google.common.annotations.VisibleForTesting;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public final class ImprovedNoise {
    private static final float SHIFT_UP_EPSILON = 1.0E-7F;
    private final byte[] p;
    public final double xo;
    public final double yo;
    public final double zo;

    public ImprovedNoise(RandomSource param0) {
        this.xo = param0.nextDouble() * 256.0;
        this.yo = param0.nextDouble() * 256.0;
        this.zo = param0.nextDouble() * 256.0;
        this.p = new byte[256];

        for(int var0 = 0; var0 < 256; ++var0) {
            this.p[var0] = (byte)var0;
        }

        for(int var1 = 0; var1 < 256; ++var1) {
            int var2 = param0.nextInt(256 - var1);
            byte var3 = this.p[var1];
            this.p[var1] = this.p[var1 + var2];
            this.p[var1 + var2] = var3;
        }

    }

    public double noise(double param0, double param1, double param2) {
        return this.noise(param0, param1, param2, 0.0, 0.0);
    }

    @Deprecated
    public double noise(double param0, double param1, double param2, double param3, double param4) {
        double var0 = param0 + this.xo;
        double var1 = param1 + this.yo;
        double var2 = param2 + this.zo;
        int var3 = Mth.floor(var0);
        int var4 = Mth.floor(var1);
        int var5 = Mth.floor(var2);
        double var6 = var0 - (double)var3;
        double var7 = var1 - (double)var4;
        double var8 = var2 - (double)var5;
        double var11;
        if (param3 != 0.0) {
            double var9;
            if (param4 >= 0.0 && param4 < var7) {
                var9 = param4;
            } else {
                var9 = var7;
            }

            var11 = (double)Mth.floor(var9 / param3 + 1.0E-7F) * param3;
        } else {
            var11 = 0.0;
        }

        return this.sampleAndLerp(var3, var4, var5, var6, var7 - var11, var8, var7);
    }

    public double noiseWithDerivative(double param0, double param1, double param2, double[] param3) {
        double var0 = param0 + this.xo;
        double var1 = param1 + this.yo;
        double var2 = param2 + this.zo;
        int var3 = Mth.floor(var0);
        int var4 = Mth.floor(var1);
        int var5 = Mth.floor(var2);
        double var6 = var0 - (double)var3;
        double var7 = var1 - (double)var4;
        double var8 = var2 - (double)var5;
        return this.sampleWithDerivative(var3, var4, var5, var6, var7, var8, param3);
    }

    private static double gradDot(int param0, double param1, double param2, double param3) {
        return SimplexNoise.dot(SimplexNoise.GRADIENT[param0 & 15], param1, param2, param3);
    }

    private int p(int param0) {
        return this.p[param0 & 0xFF] & 0xFF;
    }

    private double sampleAndLerp(int param0, int param1, int param2, double param3, double param4, double param5, double param6) {
        int var0 = this.p(param0);
        int var1 = this.p(param0 + 1);
        int var2 = this.p(var0 + param1);
        int var3 = this.p(var0 + param1 + 1);
        int var4 = this.p(var1 + param1);
        int var5 = this.p(var1 + param1 + 1);
        double var6 = gradDot(this.p(var2 + param2), param3, param4, param5);
        double var7 = gradDot(this.p(var4 + param2), param3 - 1.0, param4, param5);
        double var8 = gradDot(this.p(var3 + param2), param3, param4 - 1.0, param5);
        double var9 = gradDot(this.p(var5 + param2), param3 - 1.0, param4 - 1.0, param5);
        double var10 = gradDot(this.p(var2 + param2 + 1), param3, param4, param5 - 1.0);
        double var11 = gradDot(this.p(var4 + param2 + 1), param3 - 1.0, param4, param5 - 1.0);
        double var12 = gradDot(this.p(var3 + param2 + 1), param3, param4 - 1.0, param5 - 1.0);
        double var13 = gradDot(this.p(var5 + param2 + 1), param3 - 1.0, param4 - 1.0, param5 - 1.0);
        double var14 = Mth.smoothstep(param3);
        double var15 = Mth.smoothstep(param6);
        double var16 = Mth.smoothstep(param5);
        return Mth.lerp3(var14, var15, var16, var6, var7, var8, var9, var10, var11, var12, var13);
    }

    private double sampleWithDerivative(int param0, int param1, int param2, double param3, double param4, double param5, double[] param6) {
        int var0 = this.p(param0);
        int var1 = this.p(param0 + 1);
        int var2 = this.p(var0 + param1);
        int var3 = this.p(var0 + param1 + 1);
        int var4 = this.p(var1 + param1);
        int var5 = this.p(var1 + param1 + 1);
        int var6 = this.p(var2 + param2);
        int var7 = this.p(var4 + param2);
        int var8 = this.p(var3 + param2);
        int var9 = this.p(var5 + param2);
        int var10 = this.p(var2 + param2 + 1);
        int var11 = this.p(var4 + param2 + 1);
        int var12 = this.p(var3 + param2 + 1);
        int var13 = this.p(var5 + param2 + 1);
        int[] var14 = SimplexNoise.GRADIENT[var6 & 15];
        int[] var15 = SimplexNoise.GRADIENT[var7 & 15];
        int[] var16 = SimplexNoise.GRADIENT[var8 & 15];
        int[] var17 = SimplexNoise.GRADIENT[var9 & 15];
        int[] var18 = SimplexNoise.GRADIENT[var10 & 15];
        int[] var19 = SimplexNoise.GRADIENT[var11 & 15];
        int[] var20 = SimplexNoise.GRADIENT[var12 & 15];
        int[] var21 = SimplexNoise.GRADIENT[var13 & 15];
        double var22 = SimplexNoise.dot(var14, param3, param4, param5);
        double var23 = SimplexNoise.dot(var15, param3 - 1.0, param4, param5);
        double var24 = SimplexNoise.dot(var16, param3, param4 - 1.0, param5);
        double var25 = SimplexNoise.dot(var17, param3 - 1.0, param4 - 1.0, param5);
        double var26 = SimplexNoise.dot(var18, param3, param4, param5 - 1.0);
        double var27 = SimplexNoise.dot(var19, param3 - 1.0, param4, param5 - 1.0);
        double var28 = SimplexNoise.dot(var20, param3, param4 - 1.0, param5 - 1.0);
        double var29 = SimplexNoise.dot(var21, param3 - 1.0, param4 - 1.0, param5 - 1.0);
        double var30 = Mth.smoothstep(param3);
        double var31 = Mth.smoothstep(param4);
        double var32 = Mth.smoothstep(param5);
        double var33 = Mth.lerp3(
            var30,
            var31,
            var32,
            (double)var14[0],
            (double)var15[0],
            (double)var16[0],
            (double)var17[0],
            (double)var18[0],
            (double)var19[0],
            (double)var20[0],
            (double)var21[0]
        );
        double var34 = Mth.lerp3(
            var30,
            var31,
            var32,
            (double)var14[1],
            (double)var15[1],
            (double)var16[1],
            (double)var17[1],
            (double)var18[1],
            (double)var19[1],
            (double)var20[1],
            (double)var21[1]
        );
        double var35 = Mth.lerp3(
            var30,
            var31,
            var32,
            (double)var14[2],
            (double)var15[2],
            (double)var16[2],
            (double)var17[2],
            (double)var18[2],
            (double)var19[2],
            (double)var20[2],
            (double)var21[2]
        );
        double var36 = Mth.lerp2(var31, var32, var23 - var22, var25 - var24, var27 - var26, var29 - var28);
        double var37 = Mth.lerp2(var32, var30, var24 - var22, var28 - var26, var25 - var23, var29 - var27);
        double var38 = Mth.lerp2(var30, var31, var26 - var22, var27 - var23, var28 - var24, var29 - var25);
        double var39 = Mth.smoothstepDerivative(param3);
        double var40 = Mth.smoothstepDerivative(param4);
        double var41 = Mth.smoothstepDerivative(param5);
        double var42 = var33 + var39 * var36;
        double var43 = var34 + var40 * var37;
        double var44 = var35 + var41 * var38;
        param6[0] += var42;
        param6[1] += var43;
        param6[2] += var44;
        return Mth.lerp3(var30, var31, var32, var22, var23, var24, var25, var26, var27, var28, var29);
    }

    @VisibleForTesting
    public void parityConfigString(StringBuilder param0) {
        NoiseUtils.parityNoiseOctaveConfigString(param0, this.xo, this.yo, this.zo, this.p);
    }
}
