package net.minecraft.world.level.levelgen.synth;

import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.doubles.DoubleListIterator;
import net.minecraft.world.level.levelgen.WorldgenRandom;

public class NormalNoise {
    private final double valueFactor;
    private final PerlinNoise first;
    private final PerlinNoise second;

    public static NormalNoise create(WorldgenRandom param0, int param1, DoubleList param2) {
        return new NormalNoise(param0, param1, param2);
    }

    private NormalNoise(WorldgenRandom param0, int param1, DoubleList param2) {
        this.first = PerlinNoise.create(param0, param1, param2);
        this.second = PerlinNoise.create(param0, param1, param2);
        int var0 = Integer.MAX_VALUE;
        int var1 = Integer.MIN_VALUE;
        DoubleListIterator var2 = param2.iterator();

        while(var2.hasNext()) {
            int var3 = var2.nextIndex();
            double var4 = var2.nextDouble();
            if (var4 != 0.0) {
                var0 = Math.min(var0, var3);
                var1 = Math.max(var1, var3);
            }
        }

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
