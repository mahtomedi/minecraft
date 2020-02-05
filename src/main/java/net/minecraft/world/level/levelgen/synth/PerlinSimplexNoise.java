package net.minecraft.world.level.levelgen.synth;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import java.util.List;
import java.util.stream.IntStream;
import net.minecraft.world.level.levelgen.WorldgenRandom;

public class PerlinSimplexNoise implements SurfaceNoise {
    private final SimplexNoise[] noiseLevels;
    private final double highestFreqValueFactor;
    private final double highestFreqInputFactor;

    public PerlinSimplexNoise(WorldgenRandom param0, IntStream param1) {
        this(param0, param1.boxed().collect(ImmutableList.toImmutableList()));
    }

    public PerlinSimplexNoise(WorldgenRandom param0, List<Integer> param1) {
        this(param0, new IntRBTreeSet(param1));
    }

    private PerlinSimplexNoise(WorldgenRandom param0, IntSortedSet param1) {
        if (param1.isEmpty()) {
            throw new IllegalArgumentException("Need some octaves!");
        } else {
            int var0 = -param1.firstInt();
            int var1 = param1.lastInt();
            int var2 = var0 + var1 + 1;
            if (var2 < 1) {
                throw new IllegalArgumentException("Total number of octaves needs to be >= 1");
            } else {
                SimplexNoise var3 = new SimplexNoise(param0);
                int var4 = var1;
                this.noiseLevels = new SimplexNoise[var2];
                if (var1 >= 0 && var1 < var2 && param1.contains(0)) {
                    this.noiseLevels[var1] = var3;
                }

                for(int var5 = var1 + 1; var5 < var2; ++var5) {
                    if (var5 >= 0 && param1.contains(var4 - var5)) {
                        this.noiseLevels[var5] = new SimplexNoise(param0);
                    } else {
                        param0.consumeCount(262);
                    }
                }

                if (var1 > 0) {
                    long var6 = (long)(var3.getValue(var3.xo, var3.yo, var3.zo) * 9.223372E18F);
                    WorldgenRandom var7 = new WorldgenRandom(var6);

                    for(int var8 = var4 - 1; var8 >= 0; --var8) {
                        if (var8 < var2 && param1.contains(var4 - var8)) {
                            this.noiseLevels[var8] = new SimplexNoise(var7);
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

    public double getValue(double param0, double param1, boolean param2) {
        double var0 = 0.0;
        double var1 = this.highestFreqInputFactor;
        double var2 = this.highestFreqValueFactor;

        for(SimplexNoise var3 : this.noiseLevels) {
            if (var3 != null) {
                var0 += var3.getValue(param0 * var1 + (param2 ? var3.xo : 0.0), param1 * var1 + (param2 ? var3.yo : 0.0)) * var2;
            }

            var1 /= 2.0;
            var2 *= 2.0;
        }

        return var0;
    }

    @Override
    public double getSurfaceNoiseValue(double param0, double param1, double param2, double param3) {
        return this.getValue(param0, param1, true) * 0.55;
    }
}
