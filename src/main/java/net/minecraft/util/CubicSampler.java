package net.minecraft.util;

import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CubicSampler {
    private static final double[] GAUSSIAN_SAMPLE_KERNEL = new double[]{0.0, 1.0, 4.0, 6.0, 4.0, 1.0, 0.0};

    @OnlyIn(Dist.CLIENT)
    public static Vec3 gaussianSampleVec3(Vec3 param0, CubicSampler.Vec3Fetcher param1) {
        int var0 = Mth.floor(param0.x());
        int var1 = Mth.floor(param0.y());
        int var2 = Mth.floor(param0.z());
        double var3 = param0.x() - (double)var0;
        double var4 = param0.y() - (double)var1;
        double var5 = param0.z() - (double)var2;
        double var6 = 0.0;
        Vec3 var7 = Vec3.ZERO;

        for(int var8 = 0; var8 < 6; ++var8) {
            double var9 = Mth.lerp(var3, GAUSSIAN_SAMPLE_KERNEL[var8 + 1], GAUSSIAN_SAMPLE_KERNEL[var8]);
            int var10 = var0 - 2 + var8;

            for(int var11 = 0; var11 < 6; ++var11) {
                double var12 = Mth.lerp(var4, GAUSSIAN_SAMPLE_KERNEL[var11 + 1], GAUSSIAN_SAMPLE_KERNEL[var11]);
                int var13 = var1 - 2 + var11;

                for(int var14 = 0; var14 < 6; ++var14) {
                    double var15 = Mth.lerp(var5, GAUSSIAN_SAMPLE_KERNEL[var14 + 1], GAUSSIAN_SAMPLE_KERNEL[var14]);
                    int var16 = var2 - 2 + var14;
                    double var17 = var9 * var12 * var15;
                    var6 += var17;
                    var7 = var7.add(param1.fetch(var10, var13, var16).scale(var17));
                }
            }
        }

        return var7.scale(1.0 / var6);
    }

    public interface Vec3Fetcher {
        Vec3 fetch(int var1, int var2, int var3);
    }
}
