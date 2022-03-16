package net.minecraft.world.level.levelgen;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.TerrainProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.synth.BlendedNoise;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class NoiseRouterData {
    public static final float GLOBAL_OFFSET = -0.50375F;
    private static final float ORE_THICKNESS = 0.08F;
    private static final double VEININESS_FREQUENCY = 1.5;
    private static final double NOODLE_SPACING_AND_STRAIGHTNESS = 1.5;
    private static final double SURFACE_DENSITY_THRESHOLD = 1.5625;
    public static final int ISLAND_CHUNK_DISTANCE = 64;
    public static final long ISLAND_CHUNK_DISTANCE_SQR = 4096L;
    private static final DensityFunction BLENDING_FACTOR = DensityFunctions.constant(10.0);
    private static final DensityFunction BLENDING_JAGGEDNESS = DensityFunctions.zero();
    private static final ResourceKey<DensityFunction> ZERO = createKey("zero");
    private static final ResourceKey<DensityFunction> Y = createKey("y");
    private static final ResourceKey<DensityFunction> SHIFT_X = createKey("shift_x");
    private static final ResourceKey<DensityFunction> SHIFT_Z = createKey("shift_z");
    private static final ResourceKey<DensityFunction> BASE_3D_NOISE = createKey("overworld/base_3d_noise");
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
        return ResourceKey.create(Registry.DENSITY_FUNCTION_REGISTRY, new ResourceLocation(param0));
    }

    public static Holder<? extends DensityFunction> bootstrap() {
        register(ZERO, DensityFunctions.zero());
        int var0 = DimensionType.MIN_Y * 2;
        int var1 = DimensionType.MAX_Y * 2;
        register(Y, DensityFunctions.yClampedGradient(var0, var1, (double)var0, (double)var1));
        DensityFunction var2 = registerAndWrap(SHIFT_X, DensityFunctions.flatCache(DensityFunctions.cache2d(DensityFunctions.shiftA(getNoise(Noises.SHIFT)))));
        DensityFunction var3 = registerAndWrap(SHIFT_Z, DensityFunctions.flatCache(DensityFunctions.cache2d(DensityFunctions.shiftB(getNoise(Noises.SHIFT)))));
        register(BASE_3D_NOISE, BlendedNoise.UNSEEDED);
        Holder<DensityFunction> var4 = register(
            CONTINENTS, DensityFunctions.flatCache(DensityFunctions.shiftedNoise2d(var2, var3, 0.25, getNoise(Noises.CONTINENTALNESS)))
        );
        Holder<DensityFunction> var5 = register(
            EROSION, DensityFunctions.flatCache(DensityFunctions.shiftedNoise2d(var2, var3, 0.25, getNoise(Noises.EROSION)))
        );
        DensityFunction var6 = registerAndWrap(RIDGES, DensityFunctions.flatCache(DensityFunctions.shiftedNoise2d(var2, var3, 0.25, getNoise(Noises.RIDGE))));
        register(RIDGES_FOLDED, peaksAndValleys(var6));
        DensityFunction var7 = DensityFunctions.noise(getNoise(Noises.JAGGED), 1500.0, 0.0);
        registerTerrainNoises(var7, var4, var5, OFFSET, FACTOR, JAGGEDNESS, DEPTH, SLOPED_CHEESE, false);
        Holder<DensityFunction> var8 = register(
            CONTINENTS_LARGE, DensityFunctions.flatCache(DensityFunctions.shiftedNoise2d(var2, var3, 0.25, getNoise(Noises.CONTINENTALNESS_LARGE)))
        );
        Holder<DensityFunction> var9 = register(
            EROSION_LARGE, DensityFunctions.flatCache(DensityFunctions.shiftedNoise2d(var2, var3, 0.25, getNoise(Noises.EROSION_LARGE)))
        );
        registerTerrainNoises(var7, var8, var9, OFFSET_LARGE, FACTOR_LARGE, JAGGEDNESS_LARGE, DEPTH_LARGE, SLOPED_CHEESE_LARGE, false);
        registerTerrainNoises(var7, var4, var5, OFFSET_AMPLIFIED, FACTOR_AMPLIFIED, JAGGEDNESS_AMPLIFIED, DEPTH_AMPLIFIED, SLOPED_CHEESE_AMPLIFIED, true);
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

    private static void registerTerrainNoises(
        DensityFunction param0,
        Holder<DensityFunction> param1,
        Holder<DensityFunction> param2,
        ResourceKey<DensityFunction> param3,
        ResourceKey<DensityFunction> param4,
        ResourceKey<DensityFunction> param5,
        ResourceKey<DensityFunction> param6,
        ResourceKey<DensityFunction> param7,
        boolean param8
    ) {
        DensityFunctions.Spline.Coordinate var0 = new DensityFunctions.Spline.Coordinate(param1);
        DensityFunctions.Spline.Coordinate var1 = new DensityFunctions.Spline.Coordinate(param2);
        DensityFunctions.Spline.Coordinate var2 = new DensityFunctions.Spline.Coordinate(BuiltinRegistries.DENSITY_FUNCTION.getHolderOrThrow(RIDGES));
        DensityFunctions.Spline.Coordinate var3 = new DensityFunctions.Spline.Coordinate(BuiltinRegistries.DENSITY_FUNCTION.getHolderOrThrow(RIDGES_FOLDED));
        DensityFunction var4 = registerAndWrap(
            param3,
            splineWithBlending(
                DensityFunctions.add(DensityFunctions.constant(-0.50375F), DensityFunctions.spline(TerrainProvider.overworldOffset(var0, var1, var3, param8))),
                DensityFunctions.blendOffset()
            )
        );
        DensityFunction var5 = registerAndWrap(
            param4, splineWithBlending(DensityFunctions.spline(TerrainProvider.overworldFactor(var0, var1, var2, var3, param8)), BLENDING_FACTOR)
        );
        DensityFunction var6 = registerAndWrap(param6, DensityFunctions.add(DensityFunctions.yClampedGradient(-64, 320, 1.5, -1.5), var4));
        DensityFunction var7 = registerAndWrap(
            param5, splineWithBlending(DensityFunctions.spline(TerrainProvider.overworldJaggedness(var0, var1, var2, var3, param8)), BLENDING_JAGGEDNESS)
        );
        DensityFunction var8 = DensityFunctions.mul(var7, param0.halfNegative());
        DensityFunction var9 = noiseGradientDensity(var5, DensityFunctions.add(var6, var8));
        register(param7, DensityFunctions.add(var9, getFunction(BASE_3D_NOISE)));
    }

    private static DensityFunction registerAndWrap(ResourceKey<DensityFunction> param0, DensityFunction param1) {
        return new DensityFunctions.HolderHolder(BuiltinRegistries.register(BuiltinRegistries.DENSITY_FUNCTION, param0, param1));
    }

    private static Holder<DensityFunction> register(ResourceKey<DensityFunction> param0, DensityFunction param1) {
        return BuiltinRegistries.register(BuiltinRegistries.DENSITY_FUNCTION, param0, param1);
    }

    private static Holder<NormalNoise.NoiseParameters> getNoise(ResourceKey<NormalNoise.NoiseParameters> param0) {
        return BuiltinRegistries.NOISE.getHolderOrThrow(param0);
    }

    private static DensityFunction getFunction(ResourceKey<DensityFunction> param0) {
        return new DensityFunctions.HolderHolder(BuiltinRegistries.DENSITY_FUNCTION.getHolderOrThrow(param0));
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

    private static DensityFunction postProcess(NoiseSettings param0, DensityFunction param1) {
        DensityFunction var0 = DensityFunctions.slide(param0, param1);
        DensityFunction var1 = DensityFunctions.blendDensity(var0);
        return DensityFunctions.mul(DensityFunctions.interpolated(var1), DensityFunctions.constant(0.64)).squeeze();
    }

    protected static NoiseRouter overworld(NoiseSettings param0, boolean param1, boolean param2) {
        DensityFunction var0 = DensityFunctions.noise(getNoise(Noises.AQUIFER_BARRIER), 0.5);
        DensityFunction var1 = DensityFunctions.noise(getNoise(Noises.AQUIFER_FLUID_LEVEL_FLOODEDNESS), 0.67);
        DensityFunction var2 = DensityFunctions.noise(getNoise(Noises.AQUIFER_FLUID_LEVEL_SPREAD), 0.7142857142857143);
        DensityFunction var3 = DensityFunctions.noise(getNoise(Noises.AQUIFER_LAVA));
        DensityFunction var4 = getFunction(SHIFT_X);
        DensityFunction var5 = getFunction(SHIFT_Z);
        DensityFunction var6 = DensityFunctions.shiftedNoise2d(var4, var5, 0.25, getNoise(param1 ? Noises.TEMPERATURE_LARGE : Noises.TEMPERATURE));
        DensityFunction var7 = DensityFunctions.shiftedNoise2d(var4, var5, 0.25, getNoise(param1 ? Noises.VEGETATION_LARGE : Noises.VEGETATION));
        DensityFunction var8 = getFunction(param1 ? FACTOR_LARGE : (param2 ? FACTOR_AMPLIFIED : FACTOR));
        DensityFunction var9 = getFunction(param1 ? DEPTH_LARGE : (param2 ? DEPTH_AMPLIFIED : DEPTH));
        DensityFunction var10 = noiseGradientDensity(DensityFunctions.cache2d(var8), var9);
        DensityFunction var11 = getFunction(param1 ? SLOPED_CHEESE_LARGE : (param2 ? SLOPED_CHEESE_AMPLIFIED : SLOPED_CHEESE));
        DensityFunction var12 = DensityFunctions.min(var11, DensityFunctions.mul(DensityFunctions.constant(5.0), getFunction(ENTRANCES)));
        DensityFunction var13 = DensityFunctions.rangeChoice(var11, -1000000.0, 1.5625, var12, underground(var11));
        DensityFunction var14 = DensityFunctions.min(postProcess(param0, var13), getFunction(NOODLE));
        DensityFunction var15 = getFunction(Y);
        int var16 = param0.minY();
        int var17 = Stream.of(OreVeinifier.VeinType.values()).mapToInt(param0x -> param0x.minY).min().orElse(var16);
        int var18 = Stream.of(OreVeinifier.VeinType.values()).mapToInt(param0x -> param0x.maxY).max().orElse(var16);
        DensityFunction var19 = yLimitedInterpolatable(var15, DensityFunctions.noise(getNoise(Noises.ORE_VEININESS), 1.5, 1.5), var17, var18, 0);
        float var20 = 4.0F;
        DensityFunction var21 = yLimitedInterpolatable(var15, DensityFunctions.noise(getNoise(Noises.ORE_VEIN_A), 4.0, 4.0), var17, var18, 0).abs();
        DensityFunction var22 = yLimitedInterpolatable(var15, DensityFunctions.noise(getNoise(Noises.ORE_VEIN_B), 4.0, 4.0), var17, var18, 0).abs();
        DensityFunction var23 = DensityFunctions.add(DensityFunctions.constant(-0.08F), DensityFunctions.max(var21, var22));
        DensityFunction var24 = DensityFunctions.noise(getNoise(Noises.ORE_GAP));
        return new NoiseRouter(
            var0,
            var1,
            var2,
            var3,
            var6,
            var7,
            getFunction(param1 ? CONTINENTS_LARGE : CONTINENTS),
            getFunction(param1 ? EROSION_LARGE : EROSION),
            var9,
            getFunction(RIDGES),
            var10,
            var14,
            var19,
            var23,
            var24
        );
    }

    private static NoiseRouter noNewCaves(NoiseSettings param0) {
        DensityFunction var0 = getFunction(SHIFT_X);
        DensityFunction var1 = getFunction(SHIFT_Z);
        DensityFunction var2 = DensityFunctions.shiftedNoise2d(var0, var1, 0.25, getNoise(Noises.TEMPERATURE));
        DensityFunction var3 = DensityFunctions.shiftedNoise2d(var0, var1, 0.25, getNoise(Noises.VEGETATION));
        DensityFunction var4 = postProcess(param0, getFunction(BASE_3D_NOISE));
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

    protected static NoiseRouter caves(NoiseSettings param0) {
        return noNewCaves(param0);
    }

    protected static NoiseRouter floatingIslands(NoiseSettings param0) {
        return noNewCaves(param0);
    }

    protected static NoiseRouter nether(NoiseSettings param0) {
        return noNewCaves(param0);
    }

    protected static NoiseRouter end(NoiseSettings param0) {
        DensityFunction var0 = DensityFunctions.cache2d(DensityFunctions.endIslands(0L));
        DensityFunction var1 = postProcess(param0, getFunction(SLOPED_CHEESE_END));
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
            var0,
            var1,
            DensityFunctions.zero(),
            DensityFunctions.zero(),
            DensityFunctions.zero()
        );
    }

    static NormalNoise seedNoise(PositionalRandomFactory param0, Registry<NormalNoise.NoiseParameters> param1, Holder<NormalNoise.NoiseParameters> param2) {
        return Noises.instantiate(param0, param2.unwrapKey().flatMap(param1::getHolder).orElse(param2));
    }

    public static NoiseRouter createNoiseRouter(
        final NoiseSettings param0, final Registry<NormalNoise.NoiseParameters> param1, NoiseRouter param2, final RandomWithLegacy param3
    ) {
        final PositionalRandomFactory var0 = param3.random();
        final boolean var1 = param3.useLegacyInit();

        class NoiseWiringHelper implements DensityFunction.Visitor {
            private final Map<DensityFunction, DensityFunction> wrapped = new HashMap<>();

            private DensityFunction wrapNew(DensityFunction param0x) {
                if (param0 instanceof DensityFunctions.Noise var0) {
                    Holder<NormalNoise.NoiseParameters> var1 = var0.noiseData();
                    return new DensityFunctions.Noise(var1, NoiseRouterData.seedNoise(var0, param1, var1), var0.xzScale(), var0.yScale());
                } else if (param0 instanceof DensityFunctions.ShiftNoise var2) {
                    Holder<NormalNoise.NoiseParameters> var3 = var2.noiseData();
                    NormalNoise var4;
                    if (var1) {
                        var4 = NormalNoise.create(var0.fromHashOf(Noises.SHIFT.location()), new NormalNoise.NoiseParameters(0, 0.0));
                    } else {
                        var4 = NoiseRouterData.seedNoise(var0, param1, var3);
                    }

                    return var2.withNewNoise(var4);
                } else if (param0 instanceof DensityFunctions.ShiftedNoise var6) {
                    if (var1) {
                        Holder<NormalNoise.NoiseParameters> var7 = var6.noiseData();
                        if (Objects.equals(var7.unwrapKey(), Optional.of(Noises.TEMPERATURE))) {
                            NormalNoise var8 = NormalNoise.createLegacyNetherBiome(param3.newLegacyInstance(0L), new NormalNoise.NoiseParameters(-7, 1.0, 1.0));
                            return new DensityFunctions.ShiftedNoise(var6.shiftX(), var6.shiftY(), var6.shiftZ(), var6.xzScale(), var6.yScale(), var7, var8);
                        }

                        if (Objects.equals(var7.unwrapKey(), Optional.of(Noises.VEGETATION))) {
                            NormalNoise var9 = NormalNoise.createLegacyNetherBiome(param3.newLegacyInstance(1L), new NormalNoise.NoiseParameters(-7, 1.0, 1.0));
                            return new DensityFunctions.ShiftedNoise(var6.shiftX(), var6.shiftY(), var6.shiftZ(), var6.xzScale(), var6.yScale(), var7, var9);
                        }
                    }

                    Holder<NormalNoise.NoiseParameters> var10 = var6.noiseData();
                    return new DensityFunctions.ShiftedNoise(
                        var6.shiftX(), var6.shiftY(), var6.shiftZ(), var6.xzScale(), var6.yScale(), var10, NoiseRouterData.seedNoise(var0, param1, var10)
                    );
                } else if (param0 instanceof DensityFunctions.WeirdScaledSampler var11) {
                    return new DensityFunctions.WeirdScaledSampler(
                        var11.input(), var11.noiseData(), NoiseRouterData.seedNoise(var0, param1, var11.noiseData()), var11.rarityValueMapper()
                    );
                } else if (param0 instanceof BlendedNoise) {
                    RandomSource var12 = var1 ? param3.newLegacyInstance(0L) : var0.fromHashOf(new ResourceLocation("terrain"));
                    return new BlendedNoise(var12, param0.noiseSamplingSettings(), param0.getCellWidth(), param0.getCellHeight());
                } else if (param0 instanceof DensityFunctions.EndIslandDensityFunction) {
                    return new DensityFunctions.EndIslandDensityFunction(param3.legacyLevelSeed());
                } else {
                    return (DensityFunction)(param0 instanceof DensityFunctions.Slide var13 ? new DensityFunctions.Slide(param0, var13.input()) : param0);
                }
            }

            public DensityFunction apply(DensityFunction param0x) {
                return this.wrapped.computeIfAbsent(param0, this::wrapNew);
            }
        }

        return param2.mapAll(new NoiseWiringHelper());
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
