package net.minecraft.world.level.levelgen.synth;

import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.WorldgenRandom;

public class PerlinNoise implements SurfaceNoise {
    private final ImprovedNoise[] noiseLevels;
    private final double highestFreqValueFactor;
    private final double highestFreqInputFactor;

    public PerlinNoise(WorldgenRandom param0, int param1, int param2) {
        this(param0, new IntRBTreeSet(IntStream.rangeClosed(-param1, param2).toArray()));
    }

    public PerlinNoise(WorldgenRandom param0, IntSortedSet param1) {
        if (param1.isEmpty()) {
            throw new IllegalArgumentException("Need some octaves!");
        } else {
            int var0 = -param1.firstInt();
            int var1 = param1.lastInt();
            int var2 = var0 + var1 + 1;
            if (var2 < 1) {
                throw new IllegalArgumentException("Total number of octaves needs to be >= 1");
            } else {
                ImprovedNoise var3 = new ImprovedNoise(param0);
                int var4 = var1;
                this.noiseLevels = new ImprovedNoise[var2];
                if (var1 >= 0 && var1 < var2 && param1.contains(0)) {
                    this.noiseLevels[var1] = var3;
                }

                for(int var5 = var1 + 1; var5 < var2; ++var5) {
                    if (var5 >= 0 && param1.contains(var4 - var5)) {
                        this.noiseLevels[var5] = new ImprovedNoise(param0);
                    } else {
                        param0.consumeCount(262);
                    }
                }

                if (var1 > 0) {
                    long var6 = (long)(var3.noise(0.0, 0.0, 0.0, 0.0, 0.0) * 9.223372E18F);
                    WorldgenRandom var7 = new WorldgenRandom(var6);

                    for(int var8 = var4 - 1; var8 >= 0; --var8) {
                        if (var8 < var2 && param1.contains(var4 - var8)) {
                            this.noiseLevels[var8] = new ImprovedNoise(var7);
                        } else {
                            var7.consumeCount(262);
                        }
                    }
                }

                this.highestFreqInputFactor = Math.pow(2.0, (double)var1);
                this.highestFreqValueFactor = 1.0 / (Math.pow(2.0, (double)var2) - 1.0);
            }
        }
    }

    public double getValue(double param0, double param1, double param2) {
        return this.getValue(param0, param1, param2, 0.0, 0.0, false);
    }

    public double getValue(double param0, double param1, double param2, double param3, double param4, boolean param5) {
        double var0 = 0.0;
        double var1 = this.highestFreqInputFactor;
        double var2 = this.highestFreqValueFactor;

        for(ImprovedNoise var3 : this.noiseLevels) {
            if (var3 != null) {
                var0 += var3.noise(wrap(param0 * var1), param5 ? -var3.yo : wrap(param1 * var1), wrap(param2 * var1), param3 * var1, param4 * var1) * var2;
            }

            var1 /= 2.0;
            var2 *= 2.0;
        }

        return var0;
    }

    @Nullable
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
