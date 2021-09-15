package net.minecraft.world.level.levelgen.synth;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.ints.IntBidirectionalIterator;
import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import java.util.List;
import java.util.function.LongFunction;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.RandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;

public class PerlinNoise implements SurfaceNoise {
    private static final int ROUND_OFF = 33554432;
    private final ImprovedNoise[] noiseLevels;
    private final int firstOctave;
    private final DoubleList amplitudes;
    private final double lowestFreqValueFactor;
    private final double lowestFreqInputFactor;

    public PerlinNoise(RandomSource param0, IntStream param1) {
        this(param0, param1.boxed().collect(ImmutableList.toImmutableList()));
    }

    public PerlinNoise(RandomSource param0, List<Integer> param1) {
        this(param0, new IntRBTreeSet(param1));
    }

    public static PerlinNoise create(RandomSource param0, int param1, double... param2) {
        return create(param0, param1, (DoubleList)(new DoubleArrayList(param2)));
    }

    public static PerlinNoise create(RandomSource param0, int param1, DoubleList param2) {
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

    private PerlinNoise(RandomSource param0, IntSortedSet param1) {
        this(param0, param1, WorldgenRandom::new);
    }

    private PerlinNoise(RandomSource param0, IntSortedSet param1, LongFunction<RandomSource> param2) {
        this(param0, makeAmplitudes(param1), param2);
    }

    protected PerlinNoise(RandomSource param0, Pair<Integer, DoubleList> param1) {
        this(param0, param1, WorldgenRandom::new);
    }

    protected PerlinNoise(RandomSource param0, Pair<Integer, DoubleList> param1, LongFunction<RandomSource> param2) {
        this.firstOctave = param1.getFirst();
        this.amplitudes = param1.getSecond();
        ImprovedNoise var0 = new ImprovedNoise(param0);
        int var1 = this.amplitudes.size();
        int var2 = -this.firstOctave;
        this.noiseLevels = new ImprovedNoise[var1];
        if (var2 >= 0 && var2 < var1) {
            double var3 = this.amplitudes.getDouble(var2);
            if (var3 != 0.0) {
                this.noiseLevels[var2] = var0;
            }
        }

        for(int var4 = var2 - 1; var4 >= 0; --var4) {
            if (var4 < var1) {
                double var5 = this.amplitudes.getDouble(var4);
                if (var5 != 0.0) {
                    this.noiseLevels[var4] = new ImprovedNoise(param0);
                } else {
                    skipOctave(param0);
                }
            } else {
                skipOctave(param0);
            }
        }

        if (var2 < var1 - 1) {
            throw new IllegalArgumentException("Positive octaves are temporarily disabled");
        } else {
            this.lowestFreqInputFactor = Math.pow(2.0, (double)(-var2));
            this.lowestFreqValueFactor = Math.pow(2.0, (double)(var1 - 1)) / (Math.pow(2.0, (double)var1) - 1.0);
        }
    }

    private static void skipOctave(RandomSource param0) {
        param0.consumeCount(262);
    }

    public double getValue(double param0, double param1, double param2) {
        return this.getValue(param0, param1, param2, 0.0, 0.0, false);
    }

    @Deprecated
    public double getValue(double param0, double param1, double param2, double param3, double param4, boolean param5) {
        double var0 = 0.0;
        double var1 = this.lowestFreqInputFactor;
        double var2 = this.lowestFreqValueFactor;

        for(int var3 = 0; var3 < this.noiseLevels.length; ++var3) {
            ImprovedNoise var4 = this.noiseLevels[var3];
            if (var4 != null) {
                double var5 = var4.noise(wrap(param0 * var1), param5 ? -var4.yo : wrap(param1 * var1), wrap(param2 * var1), param3 * var1, param4 * var1);
                var0 += this.amplitudes.getDouble(var3) * var5 * var2;
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

    protected int firstOctave() {
        return this.firstOctave;
    }

    protected DoubleList amplitudes() {
        return this.amplitudes;
    }
}
