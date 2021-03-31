package net.minecraft.world.level.levelgen.synth;

import net.minecraft.util.Mth;

public class NoiseUtils {
    public static double sampleNoiseAndMapToRange(NormalNoise param0, double param1, double param2, double param3, double param4, double param5) {
        double var0 = param0.getValue(param1, param2, param3);
        return Mth.map(var0, -1.0, 1.0, param4, param5);
    }

    public static double biasTowardsExtreme(double param0, double param1) {
        return param0 + Math.sin(Math.PI * param0) * param1 / Math.PI;
    }
}
