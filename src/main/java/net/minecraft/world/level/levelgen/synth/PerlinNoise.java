package net.minecraft.world.level.levelgen.synth;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.ints.IntBidirectionalIterator;
import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import java.util.List;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.WorldgenRandom;

public class PerlinNoise implements SurfaceNoise {
    private final ImprovedNoise[] noiseLevels;
    private final DoubleList amplitudes;
    private final double lowestFreqValueFactor;
    private final double lowestFreqInputFactor;

    public PerlinNoise(WorldgenRandom param0, IntStream param1) {
        this(param0, param1.boxed().collect(ImmutableList.toImmutableList()));
    }

    public PerlinNoise(WorldgenRandom param0, List<Integer> param1) {
        this(param0, new IntRBTreeSet(param1));
    }

    public static PerlinNoise create(WorldgenRandom param0, int param1, DoubleList param2) {
        return new PerlinNoise(param0, Pair.of(param1, param2));
    }

    private static Pair<Integer, DoubleList> makeAmplitudes(IntSortedSet param0) {
        if (param0.isEmpty()) {
            throw new IllegalArgumentException("Need some octaves!");
        } else {
            int var0 = -param0.firstInt();
            int var1 = param0.lastInt();
            int var2 = var0 + var1 + 1;
            if (var2 < 1) {
                throw new IllegalArgumentException("Total number of octaves needs to be >= 1");
            } else {
                DoubleList var3 = new DoubleArrayList(new double[var2]);
                IntBidirectionalIterator var4 = param0.iterator();

                while(var4.hasNext()) {
                    int var5 = var4.nextInt();
                    var3.set(var5 + var0, 1.0);
                }

                return Pair.of(-var0, var3);
            }
        }
    }

    private PerlinNoise(WorldgenRandom param0, IntSortedSet param1) {
        this(param0, makeAmplitudes(param1));
    }

    private PerlinNoise(WorldgenRandom param0, Pair<Integer, DoubleList> param1) {
        int var0 = param1.getFirst();
        this.amplitudes = param1.getSecond();
        ImprovedNoise var1 = new ImprovedNoise(param0);
        int var2 = this.amplitudes.size();
        int var3 = -var0;
        this.noiseLevels = new ImprovedNoise[var2];
        if (var3 >= 0 && var3 < var2) {
            double var4 = this.amplitudes.getDouble(var3);
            if (var4 != 0.0) {
                this.noiseLevels[var3] = var1;
            }
        }

        for(int var5 = var3 - 1; var5 >= 0; --var5) {
            if (var5 < var2) {
                double var6 = this.amplitudes.getDouble(var5);
                if (var6 != 0.0) {
                    this.noiseLevels[var5] = new ImprovedNoise(param0);
                } else {
                    param0.consumeCount(262);
                }
            } else {
                param0.consumeCount(262);
            }
        }

        if (var3 < var2 - 1) {
            long var7 = (long)(var1.noise(0.0, 0.0, 0.0, 0.0, 0.0) * 9.223372E18F);
            WorldgenRandom var8 = new WorldgenRandom(var7);

            for(int var9 = var3 + 1; var9 < var2; ++var9) {
                if (var9 >= 0) {
                    double var10 = this.amplitudes.getDouble(var9);
                    if (var10 != 0.0) {
                        this.noiseLevels[var9] = new ImprovedNoise(var8);
                    } else {
                        var8.consumeCount(262);
                    }
                } else {
                    var8.consumeCount(262);
                }
            }
        }

        this.lowestFreqInputFactor = Math.pow(2.0, (double)(-var3));
        this.lowestFreqValueFactor = Math.pow(2.0, (double)(var2 - 1)) / (Math.pow(2.0, (double)var2) - 1.0);
    }

    public double getValue(double param0, double param1, double param2) {
        return this.getValue(param0, param1, param2, 0.0, 0.0, false);
    }

    public double getValue(double param0, double param1, double param2, double param3, double param4, boolean param5) {
        double var0 = 0.0;
        double var1 = this.lowestFreqInputFactor;
        double var2 = this.lowestFreqValueFactor;

        for(int var3 = 0; var3 < this.noiseLevels.length; ++var3) {
            ImprovedNoise var4 = this.noiseLevels[var3];
            if (var4 != null) {
                var0 += this.amplitudes.getDouble(var3)
                    * var4.noise(wrap(param0 * var1), param5 ? -var4.yo : wrap(param1 * var1), wrap(param2 * var1), param3 * var1, param4 * var1)
                    * var2;
            }

            var1 *= 2.0;
            var2 /= 2.0;
        }

        return var0;
    }

    @Nullable
    public ImprovedNoise getOctaveNoise(int param0) {
        return this.noiseLevels[this.noiseLevels.length - 1 - param0];
    }

    public static double wrap(double param0) {
        return param0 - (double)Mth.lfloor(param0 / 3.3554432E7 + 0.5) * 3.3554432E7;
    }

    @Override
    public double getSurfaceNoiseValue(double param0, double param1, double param2, double param3) {
        return this.getValue(param0, param1, 0.0, param2, param3, false);
    }
}
