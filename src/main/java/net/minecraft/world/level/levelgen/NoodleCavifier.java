package net.minecraft.world.level.levelgen;

import java.util.Random;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.synth.NoiseUtils;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class NoodleCavifier {
    private static final int NOODLES_MAX_Y = 100;
    private static final double SPACING_AND_STRAIGHTNESS = 1.5;
    private static final double XZ_FREQUENCY = 2.6666666666666665;
    private static final double Y_FREQUENCY = 2.6666666666666665;
    private final NormalNoise toggleNoiseSource;
    private final NormalNoise thicknessNoiseSource;
    private final NormalNoise noodleANoiseSource;
    private final NormalNoise noodleBNoiseSource;

    public NoodleCavifier(long param0) {
        Random var0 = new Random(param0);
        this.toggleNoiseSource = NormalNoise.create(new SimpleRandomSource(var0.nextLong()), -8, 1.0);
        this.thicknessNoiseSource = NormalNoise.create(new SimpleRandomSource(var0.nextLong()), -8, 1.0);
        this.noodleANoiseSource = NormalNoise.create(new SimpleRandomSource(var0.nextLong()), -7, 1.0);
        this.noodleBNoiseSource = NormalNoise.create(new SimpleRandomSource(var0.nextLong()), -7, 1.0);
    }

    public void fillToggleNoiseColumn(double[] param0, int param1, int param2, int param3, int param4) {
        this.fillNoiseColumn(param0, param1, param2, param3, param4, this.toggleNoiseSource, 1.0);
    }

    public void fillThicknessNoiseColumn(double[] param0, int param1, int param2, int param3, int param4) {
        this.fillNoiseColumn(param0, param1, param2, param3, param4, this.thicknessNoiseSource, 1.0);
    }

    public void fillRidgeANoiseColumn(double[] param0, int param1, int param2, int param3, int param4) {
        this.fillNoiseColumn(param0, param1, param2, param3, param4, this.noodleANoiseSource, 2.6666666666666665, 2.6666666666666665);
    }

    public void fillRidgeBNoiseColumn(double[] param0, int param1, int param2, int param3, int param4) {
        this.fillNoiseColumn(param0, param1, param2, param3, param4, this.noodleBNoiseSource, 2.6666666666666665, 2.6666666666666665);
    }

    public void fillNoiseColumn(double[] param0, int param1, int param2, int param3, int param4, NormalNoise param5, double param6) {
        this.fillNoiseColumn(param0, param1, param2, param3, param4, param5, param6, param6);
    }

    public void fillNoiseColumn(double[] param0, int param1, int param2, int param3, int param4, NormalNoise param5, double param6, double param7) {
        int var0 = 8;
        int var1 = 4;

        for(int var2 = 0; var2 < param4; ++var2) {
            int var3 = var2 + param3;
            int var4 = param1 * 4;
            int var5 = var3 * 8;
            int var6 = param2 * 4;
            double var7;
            if (var5 < 108) {
                var7 = NoiseUtils.sampleNoiseAndMapToRange(param5, (double)var4 * param6, (double)var5 * param7, (double)var6 * param6, -1.0, 1.0);
            } else {
                var7 = 1.0;
            }

            param0[var2] = var7;
        }

    }

    public double noodleCavify(double param0, int param1, int param2, int param3, double param4, double param5, double param6, double param7, int param8) {
        if (param2 > 100 || param2 < param8 + 4) {
            return param0;
        } else if (param0 < 0.0) {
            return param0;
        } else if (param4 < 0.0) {
            return param0;
        } else {
            double var0 = 0.05;
            double var1 = 0.07;
            double var2 = Mth.clampedMap(param5, -1.0, 1.0, 0.05, 0.07);
            double var3 = Math.abs(1.5 * param6) - var2;
            double var4 = Math.abs(1.5 * param7) - var2;
            double var5 = Math.max(var3, var4);
            return Math.min(param0, var5);
        }
    }
}
