package net.minecraft.world.level.levelgen.synth;

import java.util.Random;

public class PerlinSimplexNoise implements SurfaceNoise {
    private final SimplexNoise[] noiseLevels;
    private final int levels;

    public PerlinSimplexNoise(Random param0, int param1) {
        this.levels = param1;
        this.noiseLevels = new SimplexNoise[param1];

        for(int var0 = 0; var0 < param1; ++var0) {
            this.noiseLevels[var0] = new SimplexNoise(param0);
        }

    }

    public double getValue(double param0, double param1) {
        return this.getValue(param0, param1, false);
    }

    public double getValue(double param0, double param1, boolean param2) {
        double var0 = 0.0;
        double var1 = 1.0;

        for(int var2 = 0; var2 < this.levels; ++var2) {
            var0 += this.noiseLevels[var2]
                    .getValue(param0 * var1 + (param2 ? this.noiseLevels[var2].xo : 0.0), param1 * var1 + (param2 ? this.noiseLevels[var2].yo : 0.0))
                / var1;
            var1 /= 2.0;
        }

        return var0;
    }

    @Override
    public double getSurfaceNoiseValue(double param0, double param1, double param2, double param3) {
        return this.getValue(param0, param1, true) * 0.55;
    }
}
