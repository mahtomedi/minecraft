package net.minecraft.world.level.levelgen;

import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.levelgen.synth.BlendedNoise;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;

public class NoiseSampler {
    private static final float[] BIOME_WEIGHTS = Util.make(new float[25], param0 -> {
        for(int var0 = -2; var0 <= 2; ++var0) {
            for(int var1 = -2; var1 <= 2; ++var1) {
                float var2 = 10.0F / Mth.sqrt((float)(var0 * var0 + var1 * var1) + 0.2F);
                param0[var0 + 2 + (var1 + 2) * 5] = var2;
            }
        }

    });
    private final BiomeSource biomeSource;
    private final int cellWidth;
    private final int cellHeight;
    private final int cellCountY;
    private final NoiseSettings noiseSettings;
    private final BlendedNoise blendedNoise;
    @Nullable
    private final SimplexNoise islandNoise;
    private final PerlinNoise depthNoise;
    private final double topSlideTarget;
    private final double topSlideSize;
    private final double topSlideOffset;
    private final double bottomSlideTarget;
    private final double bottomSlideSize;
    private final double bottomSlideOffset;
    private final double dimensionDensityFactor;
    private final double dimensionDensityOffset;

    public NoiseSampler(
        BiomeSource param0, int param1, int param2, int param3, NoiseSettings param4, BlendedNoise param5, @Nullable SimplexNoise param6, PerlinNoise param7
    ) {
        this.cellWidth = param1;
        this.cellHeight = param2;
        this.biomeSource = param0;
        this.cellCountY = param3;
        this.noiseSettings = param4;
        this.blendedNoise = param5;
        this.islandNoise = param6;
        this.depthNoise = param7;
        this.topSlideTarget = (double)param4.topSlideSettings().target();
        this.topSlideSize = (double)param4.topSlideSettings().size();
        this.topSlideOffset = (double)param4.topSlideSettings().offset();
        this.bottomSlideTarget = (double)param4.bottomSlideSettings().target();
        this.bottomSlideSize = (double)param4.bottomSlideSettings().size();
        this.bottomSlideOffset = (double)param4.bottomSlideSettings().offset();
        this.dimensionDensityFactor = param4.densityFactor();
        this.dimensionDensityOffset = param4.densityOffset();
    }

    public void fillNoiseColumn(double[] param0, int param1, int param2, NoiseSettings param3, int param4, int param5, int param6) {
        double var0;
        double var1;
        if (this.islandNoise != null) {
            var0 = (double)(TheEndBiomeSource.getHeightValue(this.islandNoise, param1, param2) - 8.0F);
            if (var0 > 0.0) {
                var1 = 0.25;
            } else {
                var1 = 1.0;
            }
        } else {
            float var3 = 0.0F;
            float var4 = 0.0F;
            float var5 = 0.0F;
            int var6 = 2;
            int var7 = param4;
            float var8 = this.biomeSource.getNoiseBiome(param1, param4, param2).getDepth();

            for(int var9 = -2; var9 <= 2; ++var9) {
                for(int var10 = -2; var10 <= 2; ++var10) {
                    Biome var11 = this.biomeSource.getNoiseBiome(param1 + var9, var7, param2 + var10);
                    float var12 = var11.getDepth();
                    float var13 = var11.getScale();
                    float var14;
                    float var15;
                    if (param3.isAmplified() && var12 > 0.0F) {
                        var14 = 1.0F + var12 * 2.0F;
                        var15 = 1.0F + var13 * 4.0F;
                    } else {
                        var14 = var12;
                        var15 = var13;
                    }

                    float var18 = var12 > var8 ? 0.5F : 1.0F;
                    float var19 = var18 * BIOME_WEIGHTS[var9 + 2 + (var10 + 2) * 5] / (var14 + 2.0F);
                    var3 += var15 * var19;
                    var4 += var14 * var19;
                    var5 += var19;
                }
            }

            float var20 = var4 / var5;
            float var21 = var3 / var5;
            double var22 = (double)(var20 * 0.5F - 0.125F);
            double var23 = (double)(var21 * 0.9F + 0.1F);
            var0 = var22 * 0.265625;
            var1 = 96.0 / var23;
        }

        double var26 = 684.412 * param3.noiseSamplingSettings().xzScale();
        double var27 = 684.412 * param3.noiseSamplingSettings().yScale();
        double var28 = var26 / param3.noiseSamplingSettings().xzFactor();
        double var29 = var27 / param3.noiseSamplingSettings().yFactor();
        double var30 = param3.randomDensityOffset() ? this.getRandomDensity(param1, param2) : 0.0;

        for(int var31 = 0; var31 <= param6; ++var31) {
            int var32 = var31 + param5;
            double var33 = this.blendedNoise.sampleAndClampNoise(param1, var32, param2, var26, var27, var28, var29);
            double var34 = this.computeInitialDensity(var32, var0, var1, var30) + var33;
            var34 = this.applySlide(var34, var32);
            param0[var31] = var34;
        }

    }

    private double computeInitialDensity(int param0, double param1, double param2, double param3) {
        double var0 = 1.0 - (double)param0 * 2.0 / 32.0 + param3;
        double var1 = var0 * this.dimensionDensityFactor + this.dimensionDensityOffset;
        double var2 = (var1 + param1) * param2;
        return var2 * (double)(var2 > 0.0 ? 4 : 1);
    }

    private double applySlide(double param0, int param1) {
        int var0 = Mth.intFloorDiv(this.noiseSettings.minY(), this.cellHeight);
        int var1 = param1 - var0;
        if (this.topSlideSize > 0.0) {
            double var2 = ((double)(this.cellCountY - var1) - this.topSlideOffset) / this.topSlideSize;
            param0 = Mth.clampedLerp(this.topSlideTarget, param0, var2);
        }

        if (this.bottomSlideSize > 0.0) {
            double var3 = ((double)var1 - this.bottomSlideOffset) / this.bottomSlideSize;
            param0 = Mth.clampedLerp(this.bottomSlideTarget, param0, var3);
        }

        return param0;
    }

    private double getRandomDensity(int param0, int param1) {
        double var0 = this.depthNoise.getValue((double)(param0 * 200), 10.0, (double)(param1 * 200), 1.0, 0.0, true);
        double var1;
        if (var0 < 0.0) {
            var1 = -var0 * 0.3;
        } else {
            var1 = var0;
        }

        double var3 = var1 * 24.575625 - 2.0;
        return var3 < 0.0 ? var3 * 0.009486607142857142 : Math.min(var3, 1.0) * 0.006640625;
    }
}
