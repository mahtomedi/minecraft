package net.minecraft.world.level.levelgen.synth;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.doubles.DoubleListIterator;
import java.util.List;
import net.minecraft.world.level.levelgen.RandomSource;

public class NormalNoise {
    private static final double INPUT_FACTOR = 1.0181268882175227;
    private static final double TARGET_DEVIATION = 0.3333333333333333;
    private final double valueFactor;
    private final PerlinNoise first;
    private final PerlinNoise second;

    public static NormalNoise create(RandomSource param0, int param1, double... param2) {
        return new NormalNoise(param0, param1, new DoubleArrayList(param2));
    }

    public static NormalNoise create(RandomSource param0, NormalNoise.NoiseParameters param1) {
        return create(param0, param1.firstOctave(), param1.amplitudes());
    }

    public static NormalNoise create(RandomSource param0, int param1, DoubleList param2) {
        return new NormalNoise(param0, param1, param2);
    }

    private NormalNoise(RandomSource param0, int param1, DoubleList param2) {
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

    public NormalNoise.NoiseParameters parameters() {
        return new NormalNoise.NoiseParameters(this.first.firstOctave(), this.first.amplitudes());
    }

    public static class NoiseParameters {
        private final int firstOctave;
        private final DoubleList amplitudes;
        public static final Codec<NormalNoise.NoiseParameters> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(
                        Codec.INT.fieldOf("firstOctave").forGetter(NormalNoise.NoiseParameters::firstOctave),
                        Codec.DOUBLE.listOf().fieldOf("amplitudes").forGetter(NormalNoise.NoiseParameters::amplitudes)
                    )
                    .apply(param0, NormalNoise.NoiseParameters::new)
        );

        public NoiseParameters(int param0, List<Double> param1) {
            this.firstOctave = param0;
            this.amplitudes = new DoubleArrayList(param1);
        }

        public NoiseParameters(int param0, double... param1) {
            this.firstOctave = param0;
            this.amplitudes = new DoubleArrayList(param1);
        }

        public int firstOctave() {
            return this.firstOctave;
        }

        public DoubleList amplitudes() {
            return this.amplitudes;
        }
    }
}
