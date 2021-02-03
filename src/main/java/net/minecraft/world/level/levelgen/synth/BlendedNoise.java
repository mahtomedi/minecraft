package net.minecraft.world.level.levelgen.synth;

import java.util.stream.IntStream;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.RandomSource;

public class BlendedNoise {
    private PerlinNoise minLimitNoise;
    private PerlinNoise maxLimitNoise;
    private PerlinNoise mainNoise;

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

        for(int var5 = 0; var5 < 16; ++var5) {
            double var6 = PerlinNoise.wrap((double)param0 * param3 * var4);
            double var7 = PerlinNoise.wrap((double)param1 * param4 * var4);
            double var8 = PerlinNoise.wrap((double)param2 * param3 * var4);
            double var9 = param4 * var4;
            ImprovedNoise var10 = this.minLimitNoise.getOctaveNoise(var5);
            if (var10 != null) {
                var0 += var10.noise(var6, var7, var8, var9, (double)param1 * var9) / var4;
            }

            ImprovedNoise var11 = this.maxLimitNoise.getOctaveNoise(var5);
            if (var11 != null) {
                var1 += var11.noise(var6, var7, var8, var9, (double)param1 * var9) / var4;
            }

            if (var5 < 8) {
                ImprovedNoise var12 = this.mainNoise.getOctaveNoise(var5);
                if (var12 != null) {
                    var2 += var12.noise(
                            PerlinNoise.wrap((double)param0 * param5 * var4),
                            PerlinNoise.wrap((double)param1 * param6 * var4),
                            PerlinNoise.wrap((double)param2 * param5 * var4),
                            param6 * var4,
                            (double)param1 * param6 * var4
                        )
                        / var4;
                }
            }

            var4 /= 2.0;
        }

        return Mth.clampedLerp(var0 / 512.0, var1 / 512.0, (var2 / 10.0 + 1.0) / 2.0);
    }
}
