package net.minecraft.world.level.levelgen.synth;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.ints.IntBidirectionalIterator;
import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.RandomSource;

public class PerlinNoise {
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

    @Deprecated
    public static PerlinNoise createLegacy(RandomSource param0, int param1, double param2, double... param3) {
        DoubleArrayList var0 = new DoubleArrayList(param3);
        var0.add(0, param2);
        return createLegacy(param0, param1, var0);
    }

    @Deprecated
    public static PerlinNoise createLegacy(RandomSource param0, int param1, DoubleList param2) {
        return new PerlinNoise(param0, Pair.of(param1, param2));
    }

    public static PerlinNoise create(RandomSource param0, IntStream param1) {
        return create(param0, param1.boxed().collect(ImmutableList.toImmutableList()));
    }

    public static PerlinNoise create(RandomSource param0, List<Integer> param1) {
        return new PerlinNoise(param0, makeAmplitudes(new IntRBTreeSet(param1)), true);
    }

    public static PerlinNoise create(RandomSource param0, int param1, double param2, double... param3) {
        DoubleArrayList var0 = new DoubleArrayList(param3);
        var0.add(0, param2);
        return new PerlinNoise(param0, Pair.of(param1, var0), true);
    }

    public static PerlinNoise create(RandomSource param0, int param1, DoubleList param2) {
        return new PerlinNoise(param0, Pair.of(param1, param2), true);
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
        this(param0, makeAmplitudes(param1), false);
    }

    protected PerlinNoise(RandomSource param0, Pair<Integer, DoubleList> param1) {
        this(param0, param1, false);
    }

    protected PerlinNoise(RandomSource param0, Pair<Integer, DoubleList> param1, boolean param2) {
        this.firstOctave = param1.getFirst();
        this.amplitudes = param1.getSecond();
        int var0 = this.amplitudes.size();
        int var1 = -this.firstOctave;
        this.noiseLevels = new ImprovedNoise[var0];
        if (param2) {
            PositionalRandomFactory var2 = param0.forkPositional();

            for(int var3 = 0; var3 < var0; ++var3) {
                if (this.amplitudes.getDouble(var3) != 0.0) {
                    int var4 = this.firstOctave + var3;
                    this.noiseLevels[var3] = new ImprovedNoise(var2.fromHashOf("octave_" + var4));
                }
            }
        } else {
            ImprovedNoise var5 = new ImprovedNoise(param0);
            if (var1 >= 0 && var1 < var0) {
                double var6 = this.amplitudes.getDouble(var1);
                if (var6 != 0.0) {
                    this.noiseLevels[var1] = var5;
                }
            }

            for(int var7 = var1 - 1; var7 >= 0; --var7) {
                if (var7 < var0) {
                    double var8 = this.amplitudes.getDouble(var7);
                    if (var8 != 0.0) {
                        this.noiseLevels[var7] = new ImprovedNoise(param0);
                    } else {
                        skipOctave(param0);
                    }
                } else {
                    skipOctave(param0);
                }
            }

            if (Arrays.stream(this.noiseLevels).filter(Objects::nonNull).count() != this.amplitudes.stream().filter(param0x -> param0x != 0.0).count()) {
                throw new IllegalStateException("Failed to create correct number of noise levels for given non-zero amplitudes");
            }

            if (var1 < var0 - 1) {
                throw new IllegalArgumentException("Positive octaves are temporarily disabled");
            }
        }

        this.lowestFreqInputFactor = Math.pow(2.0, (double)(-var1));
        this.lowestFreqValueFactor = Math.pow(2.0, (double)(var0 - 1)) / (Math.pow(2.0, (double)var0) - 1.0);
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

    protected int firstOctave() {
        return this.firstOctave;
    }

    protected DoubleList amplitudes() {
        return this.amplitudes;
    }

    @VisibleForTesting
    public void parityConfigString(StringBuilder param0) {
        param0.append("PerlinNoise{");
        List<String> var0 = this.amplitudes.stream().map(param0x -> String.format("%.2f", param0x)).toList();
        param0.append("first octave: ").append(this.firstOctave).append(", amplitudes: ").append(var0).append(", noise levels: [");

        for(int var1 = 0; var1 < this.noiseLevels.length; ++var1) {
            param0.append(var1).append(": ");
            ImprovedNoise var2 = this.noiseLevels[var1];
            if (var2 == null) {
                param0.append("null");
            } else {
                var2.parityConfigString(param0);
            }

            param0.append(", ");
        }

        param0.append("]");
        param0.append("}");
    }
}
