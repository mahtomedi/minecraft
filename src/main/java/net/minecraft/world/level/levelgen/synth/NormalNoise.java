package net.minecraft.world.level.levelgen.synth;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.doubles.DoubleListIterator;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.util.RandomSource;

public class NormalNoise {
    private static final double INPUT_FACTOR = 1.0181268882175227;
    private static final double TARGET_DEVIATION = 0.3333333333333333;
    private final double valueFactor;
    private final PerlinNoise first;
    private final PerlinNoise second;
    private final double maxValue;
    private final NormalNoise.NoiseParameters parameters;

    @Deprecated
    public static NormalNoise createLegacyNetherBiome(RandomSource param0, NormalNoise.NoiseParameters param1) {
        return new NormalNoise(param0, param1, false);
    }

    public static NormalNoise create(RandomSource param0, int param1, double... param2) {
        return create(param0, new NormalNoise.NoiseParameters(param1, new DoubleArrayList(param2)));
    }

    public static NormalNoise create(RandomSource param0, NormalNoise.NoiseParameters param1) {
        return new NormalNoise(param0, param1, true);
    }

    private NormalNoise(RandomSource param0, NormalNoise.NoiseParameters param1, boolean param2) {
        int var0 = param1.firstOctave;
        DoubleList var1 = param1.amplitudes;
        this.parameters = param1;
        if (param2) {
            this.first = PerlinNoise.create(param0, var0, var1);
            this.second = PerlinNoise.create(param0, var0, var1);
        } else {
            this.first = PerlinNoise.createLegacyForLegacyNetherBiome(param0, var0, var1);
            this.second = PerlinNoise.createLegacyForLegacyNetherBiome(param0, var0, var1);
        }

        int var2 = Integer.MAX_VALUE;
        int var3 = Integer.MIN_VALUE;
        DoubleListIterator var4 = var1.iterator();

        while(var4.hasNext()) {
            int var5 = var4.nextIndex();
            double var6 = var4.nextDouble();
            if (var6 != 0.0) {
                var2 = Math.min(var2, var5);
                var3 = Math.max(var3, var5);
            }
        }

        this.valueFactor = 0.16666666666666666 / expectedDeviation(var3 - var2);
        this.maxValue = (this.first.maxValue() + this.second.maxValue()) * this.valueFactor;
    }

    public double maxValue() {
        return this.maxValue;
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
        return this.parameters;
    }

    @VisibleForTesting
    public void parityConfigString(StringBuilder param0) {
        param0.append("NormalNoise {");
        param0.append("first: ");
        this.first.parityConfigString(param0);
        param0.append(", second: ");
        this.second.parityConfigString(param0);
        param0.append("}");
    }

    public static record NoiseParameters(int firstOctave, DoubleList amplitudes) {
        public static final Codec<NormalNoise.NoiseParameters> DIRECT_CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(
                        Codec.INT.fieldOf("firstOctave").forGetter(NormalNoise.NoiseParameters::firstOctave),
                        Codec.DOUBLE.listOf().fieldOf("amplitudes").forGetter(NormalNoise.NoiseParameters::amplitudes)
                    )
                    .apply(param0, NormalNoise.NoiseParameters::new)
        );
        public static final Codec<Holder<NormalNoise.NoiseParameters>> CODEC = RegistryFileCodec.create(Registries.NOISE, DIRECT_CODEC);

        public NoiseParameters(int param0, List<Double> param1) {
            this(param0, new DoubleArrayList(param1));
        }

        public NoiseParameters(int param0, double param1, double... param2) {
            this(param0, Util.make(new DoubleArrayList(param2), param1x -> param1x.add(0, param1)));
        }
    }
}
