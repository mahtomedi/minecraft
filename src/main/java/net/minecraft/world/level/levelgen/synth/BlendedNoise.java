package net.minecraft.world.level.levelgen.synth;

import java.util.stream.IntStream;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.RandomSource;

public class BlendedNoise {
    private final PerlinNoise minLimitNoise;
    private final PerlinNoise maxLimitNoise;
    private final PerlinNoise mainNoise;

    public BlendedNoise(PerlinNoise param0, PerlinNoise param1, PerlinNoise param2) {
        this.minLimitNoise = param0;
        this.maxLimitNoise = param1;
        this.mainNoise = param2;
    }

    public BlendedNoise(RandomSource param0) {
        this(
            new PerlinNoise(param0, IntStream.rangeClosed(-15, 0)),
            new PerlinNoise(param0, IntStream.rangeClosed(-15, 0)),
            new PerlinNoise(param0, IntStream.rangeClosed(-7, 0))
        );
    }

    public double sampleAndClampNoise(int param0, int param1, int param2, double param3, double param4, double param5, double param6) {
        double var0 = 0.0;
        double var1 = 0.0;
        double var2 = 0.0;
        boolean var3 = true;
        double var4 = 1.0;

        for(int var5 = 0; var5 < 8; ++var5) {
            ImprovedNoise var6 = this.mainNoise.getOctaveNoise(var5);
            if (var6 != null) {
                var2 += var6.noise(
                        PerlinNoise.wrap((double)param0 * param5 * var4),
                        PerlinNoise.wrap((double)param1 * param6 * var4),
                        PerlinNoise.wrap((double)param2 * param5 * var4),
                        param6 * var4,
                        (double)param1 * param6 * var4
                    )
                    / var4;
            }

            var4 /= 2.0;
        }

        double var7 = (var2 / 10.0 + 1.0) / 2.0;
        boolean var8 = var7 >= 1.0;
        boolean var9 = var7 <= 0.0;
        var4 = 1.0;

        for(int var10 = 0; var10 < 16; ++var10) {
            double var11 = PerlinNoise.wrap((double)param0 * param3 * var4);
            double var12 = PerlinNoise.wrap((double)param1 * param4 * var4);
            double var13 = PerlinNoise.wrap((double)param2 * param3 * var4);
            double var14 = param4 * var4;
            if (!var8) {
                ImprovedNoise var15 = this.minLimitNoise.getOctaveNoise(var10);
                if (var15 != null) {
                    var0 += var15.noise(var11, var12, var13, var14, (double)param1 * var14) / var4;
                }
            }

            if (!var9) {
                ImprovedNoise var16 = this.maxLimitNoise.getOctaveNoise(var10);
                if (var16 != null) {
                    var1 += var16.noise(var11, var12, var13, var14, (double)param1 * var14) / var4;
                }
            }

            var4 /= 2.0;
        }

        return Mth.clampedLerp(var0 / 512.0, var1 / 512.0, var7);
    }
}
