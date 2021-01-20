package net.minecraft.world.level.levelgen.synth;

import java.util.Random;
import net.minecraft.util.Mth;

public final class ImprovedNoise {
    private final byte[] p;
    public final double xo;
    public final double yo;
    public final double zo;

    public ImprovedNoise(Random param0) {
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
        double var9 = Mth.smoothstep(var6);
        double var10 = Mth.smoothstep(var7);
        double var11 = Mth.smoothstep(var8);
        double var13;
        if (param3 != 0.0) {
            double var12 = param4 < 0.0 ? 0.0 : Math.min(param4, var7);
            var13 = (double)Mth.floor(var12 / param3) * param3;
        } else {
            var13 = 0.0;
        }

        return this.sampleAndLerp(var3, var4, var5, var6, var7 - var13, var8, var9, var10, var11);
    }

    private static double gradDot(int param0, double param1, double param2, double param3) {
        int var0 = param0 & 15;
        return SimplexNoise.dot(SimplexNoise.GRADIENT[var0], param1, param2, param3);
    }

    private int p(int param0) {
        return this.p[param0 & 0xFF] & 0xFF;
    }

    public double sampleAndLerp(int param0, int param1, int param2, double param3, double param4, double param5, double param6, double param7, double param8) {
        int var0 = this.p(param0) + param1;
        int var1 = this.p(var0) + param2;
        int var2 = this.p(var0 + 1) + param2;
        int var3 = this.p(param0 + 1) + param1;
        int var4 = this.p(var3) + param2;
        int var5 = this.p(var3 + 1) + param2;
        double var6 = gradDot(this.p(var1), param3, param4, param5);
        double var7 = gradDot(this.p(var4), param3 - 1.0, param4, param5);
        double var8 = gradDot(this.p(var2), param3, param4 - 1.0, param5);
        double var9 = gradDot(this.p(var5), param3 - 1.0, param4 - 1.0, param5);
        double var10 = gradDot(this.p(var1 + 1), param3, param4, param5 - 1.0);
        double var11 = gradDot(this.p(var4 + 1), param3 - 1.0, param4, param5 - 1.0);
        double var12 = gradDot(this.p(var2 + 1), param3, param4 - 1.0, param5 - 1.0);
        double var13 = gradDot(this.p(var5 + 1), param3 - 1.0, param4 - 1.0, param5 - 1.0);
        return Mth.lerp3(param6, param7, param8, var6, var7, var8, var9, var10, var11, var12, var13);
    }
}
