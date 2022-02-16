package net.minecraft.world.level.levelgen;

import java.util.stream.Stream;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.ToFloatFunction;
import net.minecraft.world.level.biome.OverworldBiomeBuilder;
import net.minecraft.world.level.biome.TerrainShaper;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.synth.BlendedNoise;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class NoiseRouterData {
    private static final float ORE_THICKNESS = 0.08F;
    private static final double VEININESS_FREQUENCY = 1.5;
    private static final double NOODLE_SPACING_AND_STRAIGHTNESS = 1.5;
    private static final double SURFACE_DENSITY_THRESHOLD = 1.5625;
    private static final DensityFunction BLENDING_FACTOR = DensityFunctions.constant(10.0);
    private static final DensityFunction BLENDING_JAGGEDNESS = DensityFunctions.zero();

    public static NoiseRouter createNoiseRouter(
        NoiseSettings param0, boolean param1, boolean param2, long param3, Registry<NormalNoise.NoiseParameters> param4, WorldgenRandom.Algorithm param5
    ) {
        PositionalRandomFactory var0 = param5.newInstance(param3).forkPositional();
        DensityFunction var1 = DensityFunctions.noise(Noises.instantiate(param4, var0, Noises.AQUIFER_BARRIER), 0.5);
        DensityFunction var2 = DensityFunctions.noise(Noises.instantiate(param4, var0, Noises.AQUIFER_FLUID_LEVEL_FLOODEDNESS), 0.67);
        DensityFunction var3 = DensityFunctions.noise(Noises.instantiate(param4, var0, Noises.AQUIFER_LAVA));
        DensityFunction var4 = DensityFunctions.noise(Noises.instantiate(param4, var0, Noises.AQUIFER_FLUID_LEVEL_SPREAD), 0.7142857142857143);
        PositionalRandomFactory var5 = var0.fromHashOf(new ResourceLocation("aquifer")).forkPositional();
        PositionalRandomFactory var6 = var0.fromHashOf(new ResourceLocation("ore")).forkPositional();
        double var7 = 25.0;
        double var8 = 0.3;
        DensityFunction var9 = DensityFunctions.noise(Noises.instantiate(param4, var0, Noises.PILLAR), 25.0, 0.3);
        DensityFunction var10 = DensityFunctions.mappedNoise(Noises.instantiate(param4, var0, Noises.PILLAR_RARENESS), 0.0, -2.0);
        DensityFunction var11 = DensityFunctions.mappedNoise(Noises.instantiate(param4, var0, Noises.PILLAR_THICKNESS), 0.0, 1.1);
        DensityFunction var12 = DensityFunctions.add(DensityFunctions.mul(var9, DensityFunctions.constant(2.0)), var10);
        DensityFunction var13 = DensityFunctions.cacheOnce(DensityFunctions.mul(var12, var11.cube()));
        DensityFunction var14 = DensityFunctions.rangeChoice(var13, Double.NEGATIVE_INFINITY, 0.03, DensityFunctions.constant(Double.NEGATIVE_INFINITY), var13);
        DensityFunction var15 = DensityFunctions.noise(Noises.instantiate(param4, var0, Noises.SPAGHETTI_2D_MODULATOR), 2.0, 1.0);
        DensityFunction var16 = DensityFunctions.weirdScaledSampler(
            var15, Noises.instantiate(param4, var0, Noises.SPAGHETTI_2D), NoiseRouterData.QuantizedSpaghettiRarity::getSphaghettiRarity2D, 3.0
        );
        DensityFunction var17 = DensityFunctions.mappedNoise(
            Noises.instantiate(param4, var0, Noises.SPAGHETTI_2D_ELEVATION), 0.0, (double)param0.getMinCellY(), 8.0
        );
        DensityFunction var18 = DensityFunctions.cacheOnce(
            DensityFunctions.mappedNoise(Noises.instantiate(param4, var0, Noises.SPAGHETTI_2D_THICKNESS), 2.0, 1.0, -0.6, -1.3)
        );
        DensityFunction var19 = DensityFunctions.add(var17, DensityFunctions.yClampedGradient(-64, 320, 8.0, -40.0)).abs();
        DensityFunction var20 = DensityFunctions.add(var19, var18).cube();
        double var21 = 0.083;
        DensityFunction var22 = DensityFunctions.add(var16, DensityFunctions.mul(DensityFunctions.constant(0.083), var18));
        DensityFunction var23 = DensityFunctions.max(var22, var20).clamp(-1.0, 1.0);
        DensityFunction var24 = DensityFunctions.cacheOnce(DensityFunctions.noise(Noises.instantiate(param4, var0, Noises.SPAGHETTI_3D_RARITY), 2.0, 1.0));
        DensityFunction var25 = DensityFunctions.mappedNoise(Noises.instantiate(param4, var0, Noises.SPAGHETTI_3D_THICKNESS), -0.065, -0.088);
        DensityFunction var26 = DensityFunctions.weirdScaledSampler(
            var24, Noises.instantiate(param4, var0, Noises.SPAGHETTI_3D_1), NoiseRouterData.QuantizedSpaghettiRarity::getSpaghettiRarity3D, 2.0
        );
        DensityFunction var27 = DensityFunctions.weirdScaledSampler(
            var24, Noises.instantiate(param4, var0, Noises.SPAGHETTI_3D_2), NoiseRouterData.QuantizedSpaghettiRarity::getSpaghettiRarity3D, 2.0
        );
        DensityFunction var28 = DensityFunctions.add(DensityFunctions.max(var26, var27), var25).clamp(-1.0, 1.0);
        DensityFunction var29 = DensityFunctions.noise(Noises.instantiate(param4, var0, Noises.SPAGHETTI_ROUGHNESS));
        DensityFunction var30 = DensityFunctions.mappedNoise(Noises.instantiate(param4, var0, Noises.SPAGHETTI_ROUGHNESS_MODULATOR), 0.0, -0.1);
        DensityFunction var31 = DensityFunctions.cacheOnce(DensityFunctions.mul(var30, DensityFunctions.add(var29.abs(), DensityFunctions.constant(-0.4))));
        DensityFunction var32 = DensityFunctions.noise(Noises.instantiate(param4, var0, Noises.CAVE_ENTRANCE), 0.75, 0.5);
        DensityFunction var33 = DensityFunctions.add(
            DensityFunctions.add(var32, DensityFunctions.constant(0.37)), DensityFunctions.yClampedGradient(-10, 30, 0.3, 0.0)
        );
        DensityFunction var34 = DensityFunctions.cacheOnce(DensityFunctions.min(var33, DensityFunctions.add(var31, var28)));
        DensityFunction var35 = DensityFunctions.noise(Noises.instantiate(param4, var0, Noises.CAVE_LAYER), 8.0);
        DensityFunction var36 = DensityFunctions.mul(DensityFunctions.constant(4.0), var35.square());
        DensityFunction var37 = DensityFunctions.noise(Noises.instantiate(param4, var0, Noises.CAVE_CHEESE), 0.6666666666666666);
        int var38 = DimensionType.MIN_Y * 2;
        int var39 = DimensionType.MAX_Y * 2;
        DensityFunction var40 = DensityFunctions.yClampedGradient(var38, var39, (double)var38, (double)var39);
        int var41 = param0.minY();
        int var42 = Stream.of(OreVeinifier.VeinType.values()).mapToInt(param0x -> param0x.minY).min().orElse(var41);
        int var43 = Stream.of(OreVeinifier.VeinType.values()).mapToInt(param0x -> param0x.maxY).max().orElse(var41);
        DensityFunction var44 = yLimitedInterpolatable(
            var40, DensityFunctions.noise(Noises.instantiate(param4, var0, Noises.ORE_VEININESS), 1.5, 1.5), var42, var43, 0
        );
        float var45 = 4.0F;
        DensityFunction var46 = yLimitedInterpolatable(
                var40, DensityFunctions.noise(Noises.instantiate(param4, var0, Noises.ORE_VEIN_A), 4.0, 4.0), var42, var43, 0
            )
            .abs();
        DensityFunction var47 = yLimitedInterpolatable(
                var40, DensityFunctions.noise(Noises.instantiate(param4, var0, Noises.ORE_VEIN_B), 4.0, 4.0), var42, var43, 0
            )
            .abs();
        DensityFunction var48 = DensityFunctions.add(DensityFunctions.constant(-0.08F), DensityFunctions.max(var46, var47));
        DensityFunction var49 = DensityFunctions.noise(Noises.instantiate(param4, var0, Noises.ORE_GAP));
        int var50 = var41 + 4;
        int var51 = var41 + param0.height();
        DensityFunction var58;
        if (param2) {
            DensityFunction var52 = yLimitedInterpolatable(
                var40, DensityFunctions.noise(Noises.instantiate(param4, var0, Noises.NOODLE), 1.0, 1.0), var50, var51, -1
            );
            DensityFunction var53 = yLimitedInterpolatable(
                var40, DensityFunctions.mappedNoise(Noises.instantiate(param4, var0, Noises.NOODLE_THICKNESS), 1.0, 1.0, -0.05, -0.1), var50, var51, 0
            );
            double var54 = 2.6666666666666665;
            DensityFunction var55 = yLimitedInterpolatable(
                var40, DensityFunctions.noise(Noises.instantiate(param4, var0, Noises.NOODLE_RIDGE_A), 2.6666666666666665, 2.6666666666666665), var50, var51, 0
            );
            DensityFunction var56 = yLimitedInterpolatable(
                var40, DensityFunctions.noise(Noises.instantiate(param4, var0, Noises.NOODLE_RIDGE_B), 2.6666666666666665, 2.6666666666666665), var50, var51, 0
            );
            DensityFunction var57 = DensityFunctions.mul(DensityFunctions.constant(1.5), DensityFunctions.max(var55.abs(), var56.abs()));
            var58 = DensityFunctions.rangeChoice(var52, Double.NEGATIVE_INFINITY, 0.0, DensityFunctions.constant(64.0), DensityFunctions.add(var53, var57));
        } else {
            var58 = DensityFunctions.constant(64.0);
        }

        boolean var60 = param0.largeBiomes();
        NormalNoise var64;
        DensityFunction var61;
        NormalNoise var62;
        NormalNoise var63;
        if (param5 != WorldgenRandom.Algorithm.LEGACY) {
            var61 = new BlendedNoise(
                var0.fromHashOf(new ResourceLocation("terrain")), param0.noiseSamplingSettings(), param0.getCellWidth(), param0.getCellHeight()
            );
            var62 = Noises.instantiate(param4, var0, var60 ? Noises.TEMPERATURE_LARGE : Noises.TEMPERATURE);
            var63 = Noises.instantiate(param4, var0, var60 ? Noises.VEGETATION_LARGE : Noises.VEGETATION);
            var64 = Noises.instantiate(param4, var0, Noises.SHIFT);
        } else {
            var61 = new BlendedNoise(param5.newInstance(param3), param0.noiseSamplingSettings(), param0.getCellWidth(), param0.getCellHeight());
            var62 = NormalNoise.createLegacyNetherBiome(param5.newInstance(param3), new NormalNoise.NoiseParameters(-7, 1.0, 1.0));
            var63 = NormalNoise.createLegacyNetherBiome(param5.newInstance(param3 + 1L), new NormalNoise.NoiseParameters(-7, 1.0, 1.0));
            var64 = NormalNoise.create(var0.fromHashOf(Noises.SHIFT.location()), new NormalNoise.NoiseParameters(0, 0.0));
        }

        DensityFunction var69 = DensityFunctions.flatCache(DensityFunctions.cache2d(DensityFunctions.shiftA(var64)));
        DensityFunction var70 = DensityFunctions.flatCache(DensityFunctions.cache2d(DensityFunctions.shiftB(var64)));
        DensityFunction var71 = DensityFunctions.shiftedNoise2d(var69, var70, 0.25, var62);
        DensityFunction var72 = DensityFunctions.shiftedNoise2d(var69, var70, 0.25, var63);
        DensityFunction var73 = DensityFunctions.flatCache(
            DensityFunctions.shiftedNoise2d(var69, var70, 0.25, Noises.instantiate(param4, var0, var60 ? Noises.CONTINENTALNESS_LARGE : Noises.CONTINENTALNESS))
        );
        DensityFunction var74 = DensityFunctions.flatCache(
            DensityFunctions.shiftedNoise2d(var69, var70, 0.25, Noises.instantiate(param4, var0, var60 ? Noises.EROSION_LARGE : Noises.EROSION))
        );
        DensityFunction var75 = DensityFunctions.flatCache(DensityFunctions.shiftedNoise2d(var69, var70, 0.25, Noises.instantiate(param4, var0, Noises.RIDGE)));
        TerrainShaper var76 = param0.terrainShaper();
        DensityFunction var77 = splineWithBlending(var73, var74, var75, var76::offset, -0.81, 2.5, DensityFunctions.blendOffset());
        DensityFunction var78 = splineWithBlending(var73, var74, var75, var76::factor, 0.0, 8.0, BLENDING_FACTOR);
        DensityFunction var79 = splineWithBlending(var73, var74, var75, var76::jaggedness, 0.0, 1.28, BLENDING_JAGGEDNESS);
        DensityFunction var80 = DensityFunctions.add(DensityFunctions.yClampedGradient(-64, 320, 1.5, -1.5), var77);
        DensityFunction var81 = DensityFunctions.noise(Noises.instantiate(param4, var0, Noises.JAGGED), 1500.0, 0.0);
        DensityFunction var82 = DensityFunctions.mul(var79, var81.halfNegative());
        DensityFunction var83;
        DensityFunction var84;
        if (param0.islandNoiseOverride()) {
            var83 = DensityFunctions.endIslands(param3);
            var84 = DensityFunctions.cache2d(var83);
        } else {
            var83 = noiseGradientDensity(var80, var78, var82);
            var84 = noiseGradientDensity(var80, DensityFunctions.cache2d(var78), DensityFunctions.zero());
        }

        boolean var87 = !param1;
        DensityFunction var88 = DensityFunctions.cacheOnce(DensityFunctions.add(var83, var61));
        DensityFunction var89 = DensityFunctions.add(
            DensityFunctions.add(DensityFunctions.constant(0.27), var37).clamp(-1.0, 1.0),
            DensityFunctions.add(DensityFunctions.constant(1.5), DensityFunctions.mul(DensityFunctions.constant(-0.64), var88)).clamp(0.0, 0.5)
        );
        DensityFunction var90 = DensityFunctions.add(var36, var89);
        DensityFunction var91 = DensityFunctions.min(DensityFunctions.min(var90, var34), DensityFunctions.add(var23, var31));
        DensityFunction var92 = DensityFunctions.max(var91, var14);
        DensityFunction var93;
        if (var87) {
            var93 = var88;
        } else {
            DensityFunction var94 = DensityFunctions.min(var88, DensityFunctions.mul(DensityFunctions.constant(5.0), var34));
            var93 = DensityFunctions.rangeChoice(var88, Double.NEGATIVE_INFINITY, 1.5625, var94, var92);
        }

        DensityFunction var96 = DensityFunctions.slide(param0, var93);
        DensityFunction var97 = DensityFunctions.interpolated(DensityFunctions.blendDensity(var96));
        DensityFunction var98 = DensityFunctions.mul(var97, DensityFunctions.constant(0.64));
        DensityFunction var99 = DensityFunctions.min(var98.squeeze(), var58);
        return new NoiseRouter(
            var1,
            var2,
            var4,
            var3,
            var5,
            var6,
            var71,
            var72,
            var73,
            var74,
            var80,
            var75,
            var84,
            var99,
            var44,
            var48,
            var49,
            new OverworldBiomeBuilder().spawnTarget()
        );
    }

    private static DensityFunction splineWithBlending(
        DensityFunction param0,
        DensityFunction param1,
        DensityFunction param2,
        ToFloatFunction<TerrainShaper.Point> param3,
        double param4,
        double param5,
        DensityFunction param6
    ) {
        DensityFunction var0 = DensityFunctions.terrainShaperSpline(param0, param1, param2, param3, param4, param5);
        DensityFunction var1 = DensityFunctions.lerp(DensityFunctions.blendAlpha(), param6, var0);
        return DensityFunctions.flatCache(DensityFunctions.cache2d(var1));
    }

    private static DensityFunction noiseGradientDensity(DensityFunction param0, DensityFunction param1, DensityFunction param2) {
        DensityFunction var0 = DensityFunctions.mul(DensityFunctions.add(param0, param2), param1);
        return DensityFunctions.mul(DensityFunctions.constant(4.0), var0.quarterNegative());
    }

    private static DensityFunction yLimitedInterpolatable(DensityFunction param0, DensityFunction param1, int param2, int param3, int param4) {
        return DensityFunctions.interpolated(
            DensityFunctions.rangeChoice(param0, (double)param2, (double)(param3 + 1), param1, DensityFunctions.constant((double)param4))
        );
    }

    protected static double applySlide(NoiseSettings param0, double param1, double param2) {
        double var0 = (double)((int)param2 / param0.getCellHeight() - param0.getMinCellY());
        param1 = param0.topSlideSettings().applySlide(param1, (double)param0.getCellCountY() - var0);
        return param0.bottomSlideSettings().applySlide(param1, var0);
    }

    protected static double computePreliminarySurfaceLevelScanning(NoiseSettings param0, DensityFunction param1, int param2, int param3) {
        for(int var0 = param0.getMinCellY() + param0.getCellCountY(); var0 >= param0.getMinCellY(); --var0) {
            int var1 = var0 * param0.getCellHeight();
            double var2 = -0.703125;
            double var3 = param1.compute(new DensityFunction.SinglePointContext(param2, var1, param3)) + -0.703125;
            double var4 = Mth.clamp(var3, -64.0, 64.0);
            var4 = applySlide(param0, var4, (double)var1);
            if (var4 > 0.390625) {
                return (double)var1;
            }
        }

        return 2.147483647E9;
    }

    static final class QuantizedSpaghettiRarity {
        private QuantizedSpaghettiRarity() {
        }

        private static double getSphaghettiRarity2D(double param0) {
            if (param0 < -0.75) {
                return 0.5;
            } else if (param0 < -0.5) {
                return 0.75;
            } else if (param0 < 0.5) {
                return 1.0;
            } else {
                return param0 < 0.75 ? 2.0 : 3.0;
            }
        }

        private static double getSpaghettiRarity3D(double param0) {
            if (param0 < -0.5) {
                return 0.75;
            } else if (param0 < 0.0) {
                return 1.0;
            } else {
                return param0 < 0.5 ? 1.5 : 2.0;
            }
        }
    }
}
