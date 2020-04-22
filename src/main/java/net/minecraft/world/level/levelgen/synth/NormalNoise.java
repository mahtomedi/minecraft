package net.minecraft.world.level.levelgen.synth;

import java.util.List;
import net.minecraft.world.level.levelgen.WorldgenRandom;

public class NormalNoise {
    private final double valueFactor;
    private final PerlinNoise first;
    private final PerlinNoise second;

    public NormalNoise(WorldgenRandom param0, List<Integer> param1) {
        this.first = new PerlinNoise(param0, param1);
        this.second = new PerlinNoise(param0, param1);
        int var0 = param1.stream().min(Integer::compareTo).orElse(0);
        int var1 = param1.stream().max(Integer::compareTo).orElse(0);
        this.valueFactor = 0.16666666666666666 / expectedDeviation(var1 - var0);
    }

    private static double expectedDeviation(int param0) {
        return 0.1 * (1.0 + 1.0 / (double)(param0 + 1));
    }

    public double getValue(double param0, double param1, double param2) {
        double var0 = param0 * 1.0181268882175227;
        double var1 = param1 * 1.0181268882175227;
        double var2 = param2 * 1.0181268882175227;
        return (this.first.getValue(param0, param1, param2) + this.second.getValue(var0, var1, var2)) * this.valueFactor;
    }
}
