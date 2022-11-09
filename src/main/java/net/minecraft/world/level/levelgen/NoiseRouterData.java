package net.minecraft.world.level.levelgen;

import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.TerrainProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.synth.BlendedNoise;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class NoiseRouterData {
    public static final float GLOBAL_OFFSET = -0.50375F;
    private static final float ORE_THICKNESS = 0.08F;
    private static final double VEININESS_FREQUENCY = 1.5;
    private static final double NOODLE_SPACING_AND_STRAIGHTNESS = 1.5;
    private static final double SURFACE_DENSITY_THRESHOLD = 1.5625;
    private static final double CHEESE_NOISE_TARGET = -0.703125;
    public static final int ISLAND_CHUNK_DISTANCE = 64;
    public static final long ISLAND_CHUNK_DISTANCE_SQR = 4096L;
    private static final DensityFunction BLENDING_FACTOR = DensityFunctions.constant(10.0);
    private static final DensityFunction BLENDING_JAGGEDNESS = DensityFunctions.zero();
    private static final ResourceKey<DensityFunction> ZERO = createKey("zero");
    private static final ResourceKey<DensityFunction> Y = createKey("y");
    private static final ResourceKey<DensityFunction> SHIFT_X = createKey("shift_x");
    private static final ResourceKey<DensityFunction> SHIFT_Z = createKey("shift_z");
    private static final ResourceKey<DensityFunction> BASE_3D_NOISE_OVERWORLD = createKey("overworld/base_3d_noise");
    private static final ResourceKey<DensityFunction> BASE_3D_NOISE_NETHER = createKey("nether/base_3d_noise");
    private static final ResourceKey<DensityFunction> BASE_3D_NOISE_END = createKey("end/base_3d_noise");
    public static final ResourceKey<DensityFunction> CONTINENTS = createKey("overworld/continents");
    public static final ResourceKey<DensityFunction> EROSION = createKey("overworld/erosion");
    public static final ResourceKey<DensityFunction> RIDGES = createKey("overworld/ridges");
    public static final ResourceKey<DensityFunction> RIDGES_FOLDED = createKey("overworld/ridges_folded");
    public static final ResourceKey<DensityFunction> OFFSET = createKey("overworld/offset");
    public static final ResourceKey<DensityFunction> FACTOR = createKey("overworld/factor");
    public static final ResourceKey<DensityFunction> JAGGEDNESS = createKey("overworld/jaggedness");
    public static final ResourceKey<DensityFunction> DEPTH = createKey("overworld/depth");
    private static final ResourceKey<DensityFunction> SLOPED_CHEESE = createKey("overworld/sloped_cheese");
    public static final ResourceKey<DensityFunction> CONTINENTS_LARGE = createKey("overworld_large_biomes/continents");
    public static final ResourceKey<DensityFunction> EROSION_LARGE = createKey("overworld_large_biomes/erosion");
    private static final ResourceKey<DensityFunction> OFFSET_LARGE = createKey("overworld_large_biomes/offset");
    private static final ResourceKey<DensityFunction> FACTOR_LARGE = createKey("overworld_large_biomes/factor");
    private static final ResourceKey<DensityFunction> JAGGEDNESS_LARGE = createKey("overworld_large_biomes/jaggedness");
    private static final ResourceKey<DensityFunction> DEPTH_LARGE = createKey("overworld_large_biomes/depth");
    private static final ResourceKey<DensityFunction> SLOPED_CHEESE_LARGE = createKey("overworld_large_biomes/sloped_cheese");
    private static final ResourceKey<DensityFunction> OFFSET_AMPLIFIED = createKey("overworld_amplified/offset");
    private static final ResourceKey<DensityFunction> FACTOR_AMPLIFIED = createKey("overworld_amplified/factor");
    private static final ResourceKey<DensityFunction> JAGGEDNESS_AMPLIFIED = createKey("overworld_amplified/jaggedness");
    private static final ResourceKey<DensityFunction> DEPTH_AMPLIFIED = createKey("overworld_amplified/depth");
    private static final ResourceKey<DensityFunction> SLOPED_CHEESE_AMPLIFIED = createKey("overworld_amplified/sloped_cheese");
    private static final ResourceKey<DensityFunction> SLOPED_CHEESE_END = createKey("end/sloped_cheese");
    private static final ResourceKey<DensityFunction> SPAGHETTI_ROUGHNESS_FUNCTION = createKey("overworld/caves/spaghetti_roughness_function");
    private static final ResourceKey<DensityFunction> ENTRANCES = createKey("overworld/caves/entrances");
    private static final ResourceKey<DensityFunction> NOODLE = createKey("overworld/caves/noodle");
    private static final ResourceKey<DensityFunction> PILLARS = createKey("overworld/caves/pillars");
    private static final ResourceKey<DensityFunction> SPAGHETTI_2D_THICKNESS_MODULATOR = createKey("overworld/caves/spaghetti_2d_thickness_modulator");
    private static final ResourceKey<DensityFunction> SPAGHETTI_2D = createKey("overworld/caves/spaghetti_2d");

    private static ResourceKey<DensityFunction> createKey(String param0) {
        return ResourceKey.create(Registries.DENSITY_FUNCTION, new ResourceLocation(param0));
    }

    public static Holder<? extends DensityFunction> bootstrap(BootstapContext<DensityFunction> param0) {
        HolderGetter<NormalNoise.NoiseParameters> var0 = param0.lookup(Registries.NOISE);
        HolderGetter<DensityFunction> var1 = param0.lookup(Registries.DENSITY_FUNCTION);
        param0.register(ZERO, DensityFunctions.zero());
        int var2 = DimensionType.MIN_Y * 2;
        int var3 = DimensionType.MAX_Y * 2;
        param0.register(Y, DensityFunctions.yClampedGradient(var2, var3, (double)var2, (double)var3));
        DensityFunction var4 = registerAndWrap(
            param0, SHIFT_X, DensityFunctions.flatCache(DensityFunctions.cache2d(DensityFunctions.shiftA(var0.getOrThrow(Noises.SHIFT))))
        );
        DensityFunction var5 = registerAndWrap(
            param0, SHIFT_Z, DensityFunctions.flatCache(DensityFunctions.cache2d(DensityFunctions.shiftB(var0.getOrThrow(Noises.SHIFT))))
        );
        param0.register(BASE_3D_NOISE_OVERWORLD, BlendedNoise.createUnseeded(0.25, 0.125, 80.0, 160.0, 8.0));
        param0.register(BASE_3D_NOISE_NETHER, BlendedNoise.createUnseeded(0.25, 0.375, 80.0, 60.0, 8.0));
        param0.register(BASE_3D_NOISE_END, BlendedNoise.createUnseeded(0.25, 0.25, 80.0, 160.0, 4.0));
        Holder<DensityFunction> var6 = param0.register(
            CONTINENTS, DensityFunctions.flatCache(DensityFunctions.shiftedNoise2d(var4, var5, 0.25, var0.getOrThrow(Noises.CONTINENTALNESS)))
        );
        Holder<DensityFunction> var7 = param0.register(
            EROSION, DensityFunctions.flatCache(DensityFunctions.shiftedNoise2d(var4, var5, 0.25, var0.getOrThrow(Noises.EROSION)))
        );
        DensityFunction var8 = registerAndWrap(
            param0, RIDGES, DensityFunctions.flatCache(DensityFunctions.shiftedNoise2d(var4, var5, 0.25, var0.getOrThrow(Noises.RIDGE)))
        );
        param0.register(RIDGES_FOLDED, peaksAndValleys(var8));
        DensityFunction var9 = DensityFunctions.noise(var0.getOrThrow(Noises.JAGGED), 1500.0, 0.0);
        registerTerrainNoises(param0, var1, var9, var6, var7, OFFSET, FACTOR, JAGGEDNESS, DEPTH, SLOPED_CHEESE, false);
        Holder<DensityFunction> var10 = param0.register(
            CONTINENTS_LARGE, DensityFunctions.flatCache(DensityFunctions.shiftedNoise2d(var4, var5, 0.25, var0.getOrThrow(Noises.CONTINENTALNESS_LARGE)))
        );
        Holder<DensityFunction> var11 = param0.register(
            EROSION_LARGE, DensityFunctions.flatCache(DensityFunctions.shiftedNoise2d(var4, var5, 0.25, var0.getOrThrow(Noises.EROSION_LARGE)))
        );
        registerTerrainNoises(param0, var1, var9, var10, var11, OFFSET_LARGE, FACTOR_LARGE, JAGGEDNESS_LARGE, DEPTH_LARGE, SLOPED_CHEESE_LARGE, false);
        registerTerrainNoises(
            param0, var1, var9, var6, var7, OFFSET_AMPLIFIED, FACTOR_AMPLIFIED, JAGGEDNESS_AMPLIFIED, DEPTH_AMPLIFIED, SLOPED_CHEESE_AMPLIFIED, true
        );
        param0.register(SLOPED_CHEESE_END, DensityFunctions.add(DensityFunctions.endIslands(0L), getFunction(var1, BASE_3D_NOISE_END)));
        param0.register(SPAGHETTI_ROUGHNESS_FUNCTION, spaghettiRoughnessFunction(var0));
        param0.register(
            SPAGHETTI_2D_THICKNESS_MODULATOR,
            DensityFunctions.cacheOnce(DensityFunctions.mappedNoise(var0.getOrThrow(Noises.SPAGHETTI_2D_THICKNESS), 2.0, 1.0, -0.6, -1.3))
        );
        param0.register(SPAGHETTI_2D, spaghetti2D(var1, var0));
        param0.register(ENTRANCES, entrances(var1, var0));
        param0.register(NOODLE, noodle(var1, var0));
        return param0.register(PILLARS, pillars(var0));
    }

    private static void registerTerrainNoises(
        BootstapContext<DensityFunction> param0,
        HolderGetter<DensityFunction> param1,
        DensityFunction param2,
        Holder<DensityFunction> param3,
        Holder<DensityFunction> param4,
        ResourceKey<DensityFunction> param5,
        ResourceKey<DensityFunction> param6,
        ResourceKey<DensityFunction> param7,
        ResourceKey<DensityFunction> param8,
        ResourceKey<DensityFunction> param9,
        boolean param10
    ) {
        DensityFunctions.Spline.Coordinate var0 = new DensityFunctions.Spline.Coordinate(param3);
        DensityFunctions.Spline.Coordinate var1 = new DensityFunctions.Spline.Coordinate(param4);
        DensityFunctions.Spline.Coordinate var2 = new DensityFunctions.Spline.Coordinate(param1.getOrThrow(RIDGES));
        DensityFunctions.Spline.Coordinate var3 = new DensityFunctions.Spline.Coordinate(param1.getOrThrow(RIDGES_FOLDED));
        DensityFunction var4 = registerAndWrap(
            param0,
            param5,
            splineWithBlending(
                DensityFunctions.add(DensityFunctions.constant(-0.50375F), DensityFunctions.spline(TerrainProvider.overworldOffset(var0, var1, var3, param10))),
                DensityFunctions.blendOffset()
            )
        );
        DensityFunction var5 = registerAndWrap(
            param0, param6, splineWithBlending(DensityFunctions.spline(TerrainProvider.overworldFactor(var0, var1, var2, var3, param10)), BLENDING_FACTOR)
        );
        DensityFunction var6 = registerAndWrap(param0, param8, DensityFunctions.add(DensityFunctions.yClampedGradient(-64, 320, 1.5, -1.5), var4));
        DensityFunction var7 = registerAndWrap(
            param0,
            param7,
            splineWithBlending(DensityFunctions.spline(TerrainProvider.overworldJaggedness(var0, var1, var2, var3, param10)), BLENDING_JAGGEDNESS)
        );
        DensityFunction var8 = DensityFunctions.mul(var7, param2.halfNegative());
        DensityFunction var9 = noiseGradientDensity(var5, DensityFunctions.add(var6, var8));
        param0.register(param9, DensityFunctions.add(var9, getFunction(param1, BASE_3D_NOISE_OVERWORLD)));
    }

    private static DensityFunction registerAndWrap(BootstapContext<DensityFunction> param0, ResourceKey<DensityFunction> param1, DensityFunction param2) {
        return new DensityFunctions.HolderHolder(param0.register(param1, param2));
    }

    private static DensityFunction getFunction(HolderGetter<DensityFunction> param0, ResourceKey<DensityFunction> param1) {
        return new DensityFunctions.HolderHolder(param0.getOrThrow(param1));
    }

    private static DensityFunction peaksAndValleys(DensityFunction param0) {
        return DensityFunctions.mul(
            DensityFunctions.add(
                DensityFunctions.add(param0.abs(), DensityFunctions.constant(-0.6666666666666666)).abs(), DensityFunctions.constant(-0.3333333333333333)
            ),
            DensityFunctions.constant(-3.0)
        );
    }

    public static float peaksAndValleys(float param0) {
        return -(Math.abs(Math.abs(param0) - 0.6666667F) - 0.33333334F) * 3.0F;
    }

    private static DensityFunction spaghettiRoughnessFunction(HolderGetter<NormalNoise.NoiseParameters> param0) {
        DensityFunction var0 = DensityFunctions.noise(param0.getOrThrow(Noises.SPAGHETTI_ROUGHNESS));
        DensityFunction var1 = DensityFunctions.mappedNoise(param0.getOrThrow(Noises.SPAGHETTI_ROUGHNESS_MODULATOR), 0.0, -0.1);
        return DensityFunctions.cacheOnce(DensityFunctions.mul(var1, DensityFunctions.add(var0.abs(), DensityFunctions.constant(-0.4))));
    }

    private static DensityFunction entrances(HolderGetter<DensityFunction> param0, HolderGetter<NormalNoise.NoiseParameters> param1) {
        DensityFunction var0 = DensityFunctions.cacheOnce(DensityFunctions.noise(param1.getOrThrow(Noises.SPAGHETTI_3D_RARITY), 2.0, 1.0));
        DensityFunction var1 = DensityFunctions.mappedNoise(param1.getOrThrow(Noises.SPAGHETTI_3D_THICKNESS), -0.065, -0.088);
        DensityFunction var2 = DensityFunctions.weirdScaledSampler(
            var0, param1.getOrThrow(Noises.SPAGHETTI_3D_1), DensityFunctions.WeirdScaledSampler.RarityValueMapper.TYPE1
        );
        DensityFunction var3 = DensityFunctions.weirdScaledSampler(
            var0, param1.getOrThrow(Noises.SPAGHETTI_3D_2), DensityFunctions.WeirdScaledSampler.RarityValueMapper.TYPE1
        );
        DensityFunction var4 = DensityFunctions.add(DensityFunctions.max(var2, var3), var1).clamp(-1.0, 1.0);
        DensityFunction var5 = getFunction(param0, SPAGHETTI_ROUGHNESS_FUNCTION);
        DensityFunction var6 = DensityFunctions.noise(param1.getOrThrow(Noises.CAVE_ENTRANCE), 0.75, 0.5);
        DensityFunction var7 = DensityFunctions.add(
            DensityFunctions.add(var6, DensityFunctions.constant(0.37)), DensityFunctions.yClampedGradient(-10, 30, 0.3, 0.0)
        );
        return DensityFunctions.cacheOnce(DensityFunctions.min(var7, DensityFunctions.add(var5, var4)));
    }

    private static DensityFunction noodle(HolderGetter<DensityFunction> param0, HolderGetter<NormalNoise.NoiseParameters> param1) {
        DensityFunction var0 = getFunction(param0, Y);
        int var1 = -64;
        int var2 = -60;
        int var3 = 320;
        DensityFunction var4 = yLimitedInterpolatable(var0, DensityFunctions.noise(param1.getOrThrow(Noises.NOODLE), 1.0, 1.0), -60, 320, -1);
        DensityFunction var5 = yLimitedInterpolatable(
            var0, DensityFunctions.mappedNoise(param1.getOrThrow(Noises.NOODLE_THICKNESS), 1.0, 1.0, -0.05, -0.1), -60, 320, 0
        );
        double var6 = 2.6666666666666665;
        DensityFunction var7 = yLimitedInterpolatable(
            var0, DensityFunctions.noise(param1.getOrThrow(Noises.NOODLE_RIDGE_A), 2.6666666666666665, 2.6666666666666665), -60, 320, 0
        );
        DensityFunction var8 = yLimitedInterpolatable(
            var0, DensityFunctions.noise(param1.getOrThrow(Noises.NOODLE_RIDGE_B), 2.6666666666666665, 2.6666666666666665), -60, 320, 0
        );
        DensityFunction var9 = DensityFunctions.mul(DensityFunctions.constant(1.5), DensityFunctions.max(var7.abs(), var8.abs()));
        return DensityFunctions.rangeChoice(var4, -1000000.0, 0.0, DensityFunctions.constant(64.0), DensityFunctions.add(var5, var9));
    }

    private static DensityFunction pillars(HolderGetter<NormalNoise.NoiseParameters> param0) {
        double var0 = 25.0;
        double var1 = 0.3;
        DensityFunction var2 = DensityFunctions.noise(param0.getOrThrow(Noises.PILLAR), 25.0, 0.3);
        DensityFunction var3 = DensityFunctions.mappedNoise(param0.getOrThrow(Noises.PILLAR_RARENESS), 0.0, -2.0);
        DensityFunction var4 = DensityFunctions.mappedNoise(param0.getOrThrow(Noises.PILLAR_THICKNESS), 0.0, 1.1);
        DensityFunction var5 = DensityFunctions.add(DensityFunctions.mul(var2, DensityFunctions.constant(2.0)), var3);
        return DensityFunctions.cacheOnce(DensityFunctions.mul(var5, var4.cube()));
    }

    private static DensityFunction spaghetti2D(HolderGetter<DensityFunction> param0, HolderGetter<NormalNoise.NoiseParameters> param1) {
        DensityFunction var0 = DensityFunctions.noise(param1.getOrThrow(Noises.SPAGHETTI_2D_MODULATOR), 2.0, 1.0);
        DensityFunction var1 = DensityFunctions.weirdScaledSampler(
            var0, param1.getOrThrow(Noises.SPAGHETTI_2D), DensityFunctions.WeirdScaledSampler.RarityValueMapper.TYPE2
        );
        DensityFunction var2 = DensityFunctions.mappedNoise(param1.getOrThrow(Noises.SPAGHETTI_2D_ELEVATION), 0.0, (double)Math.floorDiv(-64, 8), 8.0);
        DensityFunction var3 = getFunction(param0, SPAGHETTI_2D_THICKNESS_MODULATOR);
        DensityFunction var4 = DensityFunctions.add(var2, DensityFunctions.yClampedGradient(-64, 320, 8.0, -40.0)).abs();
        DensityFunction var5 = DensityFunctions.add(var4, var3).cube();
        double var6 = 0.083;
        DensityFunction var7 = DensityFunctions.add(var1, DensityFunctions.mul(DensityFunctions.constant(0.083), var3));
        return DensityFunctions.max(var7, var5).clamp(-1.0, 1.0);
    }

    private static DensityFunction underground(HolderGetter<DensityFunction> param0, HolderGetter<NormalNoise.NoiseParameters> param1, DensityFunction param2) {
        DensityFunction var0 = getFunction(param0, SPAGHETTI_2D);
        DensityFunction var1 = getFunction(param0, SPAGHETTI_ROUGHNESS_FUNCTION);
        DensityFunction var2 = DensityFunctions.noise(param1.getOrThrow(Noises.CAVE_LAYER), 8.0);
        DensityFunction var3 = DensityFunctions.mul(DensityFunctions.constant(4.0), var2.square());
        DensityFunction var4 = DensityFunctions.noise(param1.getOrThrow(Noises.CAVE_CHEESE), 0.6666666666666666);
        DensityFunction var5 = DensityFunctions.add(
            DensityFunctions.add(DensityFunctions.constant(0.27), var4).clamp(-1.0, 1.0),
            DensityFunctions.add(DensityFunctions.constant(1.5), DensityFunctions.mul(DensityFunctions.constant(-0.64), param2)).clamp(0.0, 0.5)
        );
        DensityFunction var6 = DensityFunctions.add(var3, var5);
        DensityFunction var7 = DensityFunctions.min(DensityFunctions.min(var6, getFunction(param0, ENTRANCES)), DensityFunctions.add(var0, var1));
        DensityFunction var8 = getFunction(param0, PILLARS);
        DensityFunction var9 = DensityFunctions.rangeChoice(var8, -1000000.0, 0.03, DensityFunctions.constant(-1000000.0), var8);
        return DensityFunctions.max(var7, var9);
    }

    private static DensityFunction postProcess(DensityFunction param0) {
        DensityFunction var0 = DensityFunctions.blendDensity(param0);
        return DensityFunctions.mul(DensityFunctions.interpolated(var0), DensityFunctions.constant(0.64)).squeeze();
    }

    protected static NoiseRouter overworld(
        HolderGetter<DensityFunction> param0, HolderGetter<NormalNoise.NoiseParameters> param1, boolean param2, boolean param3
    ) {
        DensityFunction var0 = DensityFunctions.noise(param1.getOrThrow(Noises.AQUIFER_BARRIER), 0.5);
        DensityFunction var1 = DensityFunctions.noise(param1.getOrThrow(Noises.AQUIFER_FLUID_LEVEL_FLOODEDNESS), 0.67);
        DensityFunction var2 = DensityFunctions.noise(param1.getOrThrow(Noises.AQUIFER_FLUID_LEVEL_SPREAD), 0.7142857142857143);
        DensityFunction var3 = DensityFunctions.noise(param1.getOrThrow(Noises.AQUIFER_LAVA));
        DensityFunction var4 = getFunction(param0, SHIFT_X);
        DensityFunction var5 = getFunction(param0, SHIFT_Z);
        DensityFunction var6 = DensityFunctions.shiftedNoise2d(var4, var5, 0.25, param1.getOrThrow(param2 ? Noises.TEMPERATURE_LARGE : Noises.TEMPERATURE));
        DensityFunction var7 = DensityFunctions.shiftedNoise2d(var4, var5, 0.25, param1.getOrThrow(param2 ? Noises.VEGETATION_LARGE : Noises.VEGETATION));
        DensityFunction var8 = getFunction(param0, param2 ? FACTOR_LARGE : (param3 ? FACTOR_AMPLIFIED : FACTOR));
        DensityFunction var9 = getFunction(param0, param2 ? DEPTH_LARGE : (param3 ? DEPTH_AMPLIFIED : DEPTH));
        DensityFunction var10 = noiseGradientDensity(DensityFunctions.cache2d(var8), var9);
        DensityFunction var11 = getFunction(param0, param2 ? SLOPED_CHEESE_LARGE : (param3 ? SLOPED_CHEESE_AMPLIFIED : SLOPED_CHEESE));
        DensityFunction var12 = DensityFunctions.min(var11, DensityFunctions.mul(DensityFunctions.constant(5.0), getFunction(param0, ENTRANCES)));
        DensityFunction var13 = DensityFunctions.rangeChoice(var11, -1000000.0, 1.5625, var12, underground(param0, param1, var11));
        DensityFunction var14 = DensityFunctions.min(postProcess(slideOverworld(param3, var13)), getFunction(param0, NOODLE));
        DensityFunction var15 = getFunction(param0, Y);
        int var16 = Stream.of(OreVeinifier.VeinType.values()).mapToInt(param0x -> param0x.minY).min().orElse(-DimensionType.MIN_Y * 2);
        int var17 = Stream.of(OreVeinifier.VeinType.values()).mapToInt(param0x -> param0x.maxY).max().orElse(-DimensionType.MIN_Y * 2);
        DensityFunction var18 = yLimitedInterpolatable(var15, DensityFunctions.noise(param1.getOrThrow(Noises.ORE_VEININESS), 1.5, 1.5), var16, var17, 0);
        float var19 = 4.0F;
        DensityFunction var20 = yLimitedInterpolatable(var15, DensityFunctions.noise(param1.getOrThrow(Noises.ORE_VEIN_A), 4.0, 4.0), var16, var17, 0).abs();
        DensityFunction var21 = yLimitedInterpolatable(var15, DensityFunctions.noise(param1.getOrThrow(Noises.ORE_VEIN_B), 4.0, 4.0), var16, var17, 0).abs();
        DensityFunction var22 = DensityFunctions.add(DensityFunctions.constant(-0.08F), DensityFunctions.max(var20, var21));
        DensityFunction var23 = DensityFunctions.noise(param1.getOrThrow(Noises.ORE_GAP));
        return new NoiseRouter(
            var0,
            var1,
            var2,
            var3,
            var6,
            var7,
            getFunction(param0, param2 ? CONTINENTS_LARGE : CONTINENTS),
            getFunction(param0, param2 ? EROSION_LARGE : EROSION),
            var9,
            getFunction(param0, RIDGES),
            slideOverworld(param3, DensityFunctions.add(var10, DensityFunctions.constant(-0.703125)).clamp(-64.0, 64.0)),
            var14,
            var18,
            var22,
            var23
        );
    }

    private static NoiseRouter noNewCaves(HolderGetter<DensityFunction> param0, HolderGetter<NormalNoise.NoiseParameters> param1, DensityFunction param2) {
        DensityFunction var0 = getFunction(param0, SHIFT_X);
        DensityFunction var1 = getFunction(param0, SHIFT_Z);
        DensityFunction var2 = DensityFunctions.shiftedNoise2d(var0, var1, 0.25, param1.getOrThrow(Noises.TEMPERATURE));
        DensityFunction var3 = DensityFunctions.shiftedNoise2d(var0, var1, 0.25, param1.getOrThrow(Noises.VEGETATION));
        DensityFunction var4 = postProcess(param2);
        return new NoiseRouter(
            DensityFunctions.zero(),
            DensityFunctions.zero(),
            DensityFunctions.zero(),
            DensityFunctions.zero(),
            var2,
            var3,
            DensityFunctions.zero(),
            DensityFunctions.zero(),
            DensityFunctions.zero(),
            DensityFunctions.zero(),
            DensityFunctions.zero(),
            var4,
            DensityFunctions.zero(),
            DensityFunctions.zero(),
            DensityFunctions.zero()
        );
    }

    private static DensityFunction slideOverworld(boolean param0, DensityFunction param1) {
        return slide(param1, -64, 384, param0 ? 16 : 80, param0 ? 0 : 64, -0.078125, 0, 24, param0 ? 0.4 : 0.1171875);
    }

    private static DensityFunction slideNetherLike(HolderGetter<DensityFunction> param0, int param1, int param2) {
        return slide(getFunction(param0, BASE_3D_NOISE_NETHER), param1, param2, 24, 0, 0.9375, -8, 24, 2.5);
    }

    private static DensityFunction slideEndLike(DensityFunction param0, int param1, int param2) {
        return slide(param0, param1, param2, 72, -184, -23.4375, 4, 32, -0.234375);
    }

    protected static NoiseRouter nether(HolderGetter<DensityFunction> param0, HolderGetter<NormalNoise.NoiseParameters> param1) {
        return noNewCaves(param0, param1, slideNetherLike(param0, 0, 128));
    }

    protected static NoiseRouter caves(HolderGetter<DensityFunction> param0, HolderGetter<NormalNoise.NoiseParameters> param1) {
        return noNewCaves(param0, param1, slideNetherLike(param0, -64, 192));
    }

    protected static NoiseRouter floatingIslands(HolderGetter<DensityFunction> param0, HolderGetter<NormalNoise.NoiseParameters> param1) {
        return noNewCaves(param0, param1, slideEndLike(getFunction(param0, BASE_3D_NOISE_END), 0, 256));
    }

    private static DensityFunction slideEnd(DensityFunction param0) {
        return slideEndLike(param0, 0, 128);
    }

    protected static NoiseRouter end(HolderGetter<DensityFunction> param0) {
        DensityFunction var0 = DensityFunctions.cache2d(DensityFunctions.endIslands(0L));
        DensityFunction var1 = postProcess(slideEnd(getFunction(param0, SLOPED_CHEESE_END)));
        return new NoiseRouter(
            DensityFunctions.zero(),
            DensityFunctions.zero(),
            DensityFunctions.zero(),
            DensityFunctions.zero(),
            DensityFunctions.zero(),
            DensityFunctions.zero(),
            DensityFunctions.zero(),
            var0,
            DensityFunctions.zero(),
            DensityFunctions.zero(),
            slideEnd(DensityFunctions.add(var0, DensityFunctions.constant(-0.703125))),
            var1,
            DensityFunctions.zero(),
            DensityFunctions.zero(),
            DensityFunctions.zero()
        );
    }

    protected static NoiseRouter none() {
        return new NoiseRouter(
            DensityFunctions.zero(),
            DensityFunctions.zero(),
            DensityFunctions.zero(),
            DensityFunctions.zero(),
            DensityFunctions.zero(),
            DensityFunctions.zero(),
            DensityFunctions.zero(),
            DensityFunctions.zero(),
            DensityFunctions.zero(),
            DensityFunctions.zero(),
            DensityFunctions.zero(),
            DensityFunctions.zero(),
            DensityFunctions.zero(),
            DensityFunctions.zero(),
            DensityFunctions.zero()
        );
    }

    private static DensityFunction splineWithBlending(DensityFunction param0, DensityFunction param1) {
        DensityFunction var0 = DensityFunctions.lerp(DensityFunctions.blendAlpha(), param1, param0);
        return DensityFunctions.flatCache(DensityFunctions.cache2d(var0));
    }

    private static DensityFunction noiseGradientDensity(DensityFunction param0, DensityFunction param1) {
        DensityFunction var0 = DensityFunctions.mul(param1, param0);
        return DensityFunctions.mul(DensityFunctions.constant(4.0), var0.quarterNegative());
    }

    private static DensityFunction yLimitedInterpolatable(DensityFunction param0, DensityFunction param1, int param2, int param3, int param4) {
        return DensityFunctions.interpolated(
            DensityFunctions.rangeChoice(param0, (double)param2, (double)(param3 + 1), param1, DensityFunctions.constant((double)param4))
        );
    }

    private static DensityFunction slide(
        DensityFunction param0, int param1, int param2, int param3, int param4, double param5, int param6, int param7, double param8
    ) {
        DensityFunction var1 = DensityFunctions.yClampedGradient(param1 + param2 - param3, param1 + param2 - param4, 1.0, 0.0);
        DensityFunction var0 = DensityFunctions.lerp(var1, param5, param0);
        DensityFunction var2 = DensityFunctions.yClampedGradient(param1 + param6, param1 + param7, 0.0, 1.0);
        return DensityFunctions.lerp(var2, param8, var0);
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
