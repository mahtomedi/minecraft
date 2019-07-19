package net.minecraft.world.level.levelgen.synth;

import java.util.Random;
import net.minecraft.util.Mth;

public class PerlinNoise implements SurfaceNoise {
    private final ImprovedNoise[] noiseLevels;

    public PerlinNoise(Random param0, int param1) {
        this.noiseLevels = new ImprovedNoise[param1];

        for(int var0 = 0; var0 < param1; ++var0) {
            this.noiseLevels[var0] = new ImprovedNoise(param0);
        }

    }

    public double getValue(double param0, double param1, double param2) {
        return this.getValue(param0, param1, param2, 0.0, 0.0, false);
    }

    public double getValue(double param0, double param1, double param2, double param3, double param4, boolean param5) {
        double var0 = 0.0;
        double var1 = 1.0;

        for(ImprovedNoise var2 : this.noiseLevels) {
            var0 += var2.noise(wrap(param0 * var1), param5 ? -var2.yo : wrap(param1 * var1), wrap(param2 * var1), param3 * var1, param4 * var1) / var1;
            var1 /= 2.0;
        }

        return var0;
    }

    public ImprovedNoise getOctaveNoise(int param0) {
        return this.noiseLevels[param0];
    }

    public static double wrap(double param0) {
        return param0 - (double)Mth.lfloor(param0 / 3.3554432E7 + 0.5) * 3.3554432E7;
    }

    @Override
    public double getSurfaceNoiseValue(double param0, double param1, double param2, double param3) {
        return this.getValue(param0, param1, 0.0, param2, param3, false);
    }
}
