package net.minecraft.world.level.levelgen.synth;

import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.RandomSource;

public final class ImprovedNoise {
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
}
