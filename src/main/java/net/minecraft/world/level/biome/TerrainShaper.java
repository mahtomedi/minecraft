package net.minecraft.world.level.biome;

import com.mojang.datafixers.util.Pair;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.CubicSpline;
import net.minecraft.util.Mth;
import net.minecraft.util.ToFloatFunction;
import net.minecraft.util.VisibleForDebug;

public final class TerrainShaper {
    private static final float GLOBAL_OFFSET = 0.015F;
    static final ToFloatFunction<TerrainShaper.Point> CONTINENTS_EXTRACTOR = new ToFloatFunction<TerrainShaper.Point>() {
        public float apply(TerrainShaper.Point param0) {
            return param0.continents;
        }

        @Override
        public String toString() {
            return "continents";
        }
    };
    static final ToFloatFunction<TerrainShaper.Point> EROSION_EXTRACTOR = new ToFloatFunction<TerrainShaper.Point>() {
        public float apply(TerrainShaper.Point param0) {
            return param0.erosion;
        }

        @Override
        public String toString() {
            return "erosion";
        }
    };
    static final ToFloatFunction<TerrainShaper.Point> WEIRDNESS_EXTRACTOR = new ToFloatFunction<TerrainShaper.Point>() {
        public float apply(TerrainShaper.Point param0) {
            return param0.weirdness;
        }

        @Override
        public String toString() {
            return "weirdness";
        }
    };
    static final ToFloatFunction<TerrainShaper.Point> RIDGES_EXTRACTOR = new ToFloatFunction<TerrainShaper.Point>() {
        public float apply(TerrainShaper.Point param0) {
            return param0.ridges;
        }

        @Override
        public String toString() {
            return "ridges";
        }
    };
    @VisibleForDebug
    public CubicSpline<TerrainShaper.Point> offsetSampler;
    @VisibleForDebug
    public CubicSpline<TerrainShaper.Point> factorSampler;
    @VisibleForDebug
    public CubicSpline<TerrainShaper.Point> peakNoiseBlockAmplitudeSampler;

    public TerrainShaper() {
        CubicSpline<TerrainShaper.Point> var0 = buildErosionOffsetSpline(-0.15F, 0.0F, 0.0F, 0.1F, 0.0F, -0.03F, false, false);
        CubicSpline<TerrainShaper.Point> var1 = buildErosionOffsetSpline(-0.1F, 0.03F, 0.1F, 0.1F, 0.01F, -0.03F, false, false);
        CubicSpline<TerrainShaper.Point> var2 = buildErosionOffsetSpline(-0.1F, 0.03F, 0.1F, 0.7F, 0.01F, -0.03F, true, true);
        CubicSpline<TerrainShaper.Point> var3 = buildErosionOffsetSpline(-0.05F, 0.03F, 0.1F, 1.0F, 0.01F, 0.01F, true, true);
        float var4 = -0.51F;
        float var5 = -0.4F;
        float var6 = 0.1F;
        float var7 = -0.15F;
        this.offsetSampler = CubicSpline.builder(CONTINENTS_EXTRACTOR)
            .addPoint(-1.1F, 0.044F, 0.0F)
            .addPoint(-1.02F, -0.2222F, 0.0F)
            .addPoint(-0.51F, -0.2222F, 0.0F)
            .addPoint(-0.44F, -0.12F, 0.0F)
            .addPoint(-0.18F, -0.12F, 0.0F)
            .addPoint(-0.16F, var0, 0.0F)
            .addPoint(-0.15F, var0, 0.0F)
            .addPoint(-0.1F, var1, 0.0F)
            .addPoint(0.25F, var2, 0.0F)
            .addPoint(1.0F, var3, 0.0F)
            .build();
        this.factorSampler = CubicSpline.builder(CONTINENTS_EXTRACTOR)
            .addPoint(-0.19F, 3.95F, 0.0F)
            .addPoint(-0.15F, getErosionFactor(6.25F, true), 0.0F)
            .addPoint(-0.1F, getErosionFactor(5.47F, true), 0.0F)
            .addPoint(0.03F, getErosionFactor(5.08F, true), 0.0F)
            .addPoint(0.06F, getErosionFactor(4.69F, false), 0.0F)
            .build();
        this.peakNoiseBlockAmplitudeSampler = CubicSpline.builder(CONTINENTS_EXTRACTOR)
            .addPoint(0.1F, 0.0F, 0.0F)
            .addPoint(
                0.2F,
                CubicSpline.builder(EROSION_EXTRACTOR)
                    .addPoint(
                        -0.8F,
                        CubicSpline.builder(RIDGES_EXTRACTOR)
                            .addPoint(-1.0F, 0.0F, 0.0F)
                            .addPoint(0.2F, 0.0F, 0.0F)
                            .addPoint(
                                1.0F, CubicSpline.builder(WEIRDNESS_EXTRACTOR).addPoint(-0.01F, 0.625F, 0.0F).addPoint(0.01F, 0.15625F, 0.0F).build(), 0.0F
                            )
                            .build(),
                        0.0F
                    )
                    .addPoint(-0.4F, 0.0F, 0.0F)
                    .build(),
                0.0F
            )
            .build();
    }

    private static CubicSpline<TerrainShaper.Point> getErosionFactor(float param0, boolean param1) {
        CubicSpline<TerrainShaper.Point> var0 = CubicSpline.builder(WEIRDNESS_EXTRACTOR).addPoint(-0.2F, 6.3F, 0.0F).addPoint(0.2F, param0, 0.0F).build();
        CubicSpline.Builder<TerrainShaper.Point> var1 = CubicSpline.builder(EROSION_EXTRACTOR)
            .addPoint(-0.6F, var0, 0.0F)
            .addPoint(-0.5F, CubicSpline.builder(WEIRDNESS_EXTRACTOR).addPoint(-0.05F, 6.3F, 0.0F).addPoint(0.05F, 2.67F, 0.0F).build(), 0.0F)
            .addPoint(-0.35F, var0, 0.0F)
            .addPoint(-0.25F, var0, 0.0F)
            .addPoint(-0.1F, CubicSpline.builder(WEIRDNESS_EXTRACTOR).addPoint(-0.05F, 2.67F, 0.0F).addPoint(0.05F, 6.3F, 0.0F).build(), 0.0F)
            .addPoint(0.03F, var0, 0.0F);
        if (param1) {
            CubicSpline<TerrainShaper.Point> var2 = CubicSpline.builder(WEIRDNESS_EXTRACTOR).addPoint(0.0F, param0, 0.0F).addPoint(0.1F, 0.625F, 0.0F).build();
            CubicSpline<TerrainShaper.Point> var3 = CubicSpline.builder(RIDGES_EXTRACTOR).addPoint(-0.9F, param0, 0.0F).addPoint(-0.69F, var2, 0.0F).build();
            var1.addPoint(0.35F, param0, 0.0F).addPoint(0.45F, var3, 0.0F).addPoint(0.55F, var3, 0.0F).addPoint(0.62F, param0, 0.0F);
        } else {
            CubicSpline<TerrainShaper.Point> var4 = CubicSpline.builder(RIDGES_EXTRACTOR).addPoint(-0.7F, var0, 0.0F).addPoint(-0.15F, 1.37F, 0.0F).build();
            CubicSpline<TerrainShaper.Point> var5 = CubicSpline.builder(RIDGES_EXTRACTOR).addPoint(0.45F, var0, 0.0F).addPoint(0.7F, 1.56F, 0.0F).build();
            var1.addPoint(0.05F, var5, 0.0F).addPoint(0.4F, var5, 0.0F).addPoint(0.45F, var4, 0.0F).addPoint(0.55F, var4, 0.0F).addPoint(0.58F, param0, 0.0F);
        }

        return var1.build();
    }

    private static float calculateSlope(float param0, float param1, float param2, float param3) {
        return (param1 - param0) / (param3 - param2);
    }

    private static CubicSpline<TerrainShaper.Point> buildMountainRidgeSplineWithPoints(float param0, boolean param1) {
        CubicSpline.Builder<TerrainShaper.Point> var0 = CubicSpline.builder(RIDGES_EXTRACTOR);
        float var1 = -0.7F;
        float var2 = -1.0F;
        float var3 = mountainContinentalness(-1.0F, param0, -0.7F);
        float var4 = 1.0F;
        float var5 = mountainContinentalness(1.0F, param0, -0.7F);
        float var6 = calculateMountainRidgeZeroContinentalnessPoint(param0);
        float var7 = -0.65F;
        if (-0.65F < var6 && var6 < 1.0F) {
            float var8 = mountainContinentalness(-0.65F, param0, -0.7F);
            float var9 = -0.75F;
            float var10 = mountainContinentalness(-0.75F, param0, -0.7F);
            float var11 = calculateSlope(var3, var10, -1.0F, -0.75F);
            var0.addPoint(-1.0F, var3, var11);
            var0.addPoint(-0.75F, var10, 0.0F);
            var0.addPoint(-0.65F, var8, 0.0F);
            float var12 = mountainContinentalness(var6, param0, -0.7F);
            float var13 = calculateSlope(var12, var5, var6, 1.0F);
            float var14 = 0.01F;
            var0.addPoint(var6 - 0.01F, var12, 0.0F);
            var0.addPoint(var6, var12, var13);
            var0.addPoint(1.0F, var5, var13);
        } else {
            float var15 = calculateSlope(var3, var5, -1.0F, 1.0F);
            if (param1) {
                var0.addPoint(-1.0F, Math.max(0.2F, var3), 0.0F);
                var0.addPoint(0.0F, Mth.lerp(0.5F, var3, var5), var15);
            } else {
                var0.addPoint(-1.0F, var3, var15);
            }

            var0.addPoint(1.0F, var5, var15);
        }

        return var0.build();
    }

    private static float mountainContinentalness(float param0, float param1, float param2) {
        float var0 = 1.17F;
        float var1 = 0.46082947F;
        float var2 = 1.0F - (1.0F - param1) * 0.5F;
        float var3 = 0.5F * (1.0F - param1);
        float var4 = (param0 + 1.17F) * 0.46082947F;
        float var5 = var4 * var2 - var3;
        return param0 < param2 ? Math.max(var5, -0.2222F) : Math.max(var5, 0.0F);
    }

    private static float calculateMountainRidgeZeroContinentalnessPoint(float param0) {
        float var0 = 1.17F;
        float var1 = 0.46082947F;
        float var2 = 1.0F - (1.0F - param0) * 0.5F;
        float var3 = 0.5F * (1.0F - param0);
        return var3 / (0.46082947F * var2) - 1.17F;
    }

    private static CubicSpline<TerrainShaper.Point> buildErosionOffsetSpline(
        float param0, float param1, float param2, float param3, float param4, float param5, boolean param6, boolean param7
    ) {
        float var0 = 0.6F;
        float var1 = 0.5F;
        float var2 = 0.5F;
        CubicSpline<TerrainShaper.Point> var3 = buildMountainRidgeSplineWithPoints(Mth.lerp(param3, 0.6F, 1.5F), param7);
        CubicSpline<TerrainShaper.Point> var4 = buildMountainRidgeSplineWithPoints(Mth.lerp(param3, 0.6F, 1.0F), param7);
        CubicSpline<TerrainShaper.Point> var5 = buildMountainRidgeSplineWithPoints(param3, param7);
        CubicSpline<TerrainShaper.Point> var6 = ridgeSpline(
            param0 - 0.15F, 0.5F * param3, Mth.lerp(0.5F, 0.5F, 0.5F) * param3, 0.5F * param3, 0.6F * param3, 0.5F
        );
        CubicSpline<TerrainShaper.Point> var7 = ridgeSpline(param0, param4 * param3, param1 * param3, 0.5F * param3, 0.6F * param3, 0.5F);
        CubicSpline<TerrainShaper.Point> var8 = ridgeSpline(param0, param4, param4, param1, param2, 0.5F);
        CubicSpline<TerrainShaper.Point> var9 = ridgeSpline(param0, param4, param4, param1, param2, 0.5F);
        CubicSpline<TerrainShaper.Point> var10 = CubicSpline.builder(RIDGES_EXTRACTOR)
            .addPoint(-1.0F, param0, 0.0F)
            .addPoint(-0.4F, var8, 0.0F)
            .addPoint(0.0F, param2 + 0.07F, 0.0F)
            .build();
        CubicSpline<TerrainShaper.Point> var11 = ridgeSpline(-0.02F, param5, param5, param1, param2, 0.0F);
        CubicSpline.Builder<TerrainShaper.Point> var12 = CubicSpline.builder(EROSION_EXTRACTOR)
            .addPoint(-0.85F, var3, 0.0F)
            .addPoint(-0.7F, var4, 0.0F)
            .addPoint(-0.4F, var5, 0.0F)
            .addPoint(-0.35F, var6, 0.0F)
            .addPoint(-0.1F, var7, 0.0F)
            .addPoint(0.2F, var8, 0.0F);
        if (param6) {
            var12.addPoint(0.4F, var9, 0.0F).addPoint(0.45F, var10, 0.0F).addPoint(0.55F, var10, 0.0F).addPoint(0.58F, var9, 0.0F);
        }

        var12.addPoint(0.7F, var11, 0.0F);
        return var12.build();
    }

    private static CubicSpline<TerrainShaper.Point> ridgeSpline(float param0, float param1, float param2, float param3, float param4, float param5) {
        float var0 = Math.max(0.5F * (param1 - param0), param5);
        float var1 = 5.0F * (param2 - param1);
        return CubicSpline.builder(RIDGES_EXTRACTOR)
            .addPoint(-1.0F, param0, var0)
            .addPoint(-0.4F, param1, Math.min(var0, var1))
            .addPoint(0.0F, param2, var1)
            .addPoint(0.4F, param3, 2.0F * (param3 - param2))
            .addPoint(1.0F, param4, 0.7F * (param4 - param3))
            .build();
    }

    public void addDebugBiomesToVisualizeSplinePoints(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> param0) {
        Climate.Parameter var0 = Climate.Parameter.span(-1.0F, 1.0F);
        param0.accept(Pair.of(Climate.parameters(var0, var0, var0, var0, Climate.Parameter.point(0.0F), var0, 0.01F), Biomes.PLAINS));
        CubicSpline<TerrainShaper.Point> var1 = buildErosionOffsetSpline(-0.15F, 0.0F, 0.0F, 0.1F, 0.0F, -0.03F, false, false);
        ResourceKey<Biome> var2 = Biomes.DESERT;

        for(Float var3 : var1.debugLocations()) {
            param0.accept(Pair.of(Climate.parameters(var0, var0, var0, Climate.Parameter.point(var3), Climate.Parameter.point(0.0F), var0, 0.0F), var2));
            var2 = var2 == Biomes.DESERT ? Biomes.BADLANDS : Biomes.DESERT;
        }

        for(Float var4 : this.offsetSampler.debugLocations()) {
            param0.accept(
                Pair.of(Climate.parameters(var0, var0, Climate.Parameter.point(var4), var0, Climate.Parameter.point(0.0F), var0, 0.0F), Biomes.SNOWY_TAIGA)
            );
        }

    }

    @VisibleForDebug
    public CubicSpline<TerrainShaper.Point> offsetSampler() {
        return this.offsetSampler;
    }

    @VisibleForDebug
    public CubicSpline<TerrainShaper.Point> factorSampler() {
        return this.factorSampler;
    }

    public float offset(TerrainShaper.Point param0) {
        return this.offsetSampler.apply(param0) + 0.015F;
    }

    public float factor(TerrainShaper.Point param0) {
        return this.factorSampler.apply(param0);
    }

    public float peaks(TerrainShaper.Point param0) {
        return this.peakNoiseBlockAmplitudeSampler.apply(param0);
    }

    public TerrainShaper.Point makePoint(float param0, float param1, float param2) {
        return new TerrainShaper.Point(param0, param1, peaksAndValleys(param2), param2);
    }

    public static float peaksAndValleys(float param0) {
        return -(Math.abs(Math.abs(param0) - 0.6666667F) - 0.33333334F) * 3.0F;
    }

    public static final class Point {
        final float continents;
        final float erosion;
        final float ridges;
        final float weirdness;

        public Point(float param0, float param1, float param2, float param3) {
            this.continents = param0;
            this.erosion = param1;
            this.ridges = param2;
            this.weirdness = param3;
        }

        public float continents() {
            return this.continents;
        }

        public float erosion() {
            return this.erosion;
        }

        public float ridges() {
            return this.ridges;
        }

        public float weirdness() {
            return this.weirdness;
        }
    }
}
