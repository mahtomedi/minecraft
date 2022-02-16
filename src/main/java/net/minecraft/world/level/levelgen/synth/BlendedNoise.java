package net.minecraft.world.level.levelgen.synth;

import com.google.common.annotations.VisibleForTesting;
import java.util.stream.IntStream;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseSamplingSettings;
import net.minecraft.world.level.levelgen.RandomSource;

public class BlendedNoise implements DensityFunction.SimpleFunction {
    private final PerlinNoise minLimitNoise;
    private final PerlinNoise maxLimitNoise;
    private final PerlinNoise mainNoise;
    private final double xzScale;
    private final double yScale;
    private final double xzMainScale;
    private final double yMainScale;
    private final int cellWidth;
    private final int cellHeight;
    private final double maxValue;

    private BlendedNoise(PerlinNoise param0, PerlinNoise param1, PerlinNoise param2, NoiseSamplingSettings param3, int param4, int param5) {
        this.minLimitNoise = param0;
        this.maxLimitNoise = param1;
        this.mainNoise = param2;
        this.xzScale = 684.412 * param3.xzScale();
        this.yScale = 684.412 * param3.yScale();
        this.xzMainScale = this.xzScale / param3.xzFactor();
        this.yMainScale = this.yScale / param3.yFactor();
        this.cellWidth = param4;
        this.cellHeight = param5;
        this.maxValue = param0.maxBrokenValue(this.yScale);
    }

    public BlendedNoise(RandomSource param0, NoiseSamplingSettings param1, int param2, int param3) {
        this(
            PerlinNoise.createLegacyForBlendedNoise(param0, IntStream.rangeClosed(-15, 0)),
            PerlinNoise.createLegacyForBlendedNoise(param0, IntStream.rangeClosed(-15, 0)),
            PerlinNoise.createLegacyForBlendedNoise(param0, IntStream.rangeClosed(-7, 0)),
            param1,
            param2,
            param3
        );
    }

    @Override
    public double compute(DensityFunction.FunctionContext param0) {
        int var0 = Math.floorDiv(param0.blockX(), this.cellWidth);
        int var1 = Math.floorDiv(param0.blockY(), this.cellHeight);
        int var2 = Math.floorDiv(param0.blockZ(), this.cellWidth);
        double var3 = 0.0;
        double var4 = 0.0;
        double var5 = 0.0;
        boolean var6 = true;
        double var7 = 1.0;

        for(int var8 = 0; var8 < 8; ++var8) {
            ImprovedNoise var9 = this.mainNoise.getOctaveNoise(var8);
            if (var9 != null) {
                var5 += var9.noise(
                        PerlinNoise.wrap((double)var0 * this.xzMainScale * var7),
                        PerlinNoise.wrap((double)var1 * this.yMainScale * var7),
                        PerlinNoise.wrap((double)var2 * this.xzMainScale * var7),
                        this.yMainScale * var7,
                        (double)var1 * this.yMainScale * var7
                    )
                    / var7;
            }

            var7 /= 2.0;
        }

        double var10 = (var5 / 10.0 + 1.0) / 2.0;
        boolean var11 = var10 >= 1.0;
        boolean var12 = var10 <= 0.0;
        var7 = 1.0;

        for(int var13 = 0; var13 < 16; ++var13) {
            double var14 = PerlinNoise.wrap((double)var0 * this.xzScale * var7);
            double var15 = PerlinNoise.wrap((double)var1 * this.yScale * var7);
            double var16 = PerlinNoise.wrap((double)var2 * this.xzScale * var7);
            double var17 = this.yScale * var7;
            if (!var11) {
                ImprovedNoise var18 = this.minLimitNoise.getOctaveNoise(var13);
                if (var18 != null) {
                    var3 += var18.noise(var14, var15, var16, var17, (double)var1 * var17) / var7;
                }
            }

            if (!var12) {
                ImprovedNoise var19 = this.maxLimitNoise.getOctaveNoise(var13);
                if (var19 != null) {
                    var4 += var19.noise(var14, var15, var16, var17, (double)var1 * var17) / var7;
                }
            }

            var7 /= 2.0;
        }

        return Mth.clampedLerp(var3 / 512.0, var4 / 512.0, var10) / 128.0;
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
                    ", xzScale=%.3f, yScale=%.3f, xzMainScale=%.3f, yMainScale=%.3f, cellWidth=%d, cellHeight=%d",
                    this.xzScale,
                    this.yScale,
                    this.xzMainScale,
                    this.yMainScale,
                    this.cellWidth,
                    this.cellHeight
                )
            )
            .append('}');
    }
}
