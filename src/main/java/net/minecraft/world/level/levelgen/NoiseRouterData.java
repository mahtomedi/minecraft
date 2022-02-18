package net.minecraft.world.level.levelgen;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
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
    private static final ResourceKey<DensityFunction> ZERO = createKey("zero");
    private static final ResourceKey<DensityFunction> Y = createKey("y");
    private static final ResourceKey<DensityFunction> SHIFT_X = createKey("shift_x");
    private static final ResourceKey<DensityFunction> SHIFT_Z = createKey("shift_z");
    private static final ResourceKey<DensityFunction> BASE_3D_NOISE = createKey("overworld/base_3d_noise");
    private static final ResourceKey<DensityFunction> CONTINENTS = createKey("overworld/continents");
    private static final ResourceKey<DensityFunction> EROSION = createKey("overworld/erosion");
    private static final ResourceKey<DensityFunction> RIDGES = createKey("overworld/ridges");
    private static final ResourceKey<DensityFunction> FACTOR = createKey("overworld/factor");
    private static final ResourceKey<DensityFunction> DEPTH = createKey("overworld/depth");
    private static final ResourceKey<DensityFunction> SLOPED_CHEESE = createKey("overworld/sloped_cheese");
    private static final ResourceKey<DensityFunction> CONTINENTS_LARGE = createKey("overworld_large_biomes/continents");
    private static final ResourceKey<DensityFunction> EROSION_LARGE = createKey("overworld_large_biomes/erosion");
    private static final ResourceKey<DensityFunction> FACTOR_LARGE = createKey("overworld_large_biomes/factor");
    private static final ResourceKey<DensityFunction> DEPTH_LARGE = createKey("overworld_large_biomes/depth");
    private static final ResourceKey<DensityFunction> SLOPED_CHEESE_LARGE = createKey("overworld_large_biomes/sloped_cheese");
    private static final ResourceKey<DensityFunction> SLOPED_CHEESE_END = createKey("end/sloped_cheese");
    private static final ResourceKey<DensityFunction> SPAGHETTI_ROUGHNESS_FUNCTION = createKey("overworld/caves/spaghetti_roughness_function");
    private static final ResourceKey<DensityFunction> ENTRANCES = createKey("overworld/caves/entrances");
    private static final ResourceKey<DensityFunction> NOODLE = createKey("overworld/caves/noodle");
    private static final ResourceKey<DensityFunction> PILLARS = createKey("overworld/caves/pillars");
    private static final ResourceKey<DensityFunction> SPAGHETTI_2D_THICKNESS_MODULATOR = createKey("overworld/caves/spaghetti_2d_thickness_modulator");
    private static final ResourceKey<DensityFunction> SPAGHETTI_2D = createKey("overworld/caves/spaghetti_2d");

    protected static NoiseRouterWithOnlyNoises overworld(NoiseSettings param0) {
        return noiseRouter(param0, true, true);
    }

    protected static NoiseRouterWithOnlyNoises overworldWithoutCaves(NoiseSettings param0) {
        return noiseRouter(param0, false, false);
    }

    protected static NoiseRouterWithOnlyNoises nether(NoiseSettings param0) {
        return noiseRouter(param0, false, false);
    }

    protected static NoiseRouterWithOnlyNoises end(NoiseSettings param0) {
        return noiseRouter(param0, false, false);
    }

    private static ResourceKey<DensityFunction> createKey(String param0) {
        return ResourceKey.create(Registry.DENSITY_FUNCTION_REGISTRY, new ResourceLocation(param0));
    }

    public static Holder<? extends DensityFunction> bootstrap() {
        register(ZERO, DensityFunctions.zero());
        int var0 = DimensionType.MIN_Y * 2;
        int var1 = DimensionType.MAX_Y * 2;
        register(Y, DensityFunctions.yClampedGradient(var0, var1, (double)var0, (double)var1));
        DensityFunction var2 = register(SHIFT_X, DensityFunctions.flatCache(DensityFunctions.cache2d(DensityFunctions.shiftA(getNoise(Noises.SHIFT)))));
        DensityFunction var3 = register(SHIFT_Z, DensityFunctions.flatCache(DensityFunctions.cache2d(DensityFunctions.shiftB(getNoise(Noises.SHIFT)))));
        register(BASE_3D_NOISE, BlendedNoise.UNSEEDED);
        DensityFunction var4 = register(
            CONTINENTS, DensityFunctions.flatCache(DensityFunctions.shiftedNoise2d(var2, var3, 0.25, getNoise(Noises.CONTINENTALNESS)))
        );
        DensityFunction var5 = register(EROSION, DensityFunctions.flatCache(DensityFunctions.shiftedNoise2d(var2, var3, 0.25, getNoise(Noises.EROSION))));
        DensityFunction var6 = register(RIDGES, DensityFunctions.flatCache(DensityFunctions.shiftedNoise2d(var2, var3, 0.25, getNoise(Noises.RIDGE))));
        DensityFunction var7 = DensityFunctions.noise(getNoise(Noises.JAGGED), 1500.0, 0.0);
        DensityFunction var8 = splineWithBlending(
            var4, var5, var6, DensityFunctions.TerrainShaperSpline.SplineType.OFFSET, -0.81, 2.5, DensityFunctions.blendOffset()
        );
        DensityFunction var9 = register(
            FACTOR, splineWithBlending(var4, var5, var6, DensityFunctions.TerrainShaperSpline.SplineType.FACTOR, 0.0, 8.0, BLENDING_FACTOR)
        );
        DensityFunction var10 = register(DEPTH, DensityFunctions.add(DensityFunctions.yClampedGradient(-64, 320, 1.5, -1.5), var8));
        register(SLOPED_CHEESE, slopedCheese(var4, var5, var6, var9, var10, var7));
        DensityFunction var11 = register(
            CONTINENTS_LARGE, DensityFunctions.flatCache(DensityFunctions.shiftedNoise2d(var2, var3, 0.25, getNoise(Noises.CONTINENTALNESS_LARGE)))
        );
        DensityFunction var12 = register(
            EROSION_LARGE, DensityFunctions.flatCache(DensityFunctions.shiftedNoise2d(var2, var3, 0.25, getNoise(Noises.EROSION_LARGE)))
        );
        DensityFunction var13 = splineWithBlending(
            var11, var12, var6, DensityFunctions.TerrainShaperSpline.SplineType.OFFSET, -0.81, 2.5, DensityFunctions.blendOffset()
        );
        DensityFunction var14 = register(
            FACTOR_LARGE, splineWithBlending(var11, var12, var6, DensityFunctions.TerrainShaperSpline.SplineType.FACTOR, 0.0, 8.0, BLENDING_FACTOR)
        );
        DensityFunction var15 = register(DEPTH_LARGE, DensityFunctions.add(DensityFunctions.yClampedGradient(-64, 320, 1.5, -1.5), var13));
        register(SLOPED_CHEESE_LARGE, slopedCheese(var11, var12, var6, var14, var15, var7));
        register(SLOPED_CHEESE_END, DensityFunctions.add(DensityFunctions.endIslands(0L), getFunction(BASE_3D_NOISE)));
        register(SPAGHETTI_ROUGHNESS_FUNCTION, spaghettiRoughnessFunction());
        register(
            SPAGHETTI_2D_THICKNESS_MODULATOR,
            DensityFunctions.cacheOnce(DensityFunctions.mappedNoise(getNoise(Noises.SPAGHETTI_2D_THICKNESS), 2.0, 1.0, -0.6, -1.3))
        );
        register(SPAGHETTI_2D, spaghetti2D());
        register(ENTRANCES, entrances());
        register(NOODLE, noodle());
        register(PILLARS, pillars());
        return BuiltinRegistries.DENSITY_FUNCTION.holders().iterator().next();
    }

    private static DensityFunction register(ResourceKey<DensityFunction> param0, DensityFunction param1) {
        return new DensityFunctions.HolderHolder(BuiltinRegistries.register(BuiltinRegistries.DENSITY_FUNCTION, param0, param1));
    }

    private static Holder<NormalNoise.NoiseParameters> getNoise(ResourceKey<NormalNoise.NoiseParameters> param0) {
        return BuiltinRegistries.NOISE.getHolderOrThrow(param0);
    }

    private static DensityFunction getFunction(ResourceKey<DensityFunction> param0) {
        return new DensityFunctions.HolderHolder(BuiltinRegistries.DENSITY_FUNCTION.getHolderOrThrow(param0));
    }

    private static DensityFunction slopedCheese(
        DensityFunction param0, DensityFunction param1, DensityFunction param2, DensityFunction param3, DensityFunction param4, DensityFunction param5
    ) {
        DensityFunction var0 = splineWithBlending(
            param0, param1, param2, DensityFunctions.TerrainShaperSpline.SplineType.JAGGEDNESS, 0.0, 1.28, BLENDING_JAGGEDNESS
        );
        DensityFunction var1 = DensityFunctions.mul(var0, param5.halfNegative());
        DensityFunction var2 = noiseGradientDensity(param4, param3, var1);
        return DensityFunctions.add(var2, getFunction(BASE_3D_NOISE));
    }

    private static DensityFunction spaghettiRoughnessFunction() {
        DensityFunction var0 = DensityFunctions.noise(getNoise(Noises.SPAGHETTI_ROUGHNESS));
        DensityFunction var1 = DensityFunctions.mappedNoise(getNoise(Noises.SPAGHETTI_ROUGHNESS_MODULATOR), 0.0, -0.1);
        return DensityFunctions.cacheOnce(DensityFunctions.mul(var1, DensityFunctions.add(var0.abs(), DensityFunctions.constant(-0.4))));
    }

    private static DensityFunction entrances() {
        DensityFunction var0 = DensityFunctions.cacheOnce(DensityFunctions.noise(getNoise(Noises.SPAGHETTI_3D_RARITY), 2.0, 1.0));
        DensityFunction var1 = DensityFunctions.mappedNoise(getNoise(Noises.SPAGHETTI_3D_THICKNESS), -0.065, -0.088);
        DensityFunction var2 = DensityFunctions.weirdScaledSampler(
            var0, getNoise(Noises.SPAGHETTI_3D_1), DensityFunctions.WeirdScaledSampler.RarityValueMapper.TYPE1
        );
        DensityFunction var3 = DensityFunctions.weirdScaledSampler(
            var0, getNoise(Noises.SPAGHETTI_3D_2), DensityFunctions.WeirdScaledSampler.RarityValueMapper.TYPE1
        );
        DensityFunction var4 = DensityFunctions.add(DensityFunctions.max(var2, var3), var1).clamp(-1.0, 1.0);
        DensityFunction var5 = getFunction(SPAGHETTI_ROUGHNESS_FUNCTION);
        DensityFunction var6 = DensityFunctions.noise(getNoise(Noises.CAVE_ENTRANCE), 0.75, 0.5);
        DensityFunction var7 = DensityFunctions.add(
            DensityFunctions.add(var6, DensityFunctions.constant(0.37)), DensityFunctions.yClampedGradient(-10, 30, 0.3, 0.0)
        );
        return DensityFunctions.cacheOnce(DensityFunctions.min(var7, DensityFunctions.add(var5, var4)));
    }

    private static DensityFunction noodle() {
        DensityFunction var0 = getFunction(Y);
        int var1 = -64;
        int var2 = -60;
        int var3 = 320;
        DensityFunction var4 = yLimitedInterpolatable(var0, DensityFunctions.noise(getNoise(Noises.NOODLE), 1.0, 1.0), -60, 320, -1);
        DensityFunction var5 = yLimitedInterpolatable(var0, DensityFunctions.mappedNoise(getNoise(Noises.NOODLE_THICKNESS), 1.0, 1.0, -0.05, -0.1), -60, 320, 0);
        double var6 = 2.6666666666666665;
        DensityFunction var7 = yLimitedInterpolatable(
            var0, DensityFunctions.noise(getNoise(Noises.NOODLE_RIDGE_A), 2.6666666666666665, 2.6666666666666665), -60, 320, 0
        );
        DensityFunction var8 = yLimitedInterpolatable(
            var0, DensityFunctions.noise(getNoise(Noises.NOODLE_RIDGE_B), 2.6666666666666665, 2.6666666666666665), -60, 320, 0
        );
        DensityFunction var9 = DensityFunctions.mul(DensityFunctions.constant(1.5), DensityFunctions.max(var7.abs(), var8.abs()));
        return DensityFunctions.rangeChoice(var4, -1000000.0, 0.0, DensityFunctions.constant(64.0), DensityFunctions.add(var5, var9));
    }

    private static DensityFunction pillars() {
        double var0 = 25.0;
        double var1 = 0.3;
        DensityFunction var2 = DensityFunctions.noise(getNoise(Noises.PILLAR), 25.0, 0.3);
        DensityFunction var3 = DensityFunctions.mappedNoise(getNoise(Noises.PILLAR_RARENESS), 0.0, -2.0);
        DensityFunction var4 = DensityFunctions.mappedNoise(getNoise(Noises.PILLAR_THICKNESS), 0.0, 1.1);
        DensityFunction var5 = DensityFunctions.add(DensityFunctions.mul(var2, DensityFunctions.constant(2.0)), var3);
        return DensityFunctions.cacheOnce(DensityFunctions.mul(var5, var4.cube()));
    }

    private static DensityFunction spaghetti2D() {
        DensityFunction var0 = DensityFunctions.noise(getNoise(Noises.SPAGHETTI_2D_MODULATOR), 2.0, 1.0);
        DensityFunction var1 = DensityFunctions.weirdScaledSampler(
            var0, getNoise(Noises.SPAGHETTI_2D), DensityFunctions.WeirdScaledSampler.RarityValueMapper.TYPE2
        );
        DensityFunction var2 = DensityFunctions.mappedNoise(getNoise(Noises.SPAGHETTI_2D_ELEVATION), 0.0, (double)Math.floorDiv(-64, 8), 8.0);
        DensityFunction var3 = getFunction(SPAGHETTI_2D_THICKNESS_MODULATOR);
        DensityFunction var4 = DensityFunctions.add(var2, DensityFunctions.yClampedGradient(-64, 320, 8.0, -40.0)).abs();
        DensityFunction var5 = DensityFunctions.add(var4, var3).cube();
        double var6 = 0.083;
        DensityFunction var7 = DensityFunctions.add(var1, DensityFunctions.mul(DensityFunctions.constant(0.083), var3));
        return DensityFunctions.max(var7, var5).clamp(-1.0, 1.0);
    }

    private static DensityFunction underground(DensityFunction param0) {
        DensityFunction var0 = getFunction(SPAGHETTI_2D);
        DensityFunction var1 = getFunction(SPAGHETTI_ROUGHNESS_FUNCTION);
        DensityFunction var2 = DensityFunctions.noise(getNoise(Noises.CAVE_LAYER), 8.0);
        DensityFunction var3 = DensityFunctions.mul(DensityFunctions.constant(4.0), var2.square());
        DensityFunction var4 = DensityFunctions.noise(getNoise(Noises.CAVE_CHEESE), 0.6666666666666666);
        DensityFunction var5 = DensityFunctions.add(
            DensityFunctions.add(DensityFunctions.constant(0.27), var4).clamp(-1.0, 1.0),
            DensityFunctions.add(DensityFunctions.constant(1.5), DensityFunctions.mul(DensityFunctions.constant(-0.64), param0)).clamp(0.0, 0.5)
        );
        DensityFunction var6 = DensityFunctions.add(var3, var5);
        DensityFunction var7 = DensityFunctions.min(DensityFunctions.min(var6, getFunction(ENTRANCES)), DensityFunctions.add(var0, var1));
        DensityFunction var8 = getFunction(PILLARS);
        DensityFunction var9 = DensityFunctions.rangeChoice(var8, -1000000.0, 0.03, DensityFunctions.constant(-1000000.0), var8);
        return DensityFunctions.max(var7, var9);
    }

    private static DensityFunction finalDensity(NoiseSettings param0, boolean param1, boolean param2, boolean param3) {
        boolean var0 = !param1;
        DensityFunction var1;
        if (param0.islandNoiseOverride()) {
            var1 = getFunction(SLOPED_CHEESE_END);
        } else {
            var1 = getFunction(param3 ? SLOPED_CHEESE_LARGE : SLOPED_CHEESE);
        }

        DensityFunction var3;
        if (var0) {
            var3 = var1;
        } else {
            DensityFunction var4 = DensityFunctions.min(var1, DensityFunctions.mul(DensityFunctions.constant(5.0), getFunction(ENTRANCES)));
            var3 = DensityFunctions.rangeChoice(var1, -1000000.0, 1.5625, var4, underground(var1));
        }

        DensityFunction var6 = DensityFunctions.slide(param0, var3);
        DensityFunction var7 = DensityFunctions.interpolated(DensityFunctions.blendDensity(var6));
        DensityFunction var8 = DensityFunctions.mul(var7, DensityFunctions.constant(0.64));
        DensityFunction var9 = param2 ? getFunction(NOODLE) : DensityFunctions.constant(64.0);
        return DensityFunctions.min(var8.squeeze(), var9);
    }

    protected static NoiseRouterWithOnlyNoises noiseRouter(NoiseSettings param0, boolean param1, boolean param2) {
        DensityFunction var0 = DensityFunctions.noise(getNoise(Noises.AQUIFER_BARRIER), 0.5);
        DensityFunction var1 = DensityFunctions.noise(getNoise(Noises.AQUIFER_FLUID_LEVEL_FLOODEDNESS), 0.67);
        DensityFunction var2 = DensityFunctions.noise(getNoise(Noises.AQUIFER_FLUID_LEVEL_SPREAD), 0.7142857142857143);
        DensityFunction var3 = DensityFunctions.noise(getNoise(Noises.AQUIFER_LAVA));
        boolean var4 = param0.largeBiomes();
        DensityFunction var5 = getFunction(SHIFT_X);
        DensityFunction var6 = getFunction(SHIFT_Z);
        DensityFunction var7 = DensityFunctions.shiftedNoise2d(var5, var6, 0.25, getNoise(var4 ? Noises.TEMPERATURE_LARGE : Noises.TEMPERATURE));
        DensityFunction var8 = DensityFunctions.shiftedNoise2d(var5, var6, 0.25, getNoise(var4 ? Noises.VEGETATION_LARGE : Noises.VEGETATION));
        DensityFunction var9;
        if (param0.islandNoiseOverride()) {
            var9 = DensityFunctions.cache2d(DensityFunctions.endIslands(0L));
        } else {
            DensityFunction var10 = getFunction(var4 ? FACTOR_LARGE : FACTOR);
            DensityFunction var11 = getFunction(var4 ? DEPTH_LARGE : DEPTH);
            var9 = noiseGradientDensity(var11, DensityFunctions.cache2d(var10), DensityFunctions.zero());
        }

        DensityFunction var13 = finalDensity(param0, param1, param2, var4);
        DensityFunction var14 = getFunction(Y);
        int var15 = param0.minY();
        int var16 = Stream.of(OreVeinifier.VeinType.values()).mapToInt(param0x -> param0x.minY).min().orElse(var15);
        int var17 = Stream.of(OreVeinifier.VeinType.values()).mapToInt(param0x -> param0x.maxY).max().orElse(var15);
        DensityFunction var18 = yLimitedInterpolatable(var14, DensityFunctions.noise(getNoise(Noises.ORE_VEININESS), 1.5, 1.5), var16, var17, 0);
        float var19 = 4.0F;
        DensityFunction var20 = yLimitedInterpolatable(var14, DensityFunctions.noise(getNoise(Noises.ORE_VEIN_A), 4.0, 4.0), var16, var17, 0).abs();
        DensityFunction var21 = yLimitedInterpolatable(var14, DensityFunctions.noise(getNoise(Noises.ORE_VEIN_B), 4.0, 4.0), var16, var17, 0).abs();
        DensityFunction var22 = DensityFunctions.add(DensityFunctions.constant(-0.08F), DensityFunctions.max(var20, var21));
        DensityFunction var23 = DensityFunctions.noise(getNoise(Noises.ORE_GAP));
        return new NoiseRouterWithOnlyNoises(
            var0,
            var1,
            var2,
            var3,
            var7,
            var8,
            getFunction(var4 ? CONTINENTS_LARGE : CONTINENTS),
            getFunction(var4 ? EROSION_LARGE : EROSION),
            getFunction(var4 ? DEPTH_LARGE : DEPTH),
            getFunction(RIDGES),
            var9,
            var13,
            var18,
            var22,
            var23
        );
    }

    private static NormalNoise seedNoise(
        PositionalRandomFactory param0, Registry<NormalNoise.NoiseParameters> param1, Holder<NormalNoise.NoiseParameters> param2
    ) {
        return Noises.instantiate(param0, param2.unwrapKey().flatMap(param1::getHolder).orElse(param2));
    }

    public static NoiseRouter createNoiseRouter(
        NoiseSettings param0, long param1, Registry<NormalNoise.NoiseParameters> param2, WorldgenRandom.Algorithm param3, NoiseRouterWithOnlyNoises param4
    ) {
        boolean var0 = param3 == WorldgenRandom.Algorithm.LEGACY;
        PositionalRandomFactory var1 = param3.newInstance(param1).forkPositional();
        Map<DensityFunction, DensityFunction> var2 = new HashMap<>();
        DensityFunction.Visitor var3 = param6 -> {
            if (param6 instanceof DensityFunctions.Noise var0x) {
                Holder<NormalNoise.NoiseParameters> var1x = var0x.noiseData();
                return new DensityFunctions.Noise(var1x, seedNoise(var1, param2, var1x), var0x.xzScale(), var0x.yScale());
            } else if (param6 instanceof DensityFunctions.ShiftNoise var2x) {
                Holder<NormalNoise.NoiseParameters> var3x = var2x.noiseData();
                NormalNoise var4x;
                if (var0) {
                    var4x = NormalNoise.create(var1.fromHashOf(Noises.SHIFT.location()), new NormalNoise.NoiseParameters(0, 0.0));
                } else {
                    var4x = seedNoise(var1, param2, var3x);
                }

                return var2x.withNewNoise(var4x);
            } else if (param6 instanceof DensityFunctions.ShiftedNoise var6x) {
                if (var0) {
                    Holder<NormalNoise.NoiseParameters> var7x = var6x.noiseData();
                    if (Objects.equals(var7x.unwrapKey(), Optional.of(Noises.TEMPERATURE))) {
                        NormalNoise var8 = NormalNoise.createLegacyNetherBiome(param3.newInstance(param1), new NormalNoise.NoiseParameters(-7, 1.0, 1.0));
                        return new DensityFunctions.ShiftedNoise(var6x.shiftX(), var6x.shiftY(), var6x.shiftZ(), var6x.xzScale(), var6x.yScale(), var7x, var8);
                    }

                    if (Objects.equals(var7x.unwrapKey(), Optional.of(Noises.VEGETATION))) {
                        NormalNoise var9x = NormalNoise.createLegacyNetherBiome(param3.newInstance(param1 + 1L), new NormalNoise.NoiseParameters(-7, 1.0, 1.0));
                        return new DensityFunctions.ShiftedNoise(var6x.shiftX(), var6x.shiftY(), var6x.shiftZ(), var6x.xzScale(), var6x.yScale(), var7x, var9x);
                    }
                }

                Holder<NormalNoise.NoiseParameters> var10 = var6x.noiseData();
                return new DensityFunctions.ShiftedNoise(
                    var6x.shiftX(), var6x.shiftY(), var6x.shiftZ(), var6x.xzScale(), var6x.yScale(), var10, seedNoise(var1, param2, var10)
                );
            } else if (param6 instanceof DensityFunctions.WeirdScaledSampler var12x) {
                return new DensityFunctions.WeirdScaledSampler(
                    var12x.input(), var12x.noiseData(), seedNoise(var1, param2, var12x.noiseData()), var12x.rarityValueMapper()
                );
            } else if (param6 instanceof BlendedNoise) {
                return var0
                    ? new BlendedNoise(param3.newInstance(param1), param0.noiseSamplingSettings(), param0.getCellWidth(), param0.getCellHeight())
                    : new BlendedNoise(
                        var1.fromHashOf(new ResourceLocation("terrain")), param0.noiseSamplingSettings(), param0.getCellWidth(), param0.getCellHeight()
                    );
            } else if (param6 instanceof DensityFunctions.EndIslandDensityFunction) {
                return new DensityFunctions.EndIslandDensityFunction(param1);
            } else if (param6 instanceof DensityFunctions.TerrainShaperSpline var11x) {
                TerrainShaper var13x = param0.terrainShaper();
                return new DensityFunctions.TerrainShaperSpline(
                    var11x.continentalness(), var11x.erosion(), var11x.weirdness(), var13x, var11x.spline(), var11x.minValue(), var11x.maxValue()
                );
            } else {
                return (DensityFunction)(param6 instanceof DensityFunctions.Slide var14 ? new DensityFunctions.Slide(param0, var14.input()) : param6);
            }
        };
        DensityFunction.Visitor var4 = param2x -> var2.computeIfAbsent(param2x, var3);
        NoiseRouterWithOnlyNoises var5 = param4.mapAll(var4);
        PositionalRandomFactory var6 = var1.fromHashOf(new ResourceLocation("aquifer")).forkPositional();
        PositionalRandomFactory var7 = var1.fromHashOf(new ResourceLocation("ore")).forkPositional();
        return new NoiseRouter(
            var5.barrierNoise(),
            var5.fluidLevelFloodednessNoise(),
            var5.fluidLevelSpreadNoise(),
            var5.lavaNoise(),
            var6,
            var7,
            var5.temperature(),
            var5.vegetation(),
            var5.continents(),
            var5.erosion(),
            var5.depth(),
            var5.ridges(),
            var5.initialDensityWithoutJaggedness(),
            var5.finalDensity(),
            var5.veinToggle(),
            var5.veinRidged(),
            var5.veinGap(),
            new OverworldBiomeBuilder().spawnTarget()
        );
    }

    private static DensityFunction splineWithBlending(
        DensityFunction param0,
        DensityFunction param1,
        DensityFunction param2,
        DensityFunctions.TerrainShaperSpline.SplineType param3,
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

    protected static final class QuantizedSpaghettiRarity {
        protected static double getSphaghettiRarity2D(double param0) {
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

        protected static double getSpaghettiRarity3D(double param0) {
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
