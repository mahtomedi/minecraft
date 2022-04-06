package net.minecraft.data.worldgen;

import net.minecraft.util.CubicSpline;
import net.minecraft.util.Mth;
import net.minecraft.util.ToFloatFunction;
import net.minecraft.world.level.levelgen.NoiseRouterData;

public class TerrainProvider {
    private static final float DEEP_OCEAN_CONTINENTALNESS = -0.51F;
    private static final float OCEAN_CONTINENTALNESS = -0.4F;
    private static final float PLAINS_CONTINENTALNESS = 0.1F;
    private static final float BEACH_CONTINENTALNESS = -0.15F;
    private static final ToFloatFunction<Float> NO_TRANSFORM = ToFloatFunction.IDENTITY;
    private static final ToFloatFunction<Float> AMPLIFIED_OFFSET = ToFloatFunction.createUnlimited(param0 -> param0 < 0.0F ? param0 : param0 * 2.0F);
    private static final ToFloatFunction<Float> AMPLIFIED_FACTOR = ToFloatFunction.createUnlimited(param0 -> 1.25F - 6.25F / (param0 + 5.0F));
    private static final ToFloatFunction<Float> AMPLIFIED_JAGGEDNESS = ToFloatFunction.createUnlimited(param0 -> param0 * 2.0F);

    public static <C, I extends ToFloatFunction<C>> CubicSpline<C, I> overworldOffset(I param0, I param1, I param2, boolean param3) {
        ToFloatFunction<Float> var0 = param3 ? AMPLIFIED_OFFSET : NO_TRANSFORM;
        CubicSpline<C, I> var1 = buildErosionOffsetSpline(param1, param2, -0.15F, 0.0F, 0.0F, 0.1F, 0.0F, -0.03F, false, false, var0);
        CubicSpline<C, I> var2 = buildErosionOffsetSpline(param1, param2, -0.1F, 0.03F, 0.1F, 0.1F, 0.01F, -0.03F, false, false, var0);
        CubicSpline<C, I> var3 = buildErosionOffsetSpline(param1, param2, -0.1F, 0.03F, 0.1F, 0.7F, 0.01F, -0.03F, true, true, var0);
        CubicSpline<C, I> var4 = buildErosionOffsetSpline(param1, param2, -0.05F, 0.03F, 0.1F, 1.0F, 0.01F, 0.01F, true, true, var0);
        return CubicSpline.<C, I>builder(param0, var0)
            .addPoint(-1.1F, 0.044F)
            .addPoint(-1.02F, -0.2222F)
            .addPoint(-0.51F, -0.2222F)
            .addPoint(-0.44F, -0.12F)
            .addPoint(-0.18F, -0.12F)
            .addPoint(-0.16F, var1)
            .addPoint(-0.15F, var1)
            .addPoint(-0.1F, var2)
            .addPoint(0.25F, var3)
            .addPoint(1.0F, var4)
            .build();
    }

    public static <C, I extends ToFloatFunction<C>> CubicSpline<C, I> overworldFactor(I param0, I param1, I param2, I param3, boolean param4) {
        ToFloatFunction<Float> var0 = param4 ? AMPLIFIED_FACTOR : NO_TRANSFORM;
        return CubicSpline.<C, I>builder(param0, NO_TRANSFORM)
            .addPoint(-0.19F, 3.95F)
            .addPoint(-0.15F, getErosionFactor(param1, param2, param3, 6.25F, true, NO_TRANSFORM))
            .addPoint(-0.1F, getErosionFactor(param1, param2, param3, 5.47F, true, var0))
            .addPoint(0.03F, getErosionFactor(param1, param2, param3, 5.08F, true, var0))
            .addPoint(0.06F, getErosionFactor(param1, param2, param3, 4.69F, false, var0))
            .build();
    }

    public static <C, I extends ToFloatFunction<C>> CubicSpline<C, I> overworldJaggedness(I param0, I param1, I param2, I param3, boolean param4) {
        ToFloatFunction<Float> var0 = param4 ? AMPLIFIED_JAGGEDNESS : NO_TRANSFORM;
        float var1 = 0.65F;
        return CubicSpline.<C, I>builder(param0, var0)
            .addPoint(-0.11F, 0.0F)
            .addPoint(0.03F, buildErosionJaggednessSpline(param1, param2, param3, 1.0F, 0.5F, 0.0F, 0.0F, var0))
            .addPoint(0.65F, buildErosionJaggednessSpline(param1, param2, param3, 1.0F, 1.0F, 1.0F, 0.0F, var0))
            .build();
    }

    private static <C, I extends ToFloatFunction<C>> CubicSpline<C, I> buildErosionJaggednessSpline(
        I param0, I param1, I param2, float param3, float param4, float param5, float param6, ToFloatFunction<Float> param7
    ) {
        float var0 = -0.5775F;
        CubicSpline<C, I> var1 = buildRidgeJaggednessSpline(param1, param2, param3, param5, param7);
        CubicSpline<C, I> var2 = buildRidgeJaggednessSpline(param1, param2, param4, param6, param7);
        return CubicSpline.<C, I>builder(param0, param7).addPoint(-1.0F, var1).addPoint(-0.78F, var2).addPoint(-0.5775F, var2).addPoint(-0.375F, 0.0F).build();
    }

    private static <C, I extends ToFloatFunction<C>> CubicSpline<C, I> buildRidgeJaggednessSpline(
        I param0, I param1, float param2, float param3, ToFloatFunction<Float> param4
    ) {
        float var0 = NoiseRouterData.peaksAndValleys(0.4F);
        float var1 = NoiseRouterData.peaksAndValleys(0.56666666F);
        float var2 = (var0 + var1) / 2.0F;
        CubicSpline.Builder<C, I> var3 = CubicSpline.builder(param1, param4);
        var3.addPoint(var0, 0.0F);
        if (param3 > 0.0F) {
            var3.addPoint(var2, buildWeirdnessJaggednessSpline(param0, param3, param4));
        } else {
            var3.addPoint(var2, 0.0F);
        }

        if (param2 > 0.0F) {
            var3.addPoint(1.0F, buildWeirdnessJaggednessSpline(param0, param2, param4));
        } else {
            var3.addPoint(1.0F, 0.0F);
        }

        return var3.build();
    }

    private static <C, I extends ToFloatFunction<C>> CubicSpline<C, I> buildWeirdnessJaggednessSpline(I param0, float param1, ToFloatFunction<Float> param2) {
        float var0 = 0.63F * param1;
        float var1 = 0.3F * param1;
        return CubicSpline.<C, I>builder(param0, param2).addPoint(-0.01F, var0).addPoint(0.01F, var1).build();
    }

    private static <C, I extends ToFloatFunction<C>> CubicSpline<C, I> getErosionFactor(
        I param0, I param1, I param2, float param3, boolean param4, ToFloatFunction<Float> param5
    ) {
        CubicSpline<C, I> var0 = CubicSpline.<C, I>builder(param1, param5).addPoint(-0.2F, 6.3F).addPoint(0.2F, param3).build();
        CubicSpline.Builder<C, I> var1 = CubicSpline.<C, I>builder(param0, param5)
            .addPoint(-0.6F, var0)
            .addPoint(-0.5F, CubicSpline.<C, I>builder(param1, param5).addPoint(-0.05F, 6.3F).addPoint(0.05F, 2.67F).build())
            .addPoint(-0.35F, var0)
            .addPoint(-0.25F, var0)
            .addPoint(-0.1F, CubicSpline.<C, I>builder(param1, param5).addPoint(-0.05F, 2.67F).addPoint(0.05F, 6.3F).build())
            .addPoint(0.03F, var0);
        if (param4) {
            CubicSpline<C, I> var2 = CubicSpline.<C, I>builder(param1, param5).addPoint(0.0F, param3).addPoint(0.1F, 0.625F).build();
            CubicSpline<C, I> var3 = CubicSpline.<C, I>builder(param2, param5).addPoint(-0.9F, param3).addPoint(-0.69F, var2).build();
            var1.addPoint(0.35F, param3).addPoint(0.45F, var3).addPoint(0.55F, var3).addPoint(0.62F, param3);
        } else {
            CubicSpline<C, I> var4 = CubicSpline.<C, I>builder(param2, param5).addPoint(-0.7F, var0).addPoint(-0.15F, 1.37F).build();
            CubicSpline<C, I> var5 = CubicSpline.<C, I>builder(param2, param5).addPoint(0.45F, var0).addPoint(0.7F, 1.56F).build();
            var1.addPoint(0.05F, var5).addPoint(0.4F, var5).addPoint(0.45F, var4).addPoint(0.55F, var4).addPoint(0.58F, param3);
        }

        return var1.build();
    }

    private static float calculateSlope(float param0, float param1, float param2, float param3) {
        return (param1 - param0) / (param3 - param2);
    }

    private static <C, I extends ToFloatFunction<C>> CubicSpline<C, I> buildMountainRidgeSplineWithPoints(
        I param0, float param1, boolean param2, ToFloatFunction<Float> param3
    ) {
        CubicSpline.Builder<C, I> var0 = CubicSpline.builder(param0, param3);
        float var1 = -0.7F;
        float var2 = -1.0F;
        float var3 = mountainContinentalness(-1.0F, param1, -0.7F);
        float var4 = 1.0F;
        float var5 = mountainContinentalness(1.0F, param1, -0.7F);
        float var6 = calculateMountainRidgeZeroContinentalnessPoint(param1);
        float var7 = -0.65F;
        if (-0.65F < var6 && var6 < 1.0F) {
            float var8 = mountainContinentalness(-0.65F, param1, -0.7F);
            float var9 = -0.75F;
            float var10 = mountainContinentalness(-0.75F, param1, -0.7F);
            float var11 = calculateSlope(var3, var10, -1.0F, -0.75F);
            var0.addPoint(-1.0F, var3, var11);
            var0.addPoint(-0.75F, var10);
            var0.addPoint(-0.65F, var8);
            float var12 = mountainContinentalness(var6, param1, -0.7F);
            float var13 = calculateSlope(var12, var5, var6, 1.0F);
            float var14 = 0.01F;
            var0.addPoint(var6 - 0.01F, var12);
            var0.addPoint(var6, var12, var13);
            var0.addPoint(1.0F, var5, var13);
        } else {
            float var15 = calculateSlope(var3, var5, -1.0F, 1.0F);
            if (param2) {
                var0.addPoint(-1.0F, Math.max(0.2F, var3));
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

    public static <C, I extends ToFloatFunction<C>> CubicSpline<C, I> buildErosionOffsetSpline(
        I param0,
        I param1,
        float param2,
        float param3,
        float param4,
        float param5,
        float param6,
        float param7,
        boolean param8,
        boolean param9,
        ToFloatFunction<Float> param10
    ) {
        float var0 = 0.6F;
        float var1 = 0.5F;
        float var2 = 0.5F;
        CubicSpline<C, I> var3 = buildMountainRidgeSplineWithPoints(param1, Mth.lerp(param5, 0.6F, 1.5F), param9, param10);
        CubicSpline<C, I> var4 = buildMountainRidgeSplineWithPoints(param1, Mth.lerp(param5, 0.6F, 1.0F), param9, param10);
        CubicSpline<C, I> var5 = buildMountainRidgeSplineWithPoints(param1, param5, param9, param10);
        CubicSpline<C, I> var6 = ridgeSpline(
            param1, param2 - 0.15F, 0.5F * param5, Mth.lerp(0.5F, 0.5F, 0.5F) * param5, 0.5F * param5, 0.6F * param5, 0.5F, param10
        );
        CubicSpline<C, I> var7 = ridgeSpline(param1, param2, param6 * param5, param3 * param5, 0.5F * param5, 0.6F * param5, 0.5F, param10);
        CubicSpline<C, I> var8 = ridgeSpline(param1, param2, param6, param6, param3, param4, 0.5F, param10);
        CubicSpline<C, I> var9 = ridgeSpline(param1, param2, param6, param6, param3, param4, 0.5F, param10);
        CubicSpline<C, I> var10 = CubicSpline.<C, I>builder(param1, param10)
            .addPoint(-1.0F, param2)
            .addPoint(-0.4F, var8)
            .addPoint(0.0F, param4 + 0.07F)
            .build();
        CubicSpline<C, I> var11 = ridgeSpline(param1, -0.02F, param7, param7, param3, param4, 0.0F, param10);
        CubicSpline.Builder<C, I> var12 = CubicSpline.<C, I>builder(param0, param10)
            .addPoint(-0.85F, var3)
            .addPoint(-0.7F, var4)
            .addPoint(-0.4F, var5)
            .addPoint(-0.35F, var6)
            .addPoint(-0.1F, var7)
            .addPoint(0.2F, var8);
        if (param8) {
            var12.addPoint(0.4F, var9).addPoint(0.45F, var10).addPoint(0.55F, var10).addPoint(0.58F, var9);
        }

        var12.addPoint(0.7F, var11);
        return var12.build();
    }

    private static <C, I extends ToFloatFunction<C>> CubicSpline<C, I> ridgeSpline(
        I param0, float param1, float param2, float param3, float param4, float param5, float param6, ToFloatFunction<Float> param7
    ) {
        float var0 = Math.max(0.5F * (param2 - param1), param6);
        float var1 = 5.0F * (param3 - param2);
        return CubicSpline.<C, I>builder(param0, param7)
            .addPoint(-1.0F, param1, var0)
            .addPoint(-0.4F, param2, Math.min(var0, var1))
            .addPoint(0.0F, param3, var1)
            .addPoint(0.4F, param4, 2.0F * (param4 - param3))
            .addPoint(1.0F, param5, 0.7F * (param5 - param4))
            .build();
    }
}
