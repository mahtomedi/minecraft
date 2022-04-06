package net.minecraft.world.level.levelgen.synth;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.IntStream;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;

public class BlendedNoise implements DensityFunction.SimpleFunction {
    private static final Codec<Double> SCALE_RANGE = Codec.doubleRange(0.001, 1000.0);
    private static final MapCodec<BlendedNoise> DATA_CODEC = RecordCodecBuilder.mapCodec(
        param0 -> param0.group(
                    SCALE_RANGE.fieldOf("xz_scale").forGetter(param0x -> param0x.xzScale),
                    SCALE_RANGE.fieldOf("y_scale").forGetter(param0x -> param0x.yScale),
                    SCALE_RANGE.fieldOf("xz_factor").forGetter(param0x -> param0x.xzFactor),
                    SCALE_RANGE.fieldOf("y_factor").forGetter(param0x -> param0x.yFactor),
                    Codec.doubleRange(1.0, 8.0).fieldOf("smear_scale_multiplier").forGetter(param0x -> param0x.smearScaleMultiplier)
                )
                .apply(param0, BlendedNoise::createUnseeded)
    );
    public static final KeyDispatchDataCodec<BlendedNoise> CODEC = KeyDispatchDataCodec.of(DATA_CODEC);
    private final PerlinNoise minLimitNoise;
    private final PerlinNoise maxLimitNoise;
    private final PerlinNoise mainNoise;
    private final double xzMultiplier;
    private final double yMultiplier;
    private final double xzFactor;
    private final double yFactor;
    private final double smearScaleMultiplier;
    private final double maxValue;
    private final double xzScale;
    private final double yScale;

    public static BlendedNoise createUnseeded(double param0, double param1, double param2, double param3, double param4) {
        return new BlendedNoise(new XoroshiroRandomSource(0L), param0, param1, param2, param3, param4);
    }

    private BlendedNoise(PerlinNoise param0, PerlinNoise param1, PerlinNoise param2, double param3, double param4, double param5, double param6, double param7) {
        this.minLimitNoise = param0;
        this.maxLimitNoise = param1;
        this.mainNoise = param2;
        this.xzScale = param3;
        this.yScale = param4;
        this.xzFactor = param5;
        this.yFactor = param6;
        this.smearScaleMultiplier = param7;
        this.xzMultiplier = 684.412 * this.xzScale;
        this.yMultiplier = 684.412 * this.yScale;
        this.maxValue = param0.maxBrokenValue(this.yMultiplier);
    }

    @VisibleForTesting
    public BlendedNoise(RandomSource param0, double param1, double param2, double param3, double param4, double param5) {
        this(
            PerlinNoise.createLegacyForBlendedNoise(param0, IntStream.rangeClosed(-15, 0)),
            PerlinNoise.createLegacyForBlendedNoise(param0, IntStream.rangeClosed(-15, 0)),
            PerlinNoise.createLegacyForBlendedNoise(param0, IntStream.rangeClosed(-7, 0)),
            param1,
            param2,
            param3,
            param4,
            param5
        );
    }

    public BlendedNoise withNewRandom(RandomSource param0) {
        return new BlendedNoise(param0, this.xzScale, this.yScale, this.xzFactor, this.yFactor, this.smearScaleMultiplier);
    }

    @Override
    public double compute(DensityFunction.FunctionContext param0) {
        double var0 = (double)param0.blockX() * this.xzMultiplier;
        double var1 = (double)param0.blockY() * this.yMultiplier;
        double var2 = (double)param0.blockZ() * this.xzMultiplier;
        double var3 = var0 / this.xzFactor;
        double var4 = var1 / this.yFactor;
        double var5 = var2 / this.xzFactor;
        double var6 = this.yMultiplier * this.smearScaleMultiplier;
        double var7 = var6 / this.yFactor;
        double var8 = 0.0;
        double var9 = 0.0;
        double var10 = 0.0;
        boolean var11 = true;
        double var12 = 1.0;

        for(int var13 = 0; var13 < 8; ++var13) {
            ImprovedNoise var14 = this.mainNoise.getOctaveNoise(var13);
            if (var14 != null) {
                var10 += var14.noise(PerlinNoise.wrap(var3 * var12), PerlinNoise.wrap(var4 * var12), PerlinNoise.wrap(var5 * var12), var7 * var12, var4 * var12)
                    / var12;
            }

            var12 /= 2.0;
        }

        double var15 = (var10 / 10.0 + 1.0) / 2.0;
        boolean var16 = var15 >= 1.0;
        boolean var17 = var15 <= 0.0;
        var12 = 1.0;

        for(int var18 = 0; var18 < 16; ++var18) {
            double var19 = PerlinNoise.wrap(var0 * var12);
            double var20 = PerlinNoise.wrap(var1 * var12);
            double var21 = PerlinNoise.wrap(var2 * var12);
            double var22 = var6 * var12;
            if (!var16) {
                ImprovedNoise var23 = this.minLimitNoise.getOctaveNoise(var18);
                if (var23 != null) {
                    var8 += var23.noise(var19, var20, var21, var22, var1 * var12) / var12;
                }
            }

            if (!var17) {
                ImprovedNoise var24 = this.maxLimitNoise.getOctaveNoise(var18);
                if (var24 != null) {
                    var9 += var24.noise(var19, var20, var21, var22, var1 * var12) / var12;
                }
            }

            var12 /= 2.0;
        }

        return Mth.clampedLerp(var8 / 512.0, var9 / 512.0, var15) / 128.0;
    }

    @Override
    public double minValue() {
        return -this.maxValue();
    }

    @Override
    public double maxValue() {
        return this.maxValue;
    }

    @VisibleForTesting
    public void parityConfigString(StringBuilder param0) {
        param0.append("BlendedNoise{minLimitNoise=");
        this.minLimitNoise.parityConfigString(param0);
        param0.append(", maxLimitNoise=");
        this.maxLimitNoise.parityConfigString(param0);
        param0.append(", mainNoise=");
        this.mainNoise.parityConfigString(param0);
        param0.append(
                String.format(
                    ", xzScale=%.3f, yScale=%.3f, xzMainScale=%.3f, yMainScale=%.3f, cellWidth=4, cellHeight=8",
                    684.412,
                    684.412,
                    8.555150000000001,
                    4.277575000000001
                )
            )
            .append('}');
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return CODEC;
    }
}
