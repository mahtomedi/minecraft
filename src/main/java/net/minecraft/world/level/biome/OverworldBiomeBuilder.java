package net.minecraft.world.level.biome;

import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.SharedConstants;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.data.worldgen.TerrainProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.CubicSpline;
import net.minecraft.util.ToFloatFunction;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.NoiseRouterData;

public final class OverworldBiomeBuilder {
    private static final float VALLEY_SIZE = 0.05F;
    private static final float LOW_START = 0.26666668F;
    public static final float HIGH_START = 0.4F;
    private static final float HIGH_END = 0.93333334F;
    private static final float PEAK_SIZE = 0.1F;
    public static final float PEAK_START = 0.56666666F;
    private static final float PEAK_END = 0.7666667F;
    public static final float NEAR_INLAND_START = -0.11F;
    public static final float MID_INLAND_START = 0.03F;
    public static final float FAR_INLAND_START = 0.3F;
    public static final float EROSION_INDEX_1_START = -0.78F;
    public static final float EROSION_INDEX_2_START = -0.375F;
    private static final float EROSION_DEEP_DARK_DRYNESS_THRESHOLD = -0.225F;
    private static final float DEPTH_DEEP_DARK_DRYNESS_THRESHOLD = 0.9F;
    private final Climate.Parameter FULL_RANGE = Climate.Parameter.span(-1.0F, 1.0F);
    private final Climate.Parameter[] temperatures = new Climate.Parameter[]{
        Climate.Parameter.span(-1.0F, -0.45F),
        Climate.Parameter.span(-0.45F, -0.15F),
        Climate.Parameter.span(-0.15F, 0.2F),
        Climate.Parameter.span(0.2F, 0.55F),
        Climate.Parameter.span(0.55F, 1.0F)
    };
    private final Climate.Parameter[] humidities = new Climate.Parameter[]{
        Climate.Parameter.span(-1.0F, -0.35F),
        Climate.Parameter.span(-0.35F, -0.1F),
        Climate.Parameter.span(-0.1F, 0.1F),
        Climate.Parameter.span(0.1F, 0.3F),
        Climate.Parameter.span(0.3F, 1.0F)
    };
    private final Climate.Parameter[] erosions = new Climate.Parameter[]{
        Climate.Parameter.span(-1.0F, -0.78F),
        Climate.Parameter.span(-0.78F, -0.375F),
        Climate.Parameter.span(-0.375F, -0.2225F),
        Climate.Parameter.span(-0.2225F, 0.05F),
        Climate.Parameter.span(0.05F, 0.45F),
        Climate.Parameter.span(0.45F, 0.55F),
        Climate.Parameter.span(0.55F, 1.0F)
    };
    private final Climate.Parameter FROZEN_RANGE = this.temperatures[0];
    private final Climate.Parameter UNFROZEN_RANGE = Climate.Parameter.span(this.temperatures[1], this.temperatures[4]);
    private final Climate.Parameter mushroomFieldsContinentalness = Climate.Parameter.span(-1.2F, -1.05F);
    private final Climate.Parameter deepOceanContinentalness = Climate.Parameter.span(-1.05F, -0.455F);
    private final Climate.Parameter oceanContinentalness = Climate.Parameter.span(-0.455F, -0.19F);
    private final Climate.Parameter coastContinentalness = Climate.Parameter.span(-0.19F, -0.11F);
    private final Climate.Parameter inlandContinentalness = Climate.Parameter.span(-0.11F, 0.55F);
    private final Climate.Parameter nearInlandContinentalness = Climate.Parameter.span(-0.11F, 0.03F);
    private final Climate.Parameter midInlandContinentalness = Climate.Parameter.span(0.03F, 0.3F);
    private final Climate.Parameter farInlandContinentalness = Climate.Parameter.span(0.3F, 1.0F);
    private final ResourceKey<Biome>[][] OCEANS = new ResourceKey[][]{
        {Biomes.DEEP_FROZEN_OCEAN, Biomes.DEEP_COLD_OCEAN, Biomes.DEEP_OCEAN, Biomes.DEEP_LUKEWARM_OCEAN, Biomes.WARM_OCEAN},
        {Biomes.FROZEN_OCEAN, Biomes.COLD_OCEAN, Biomes.OCEAN, Biomes.LUKEWARM_OCEAN, Biomes.WARM_OCEAN}
    };
    private final ResourceKey<Biome>[][] MIDDLE_BIOMES = new ResourceKey[][]{
        {Biomes.SNOWY_PLAINS, Biomes.SNOWY_PLAINS, Biomes.SNOWY_PLAINS, Biomes.SNOWY_TAIGA, Biomes.TAIGA},
        {Biomes.PLAINS, Biomes.PLAINS, Biomes.FOREST, Biomes.TAIGA, Biomes.OLD_GROWTH_SPRUCE_TAIGA},
        {Biomes.FLOWER_FOREST, Biomes.PLAINS, Biomes.FOREST, Biomes.BIRCH_FOREST, Biomes.DARK_FOREST},
        {Biomes.SAVANNA, Biomes.SAVANNA, Biomes.FOREST, Biomes.JUNGLE, Biomes.JUNGLE},
        {Biomes.DESERT, Biomes.DESERT, Biomes.DESERT, Biomes.DESERT, Biomes.DESERT}
    };
    private final ResourceKey<Biome>[][] MIDDLE_BIOMES_VARIANT = new ResourceKey[][]{
        {Biomes.ICE_SPIKES, null, Biomes.SNOWY_TAIGA, null, null},
        {null, null, null, null, Biomes.OLD_GROWTH_PINE_TAIGA},
        {Biomes.SUNFLOWER_PLAINS, null, null, Biomes.OLD_GROWTH_BIRCH_FOREST, null},
        {null, null, Biomes.PLAINS, Biomes.SPARSE_JUNGLE, Biomes.BAMBOO_JUNGLE},
        {null, null, null, null, null}
    };
    private final ResourceKey<Biome>[][] PLATEAU_BIOMES = new ResourceKey[][]{
        {Biomes.SNOWY_PLAINS, Biomes.SNOWY_PLAINS, Biomes.SNOWY_PLAINS, Biomes.SNOWY_TAIGA, Biomes.SNOWY_TAIGA},
        {Biomes.MEADOW, Biomes.MEADOW, Biomes.FOREST, Biomes.TAIGA, Biomes.OLD_GROWTH_SPRUCE_TAIGA},
        {Biomes.MEADOW, Biomes.MEADOW, Biomes.MEADOW, Biomes.MEADOW, Biomes.DARK_FOREST},
        {Biomes.SAVANNA_PLATEAU, Biomes.SAVANNA_PLATEAU, Biomes.FOREST, Biomes.FOREST, Biomes.JUNGLE},
        {Biomes.BADLANDS, Biomes.BADLANDS, Biomes.BADLANDS, Biomes.WOODED_BADLANDS, Biomes.WOODED_BADLANDS}
    };
    private final ResourceKey<Biome>[][] PLATEAU_BIOMES_VARIANT = new ResourceKey[][]{
        {Biomes.ICE_SPIKES, null, null, null, null},
        {Biomes.CHERRY_GROVE, null, Biomes.MEADOW, Biomes.MEADOW, Biomes.OLD_GROWTH_PINE_TAIGA},
        {Biomes.CHERRY_GROVE, Biomes.CHERRY_GROVE, Biomes.FOREST, Biomes.BIRCH_FOREST, null},
        {null, null, null, null, null},
        {Biomes.ERODED_BADLANDS, Biomes.ERODED_BADLANDS, null, null, null}
    };
    private final ResourceKey<Biome>[][] SHATTERED_BIOMES = new ResourceKey[][]{
        {Biomes.WINDSWEPT_GRAVELLY_HILLS, Biomes.WINDSWEPT_GRAVELLY_HILLS, Biomes.WINDSWEPT_HILLS, Biomes.WINDSWEPT_FOREST, Biomes.WINDSWEPT_FOREST},
        {Biomes.WINDSWEPT_GRAVELLY_HILLS, Biomes.WINDSWEPT_GRAVELLY_HILLS, Biomes.WINDSWEPT_HILLS, Biomes.WINDSWEPT_FOREST, Biomes.WINDSWEPT_FOREST},
        {Biomes.WINDSWEPT_HILLS, Biomes.WINDSWEPT_HILLS, Biomes.WINDSWEPT_HILLS, Biomes.WINDSWEPT_FOREST, Biomes.WINDSWEPT_FOREST},
        {null, null, null, null, null},
        {null, null, null, null, null}
    };

    public List<Climate.ParameterPoint> spawnTarget() {
        Climate.Parameter var0 = Climate.Parameter.point(0.0F);
        float var1 = 0.16F;
        return List.of(
            new Climate.ParameterPoint(
                this.FULL_RANGE,
                this.FULL_RANGE,
                Climate.Parameter.span(this.inlandContinentalness, this.FULL_RANGE),
                this.FULL_RANGE,
                var0,
                Climate.Parameter.span(-1.0F, -0.16F),
                0L
            ),
            new Climate.ParameterPoint(
                this.FULL_RANGE,
                this.FULL_RANGE,
                Climate.Parameter.span(this.inlandContinentalness, this.FULL_RANGE),
                this.FULL_RANGE,
                var0,
                Climate.Parameter.span(0.16F, 1.0F),
                0L
            )
        );
    }

    protected void addBiomes(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> param0) {
        if (SharedConstants.debugGenerateSquareTerrainWithoutNoise) {
            this.addDebugBiomes(param0);
        } else {
            this.addOffCoastBiomes(param0);
            this.addInlandBiomes(param0);
            this.addUndergroundBiomes(param0);
        }
    }

    private void addDebugBiomes(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> param0) {
        HolderLookup.Provider var0 = VanillaRegistries.createLookup();
        HolderGetter<DensityFunction> var1 = var0.lookupOrThrow(Registries.DENSITY_FUNCTION);
        DensityFunctions.Spline.Coordinate var2 = new DensityFunctions.Spline.Coordinate(var1.getOrThrow(NoiseRouterData.CONTINENTS));
        DensityFunctions.Spline.Coordinate var3 = new DensityFunctions.Spline.Coordinate(var1.getOrThrow(NoiseRouterData.EROSION));
        DensityFunctions.Spline.Coordinate var4 = new DensityFunctions.Spline.Coordinate(var1.getOrThrow(NoiseRouterData.RIDGES_FOLDED));
        param0.accept(
            Pair.of(
                Climate.parameters(this.FULL_RANGE, this.FULL_RANGE, this.FULL_RANGE, this.FULL_RANGE, Climate.Parameter.point(0.0F), this.FULL_RANGE, 0.01F),
                Biomes.PLAINS
            )
        );
        CubicSpline<?, ?> var5 = TerrainProvider.buildErosionOffsetSpline(
            var3, var4, -0.15F, 0.0F, 0.0F, 0.1F, 0.0F, -0.03F, false, false, ToFloatFunction.IDENTITY
        );
        if (var5 instanceof CubicSpline.Multipoint var6) {
            ResourceKey<Biome> var7 = Biomes.DESERT;

            for(float var8 : var6.locations()) {
                param0.accept(
                    Pair.of(
                        Climate.parameters(
                            this.FULL_RANGE,
                            this.FULL_RANGE,
                            this.FULL_RANGE,
                            Climate.Parameter.point(var8),
                            Climate.Parameter.point(0.0F),
                            this.FULL_RANGE,
                            0.0F
                        ),
                        var7
                    )
                );
                var7 = var7 == Biomes.DESERT ? Biomes.BADLANDS : Biomes.DESERT;
            }
        }

        CubicSpline<?, ?> var9 = TerrainProvider.overworldOffset(var2, var3, var4, false);
        if (var9 instanceof CubicSpline.Multipoint var10) {
            for(float var11 : var10.locations()) {
                param0.accept(
                    Pair.of(
                        Climate.parameters(
                            this.FULL_RANGE,
                            this.FULL_RANGE,
                            Climate.Parameter.point(var11),
                            this.FULL_RANGE,
                            Climate.Parameter.point(0.0F),
                            this.FULL_RANGE,
                            0.0F
                        ),
                        Biomes.SNOWY_TAIGA
                    )
                );
            }
        }

    }

    private void addOffCoastBiomes(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> param0) {
        this.addSurfaceBiome(
            param0, this.FULL_RANGE, this.FULL_RANGE, this.mushroomFieldsContinentalness, this.FULL_RANGE, this.FULL_RANGE, 0.0F, Biomes.MUSHROOM_FIELDS
        );

        for(int var0 = 0; var0 < this.temperatures.length; ++var0) {
            Climate.Parameter var1 = this.temperatures[var0];
            this.addSurfaceBiome(param0, var1, this.FULL_RANGE, this.deepOceanContinentalness, this.FULL_RANGE, this.FULL_RANGE, 0.0F, this.OCEANS[0][var0]);
            this.addSurfaceBiome(param0, var1, this.FULL_RANGE, this.oceanContinentalness, this.FULL_RANGE, this.FULL_RANGE, 0.0F, this.OCEANS[1][var0]);
        }

    }

    private void addInlandBiomes(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> param0) {
        this.addMidSlice(param0, Climate.Parameter.span(-1.0F, -0.93333334F));
        this.addHighSlice(param0, Climate.Parameter.span(-0.93333334F, -0.7666667F));
        this.addPeaks(param0, Climate.Parameter.span(-0.7666667F, -0.56666666F));
        this.addHighSlice(param0, Climate.Parameter.span(-0.56666666F, -0.4F));
        this.addMidSlice(param0, Climate.Parameter.span(-0.4F, -0.26666668F));
        this.addLowSlice(param0, Climate.Parameter.span(-0.26666668F, -0.05F));
        this.addValleys(param0, Climate.Parameter.span(-0.05F, 0.05F));
        this.addLowSlice(param0, Climate.Parameter.span(0.05F, 0.26666668F));
        this.addMidSlice(param0, Climate.Parameter.span(0.26666668F, 0.4F));
        this.addHighSlice(param0, Climate.Parameter.span(0.4F, 0.56666666F));
        this.addPeaks(param0, Climate.Parameter.span(0.56666666F, 0.7666667F));
        this.addHighSlice(param0, Climate.Parameter.span(0.7666667F, 0.93333334F));
        this.addMidSlice(param0, Climate.Parameter.span(0.93333334F, 1.0F));
    }

    private void addPeaks(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> param0, Climate.Parameter param1) {
        for(int var0 = 0; var0 < this.temperatures.length; ++var0) {
            Climate.Parameter var1 = this.temperatures[var0];

            for(int var2 = 0; var2 < this.humidities.length; ++var2) {
                Climate.Parameter var3 = this.humidities[var2];
                ResourceKey<Biome> var4 = this.pickMiddleBiome(var0, var2, param1);
                ResourceKey<Biome> var5 = this.pickMiddleBiomeOrBadlandsIfHot(var0, var2, param1);
                ResourceKey<Biome> var6 = this.pickMiddleBiomeOrBadlandsIfHotOrSlopeIfCold(var0, var2, param1);
                ResourceKey<Biome> var7 = this.pickPlateauBiome(var0, var2, param1);
                ResourceKey<Biome> var8 = this.pickShatteredBiome(var0, var2, param1);
                ResourceKey<Biome> var9 = this.maybePickWindsweptSavannaBiome(var0, var2, param1, var8);
                ResourceKey<Biome> var10 = this.pickPeakBiome(var0, var2, param1);
                this.addSurfaceBiome(
                    param0, var1, var3, Climate.Parameter.span(this.coastContinentalness, this.farInlandContinentalness), this.erosions[0], param1, 0.0F, var10
                );
                this.addSurfaceBiome(
                    param0, var1, var3, Climate.Parameter.span(this.coastContinentalness, this.nearInlandContinentalness), this.erosions[1], param1, 0.0F, var6
                );
                this.addSurfaceBiome(
                    param0,
                    var1,
                    var3,
                    Climate.Parameter.span(this.midInlandContinentalness, this.farInlandContinentalness),
                    this.erosions[1],
                    param1,
                    0.0F,
                    var10
                );
                this.addSurfaceBiome(
                    param0,
                    var1,
                    var3,
                    Climate.Parameter.span(this.coastContinentalness, this.nearInlandContinentalness),
                    Climate.Parameter.span(this.erosions[2], this.erosions[3]),
                    param1,
                    0.0F,
                    var4
                );
                this.addSurfaceBiome(
                    param0,
                    var1,
                    var3,
                    Climate.Parameter.span(this.midInlandContinentalness, this.farInlandContinentalness),
                    this.erosions[2],
                    param1,
                    0.0F,
                    var7
                );
                this.addSurfaceBiome(param0, var1, var3, this.midInlandContinentalness, this.erosions[3], param1, 0.0F, var5);
                this.addSurfaceBiome(param0, var1, var3, this.farInlandContinentalness, this.erosions[3], param1, 0.0F, var7);
                this.addSurfaceBiome(
                    param0, var1, var3, Climate.Parameter.span(this.coastContinentalness, this.farInlandContinentalness), this.erosions[4], param1, 0.0F, var4
                );
                this.addSurfaceBiome(
                    param0, var1, var3, Climate.Parameter.span(this.coastContinentalness, this.nearInlandContinentalness), this.erosions[5], param1, 0.0F, var9
                );
                this.addSurfaceBiome(
                    param0,
                    var1,
                    var3,
                    Climate.Parameter.span(this.midInlandContinentalness, this.farInlandContinentalness),
                    this.erosions[5],
                    param1,
                    0.0F,
                    var8
                );
                this.addSurfaceBiome(
                    param0, var1, var3, Climate.Parameter.span(this.coastContinentalness, this.farInlandContinentalness), this.erosions[6], param1, 0.0F, var4
                );
            }
        }

    }

    private void addHighSlice(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> param0, Climate.Parameter param1) {
        for(int var0 = 0; var0 < this.temperatures.length; ++var0) {
            Climate.Parameter var1 = this.temperatures[var0];

            for(int var2 = 0; var2 < this.humidities.length; ++var2) {
                Climate.Parameter var3 = this.humidities[var2];
                ResourceKey<Biome> var4 = this.pickMiddleBiome(var0, var2, param1);
                ResourceKey<Biome> var5 = this.pickMiddleBiomeOrBadlandsIfHot(var0, var2, param1);
                ResourceKey<Biome> var6 = this.pickMiddleBiomeOrBadlandsIfHotOrSlopeIfCold(var0, var2, param1);
                ResourceKey<Biome> var7 = this.pickPlateauBiome(var0, var2, param1);
                ResourceKey<Biome> var8 = this.pickShatteredBiome(var0, var2, param1);
                ResourceKey<Biome> var9 = this.maybePickWindsweptSavannaBiome(var0, var2, param1, var4);
                ResourceKey<Biome> var10 = this.pickSlopeBiome(var0, var2, param1);
                ResourceKey<Biome> var11 = this.pickPeakBiome(var0, var2, param1);
                this.addSurfaceBiome(
                    param0, var1, var3, this.coastContinentalness, Climate.Parameter.span(this.erosions[0], this.erosions[1]), param1, 0.0F, var4
                );
                this.addSurfaceBiome(param0, var1, var3, this.nearInlandContinentalness, this.erosions[0], param1, 0.0F, var10);
                this.addSurfaceBiome(
                    param0,
                    var1,
                    var3,
                    Climate.Parameter.span(this.midInlandContinentalness, this.farInlandContinentalness),
                    this.erosions[0],
                    param1,
                    0.0F,
                    var11
                );
                this.addSurfaceBiome(param0, var1, var3, this.nearInlandContinentalness, this.erosions[1], param1, 0.0F, var6);
                this.addSurfaceBiome(
                    param0,
                    var1,
                    var3,
                    Climate.Parameter.span(this.midInlandContinentalness, this.farInlandContinentalness),
                    this.erosions[1],
                    param1,
                    0.0F,
                    var10
                );
                this.addSurfaceBiome(
                    param0,
                    var1,
                    var3,
                    Climate.Parameter.span(this.coastContinentalness, this.nearInlandContinentalness),
                    Climate.Parameter.span(this.erosions[2], this.erosions[3]),
                    param1,
                    0.0F,
                    var4
                );
                this.addSurfaceBiome(
                    param0,
                    var1,
                    var3,
                    Climate.Parameter.span(this.midInlandContinentalness, this.farInlandContinentalness),
                    this.erosions[2],
                    param1,
                    0.0F,
                    var7
                );
                this.addSurfaceBiome(param0, var1, var3, this.midInlandContinentalness, this.erosions[3], param1, 0.0F, var5);
                this.addSurfaceBiome(param0, var1, var3, this.farInlandContinentalness, this.erosions[3], param1, 0.0F, var7);
                this.addSurfaceBiome(
                    param0, var1, var3, Climate.Parameter.span(this.coastContinentalness, this.farInlandContinentalness), this.erosions[4], param1, 0.0F, var4
                );
                this.addSurfaceBiome(
                    param0, var1, var3, Climate.Parameter.span(this.coastContinentalness, this.nearInlandContinentalness), this.erosions[5], param1, 0.0F, var9
                );
                this.addSurfaceBiome(
                    param0,
                    var1,
                    var3,
                    Climate.Parameter.span(this.midInlandContinentalness, this.farInlandContinentalness),
                    this.erosions[5],
                    param1,
                    0.0F,
                    var8
                );
                this.addSurfaceBiome(
                    param0, var1, var3, Climate.Parameter.span(this.coastContinentalness, this.farInlandContinentalness), this.erosions[6], param1, 0.0F, var4
                );
            }
        }

    }

    private void addMidSlice(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> param0, Climate.Parameter param1) {
        this.addSurfaceBiome(
            param0,
            this.FULL_RANGE,
            this.FULL_RANGE,
            this.coastContinentalness,
            Climate.Parameter.span(this.erosions[0], this.erosions[2]),
            param1,
            0.0F,
            Biomes.STONY_SHORE
        );
        this.addSurfaceBiome(
            param0,
            Climate.Parameter.span(this.temperatures[1], this.temperatures[2]),
            this.FULL_RANGE,
            Climate.Parameter.span(this.nearInlandContinentalness, this.farInlandContinentalness),
            this.erosions[6],
            param1,
            0.0F,
            Biomes.SWAMP
        );
        this.addSurfaceBiome(
            param0,
            Climate.Parameter.span(this.temperatures[3], this.temperatures[4]),
            this.FULL_RANGE,
            Climate.Parameter.span(this.nearInlandContinentalness, this.farInlandContinentalness),
            this.erosions[6],
            param1,
            0.0F,
            Biomes.MANGROVE_SWAMP
        );

        for(int var0 = 0; var0 < this.temperatures.length; ++var0) {
            Climate.Parameter var1 = this.temperatures[var0];

            for(int var2 = 0; var2 < this.humidities.length; ++var2) {
                Climate.Parameter var3 = this.humidities[var2];
                ResourceKey<Biome> var4 = this.pickMiddleBiome(var0, var2, param1);
                ResourceKey<Biome> var5 = this.pickMiddleBiomeOrBadlandsIfHot(var0, var2, param1);
                ResourceKey<Biome> var6 = this.pickMiddleBiomeOrBadlandsIfHotOrSlopeIfCold(var0, var2, param1);
                ResourceKey<Biome> var7 = this.pickShatteredBiome(var0, var2, param1);
                ResourceKey<Biome> var8 = this.pickPlateauBiome(var0, var2, param1);
                ResourceKey<Biome> var9 = this.pickBeachBiome(var0, var2);
                ResourceKey<Biome> var10 = this.maybePickWindsweptSavannaBiome(var0, var2, param1, var4);
                ResourceKey<Biome> var11 = this.pickShatteredCoastBiome(var0, var2, param1);
                ResourceKey<Biome> var12 = this.pickSlopeBiome(var0, var2, param1);
                this.addSurfaceBiome(
                    param0,
                    var1,
                    var3,
                    Climate.Parameter.span(this.nearInlandContinentalness, this.farInlandContinentalness),
                    this.erosions[0],
                    param1,
                    0.0F,
                    var12
                );
                this.addSurfaceBiome(
                    param0,
                    var1,
                    var3,
                    Climate.Parameter.span(this.nearInlandContinentalness, this.midInlandContinentalness),
                    this.erosions[1],
                    param1,
                    0.0F,
                    var6
                );
                this.addSurfaceBiome(param0, var1, var3, this.farInlandContinentalness, this.erosions[1], param1, 0.0F, var0 == 0 ? var12 : var8);
                this.addSurfaceBiome(param0, var1, var3, this.nearInlandContinentalness, this.erosions[2], param1, 0.0F, var4);
                this.addSurfaceBiome(param0, var1, var3, this.midInlandContinentalness, this.erosions[2], param1, 0.0F, var5);
                this.addSurfaceBiome(param0, var1, var3, this.farInlandContinentalness, this.erosions[2], param1, 0.0F, var8);
                this.addSurfaceBiome(
                    param0, var1, var3, Climate.Parameter.span(this.coastContinentalness, this.nearInlandContinentalness), this.erosions[3], param1, 0.0F, var4
                );
                this.addSurfaceBiome(
                    param0,
                    var1,
                    var3,
                    Climate.Parameter.span(this.midInlandContinentalness, this.farInlandContinentalness),
                    this.erosions[3],
                    param1,
                    0.0F,
                    var5
                );
                if (param1.max() < 0L) {
                    this.addSurfaceBiome(param0, var1, var3, this.coastContinentalness, this.erosions[4], param1, 0.0F, var9);
                    this.addSurfaceBiome(
                        param0,
                        var1,
                        var3,
                        Climate.Parameter.span(this.nearInlandContinentalness, this.farInlandContinentalness),
                        this.erosions[4],
                        param1,
                        0.0F,
                        var4
                    );
                } else {
                    this.addSurfaceBiome(
                        param0,
                        var1,
                        var3,
                        Climate.Parameter.span(this.coastContinentalness, this.farInlandContinentalness),
                        this.erosions[4],
                        param1,
                        0.0F,
                        var4
                    );
                }

                this.addSurfaceBiome(param0, var1, var3, this.coastContinentalness, this.erosions[5], param1, 0.0F, var11);
                this.addSurfaceBiome(param0, var1, var3, this.nearInlandContinentalness, this.erosions[5], param1, 0.0F, var10);
                this.addSurfaceBiome(
                    param0,
                    var1,
                    var3,
                    Climate.Parameter.span(this.midInlandContinentalness, this.farInlandContinentalness),
                    this.erosions[5],
                    param1,
                    0.0F,
                    var7
                );
                if (param1.max() < 0L) {
                    this.addSurfaceBiome(param0, var1, var3, this.coastContinentalness, this.erosions[6], param1, 0.0F, var9);
                } else {
                    this.addSurfaceBiome(param0, var1, var3, this.coastContinentalness, this.erosions[6], param1, 0.0F, var4);
                }

                if (var0 == 0) {
                    this.addSurfaceBiome(
                        param0,
                        var1,
                        var3,
                        Climate.Parameter.span(this.nearInlandContinentalness, this.farInlandContinentalness),
                        this.erosions[6],
                        param1,
                        0.0F,
                        var4
                    );
                }
            }
        }

    }

    private void addLowSlice(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> param0, Climate.Parameter param1) {
        this.addSurfaceBiome(
            param0,
            this.FULL_RANGE,
            this.FULL_RANGE,
            this.coastContinentalness,
            Climate.Parameter.span(this.erosions[0], this.erosions[2]),
            param1,
            0.0F,
            Biomes.STONY_SHORE
        );
        this.addSurfaceBiome(
            param0,
            Climate.Parameter.span(this.temperatures[1], this.temperatures[2]),
            this.FULL_RANGE,
            Climate.Parameter.span(this.nearInlandContinentalness, this.farInlandContinentalness),
            this.erosions[6],
            param1,
            0.0F,
            Biomes.SWAMP
        );
        this.addSurfaceBiome(
            param0,
            Climate.Parameter.span(this.temperatures[3], this.temperatures[4]),
            this.FULL_RANGE,
            Climate.Parameter.span(this.nearInlandContinentalness, this.farInlandContinentalness),
            this.erosions[6],
            param1,
            0.0F,
            Biomes.MANGROVE_SWAMP
        );

        for(int var0 = 0; var0 < this.temperatures.length; ++var0) {
            Climate.Parameter var1 = this.temperatures[var0];

            for(int var2 = 0; var2 < this.humidities.length; ++var2) {
                Climate.Parameter var3 = this.humidities[var2];
                ResourceKey<Biome> var4 = this.pickMiddleBiome(var0, var2, param1);
                ResourceKey<Biome> var5 = this.pickMiddleBiomeOrBadlandsIfHot(var0, var2, param1);
                ResourceKey<Biome> var6 = this.pickMiddleBiomeOrBadlandsIfHotOrSlopeIfCold(var0, var2, param1);
                ResourceKey<Biome> var7 = this.pickBeachBiome(var0, var2);
                ResourceKey<Biome> var8 = this.maybePickWindsweptSavannaBiome(var0, var2, param1, var4);
                ResourceKey<Biome> var9 = this.pickShatteredCoastBiome(var0, var2, param1);
                this.addSurfaceBiome(
                    param0, var1, var3, this.nearInlandContinentalness, Climate.Parameter.span(this.erosions[0], this.erosions[1]), param1, 0.0F, var5
                );
                this.addSurfaceBiome(
                    param0,
                    var1,
                    var3,
                    Climate.Parameter.span(this.midInlandContinentalness, this.farInlandContinentalness),
                    Climate.Parameter.span(this.erosions[0], this.erosions[1]),
                    param1,
                    0.0F,
                    var6
                );
                this.addSurfaceBiome(
                    param0, var1, var3, this.nearInlandContinentalness, Climate.Parameter.span(this.erosions[2], this.erosions[3]), param1, 0.0F, var4
                );
                this.addSurfaceBiome(
                    param0,
                    var1,
                    var3,
                    Climate.Parameter.span(this.midInlandContinentalness, this.farInlandContinentalness),
                    Climate.Parameter.span(this.erosions[2], this.erosions[3]),
                    param1,
                    0.0F,
                    var5
                );
                this.addSurfaceBiome(
                    param0, var1, var3, this.coastContinentalness, Climate.Parameter.span(this.erosions[3], this.erosions[4]), param1, 0.0F, var7
                );
                this.addSurfaceBiome(
                    param0,
                    var1,
                    var3,
                    Climate.Parameter.span(this.nearInlandContinentalness, this.farInlandContinentalness),
                    this.erosions[4],
                    param1,
                    0.0F,
                    var4
                );
                this.addSurfaceBiome(param0, var1, var3, this.coastContinentalness, this.erosions[5], param1, 0.0F, var9);
                this.addSurfaceBiome(param0, var1, var3, this.nearInlandContinentalness, this.erosions[5], param1, 0.0F, var8);
                this.addSurfaceBiome(
                    param0,
                    var1,
                    var3,
                    Climate.Parameter.span(this.midInlandContinentalness, this.farInlandContinentalness),
                    this.erosions[5],
                    param1,
                    0.0F,
                    var4
                );
                this.addSurfaceBiome(param0, var1, var3, this.coastContinentalness, this.erosions[6], param1, 0.0F, var7);
                if (var0 == 0) {
                    this.addSurfaceBiome(
                        param0,
                        var1,
                        var3,
                        Climate.Parameter.span(this.nearInlandContinentalness, this.farInlandContinentalness),
                        this.erosions[6],
                        param1,
                        0.0F,
                        var4
                    );
                }
            }
        }

    }

    private void addValleys(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> param0, Climate.Parameter param1) {
        this.addSurfaceBiome(
            param0,
            this.FROZEN_RANGE,
            this.FULL_RANGE,
            this.coastContinentalness,
            Climate.Parameter.span(this.erosions[0], this.erosions[1]),
            param1,
            0.0F,
            param1.max() < 0L ? Biomes.STONY_SHORE : Biomes.FROZEN_RIVER
        );
        this.addSurfaceBiome(
            param0,
            this.UNFROZEN_RANGE,
            this.FULL_RANGE,
            this.coastContinentalness,
            Climate.Parameter.span(this.erosions[0], this.erosions[1]),
            param1,
            0.0F,
            param1.max() < 0L ? Biomes.STONY_SHORE : Biomes.RIVER
        );
        this.addSurfaceBiome(
            param0,
            this.FROZEN_RANGE,
            this.FULL_RANGE,
            this.nearInlandContinentalness,
            Climate.Parameter.span(this.erosions[0], this.erosions[1]),
            param1,
            0.0F,
            Biomes.FROZEN_RIVER
        );
        this.addSurfaceBiome(
            param0,
            this.UNFROZEN_RANGE,
            this.FULL_RANGE,
            this.nearInlandContinentalness,
            Climate.Parameter.span(this.erosions[0], this.erosions[1]),
            param1,
            0.0F,
            Biomes.RIVER
        );
        this.addSurfaceBiome(
            param0,
            this.FROZEN_RANGE,
            this.FULL_RANGE,
            Climate.Parameter.span(this.coastContinentalness, this.farInlandContinentalness),
            Climate.Parameter.span(this.erosions[2], this.erosions[5]),
            param1,
            0.0F,
            Biomes.FROZEN_RIVER
        );
        this.addSurfaceBiome(
            param0,
            this.UNFROZEN_RANGE,
            this.FULL_RANGE,
            Climate.Parameter.span(this.coastContinentalness, this.farInlandContinentalness),
            Climate.Parameter.span(this.erosions[2], this.erosions[5]),
            param1,
            0.0F,
            Biomes.RIVER
        );
        this.addSurfaceBiome(param0, this.FROZEN_RANGE, this.FULL_RANGE, this.coastContinentalness, this.erosions[6], param1, 0.0F, Biomes.FROZEN_RIVER);
        this.addSurfaceBiome(param0, this.UNFROZEN_RANGE, this.FULL_RANGE, this.coastContinentalness, this.erosions[6], param1, 0.0F, Biomes.RIVER);
        this.addSurfaceBiome(
            param0,
            Climate.Parameter.span(this.temperatures[1], this.temperatures[2]),
            this.FULL_RANGE,
            Climate.Parameter.span(this.inlandContinentalness, this.farInlandContinentalness),
            this.erosions[6],
            param1,
            0.0F,
            Biomes.SWAMP
        );
        this.addSurfaceBiome(
            param0,
            Climate.Parameter.span(this.temperatures[3], this.temperatures[4]),
            this.FULL_RANGE,
            Climate.Parameter.span(this.inlandContinentalness, this.farInlandContinentalness),
            this.erosions[6],
            param1,
            0.0F,
            Biomes.MANGROVE_SWAMP
        );
        this.addSurfaceBiome(
            param0,
            this.FROZEN_RANGE,
            this.FULL_RANGE,
            Climate.Parameter.span(this.inlandContinentalness, this.farInlandContinentalness),
            this.erosions[6],
            param1,
            0.0F,
            Biomes.FROZEN_RIVER
        );

        for(int var0 = 0; var0 < this.temperatures.length; ++var0) {
            Climate.Parameter var1 = this.temperatures[var0];

            for(int var2 = 0; var2 < this.humidities.length; ++var2) {
                Climate.Parameter var3 = this.humidities[var2];
                ResourceKey<Biome> var4 = this.pickMiddleBiomeOrBadlandsIfHot(var0, var2, param1);
                this.addSurfaceBiome(
                    param0,
                    var1,
                    var3,
                    Climate.Parameter.span(this.midInlandContinentalness, this.farInlandContinentalness),
                    Climate.Parameter.span(this.erosions[0], this.erosions[1]),
                    param1,
                    0.0F,
                    var4
                );
            }
        }

    }

    private void addUndergroundBiomes(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> param0) {
        this.addUndergroundBiome(
            param0, this.FULL_RANGE, this.FULL_RANGE, Climate.Parameter.span(0.8F, 1.0F), this.FULL_RANGE, this.FULL_RANGE, 0.0F, Biomes.DRIPSTONE_CAVES
        );
        this.addUndergroundBiome(
            param0, this.FULL_RANGE, Climate.Parameter.span(0.7F, 1.0F), this.FULL_RANGE, this.FULL_RANGE, this.FULL_RANGE, 0.0F, Biomes.LUSH_CAVES
        );
        this.addBottomBiome(
            param0,
            this.FULL_RANGE,
            this.FULL_RANGE,
            this.FULL_RANGE,
            Climate.Parameter.span(this.erosions[0], this.erosions[1]),
            this.FULL_RANGE,
            0.0F,
            Biomes.DEEP_DARK
        );
    }

    private ResourceKey<Biome> pickMiddleBiome(int param0, int param1, Climate.Parameter param2) {
        if (param2.max() < 0L) {
            return this.MIDDLE_BIOMES[param0][param1];
        } else {
            ResourceKey<Biome> var0 = this.MIDDLE_BIOMES_VARIANT[param0][param1];
            return var0 == null ? this.MIDDLE_BIOMES[param0][param1] : var0;
        }
    }

    private ResourceKey<Biome> pickMiddleBiomeOrBadlandsIfHot(int param0, int param1, Climate.Parameter param2) {
        return param0 == 4 ? this.pickBadlandsBiome(param1, param2) : this.pickMiddleBiome(param0, param1, param2);
    }

    private ResourceKey<Biome> pickMiddleBiomeOrBadlandsIfHotOrSlopeIfCold(int param0, int param1, Climate.Parameter param2) {
        return param0 == 0 ? this.pickSlopeBiome(param0, param1, param2) : this.pickMiddleBiomeOrBadlandsIfHot(param0, param1, param2);
    }

    private ResourceKey<Biome> maybePickWindsweptSavannaBiome(int param0, int param1, Climate.Parameter param2, ResourceKey<Biome> param3) {
        return param0 > 1 && param1 < 4 && param2.max() >= 0L ? Biomes.WINDSWEPT_SAVANNA : param3;
    }

    private ResourceKey<Biome> pickShatteredCoastBiome(int param0, int param1, Climate.Parameter param2) {
        ResourceKey<Biome> var0 = param2.max() >= 0L ? this.pickMiddleBiome(param0, param1, param2) : this.pickBeachBiome(param0, param1);
        return this.maybePickWindsweptSavannaBiome(param0, param1, param2, var0);
    }

    private ResourceKey<Biome> pickBeachBiome(int param0, int param1) {
        if (param0 == 0) {
            return Biomes.SNOWY_BEACH;
        } else {
            return param0 == 4 ? Biomes.DESERT : Biomes.BEACH;
        }
    }

    private ResourceKey<Biome> pickBadlandsBiome(int param0, Climate.Parameter param1) {
        if (param0 < 2) {
            return param1.max() < 0L ? Biomes.BADLANDS : Biomes.ERODED_BADLANDS;
        } else {
            return param0 < 3 ? Biomes.BADLANDS : Biomes.WOODED_BADLANDS;
        }
    }

    private ResourceKey<Biome> pickPlateauBiome(int param0, int param1, Climate.Parameter param2) {
        if (param2.max() >= 0L) {
            ResourceKey<Biome> var0 = this.PLATEAU_BIOMES_VARIANT[param0][param1];
            if (var0 != null) {
                return var0;
            }
        }

        return this.PLATEAU_BIOMES[param0][param1];
    }

    private ResourceKey<Biome> pickPeakBiome(int param0, int param1, Climate.Parameter param2) {
        if (param0 <= 2) {
            return param2.max() < 0L ? Biomes.JAGGED_PEAKS : Biomes.FROZEN_PEAKS;
        } else {
            return param0 == 3 ? Biomes.STONY_PEAKS : this.pickBadlandsBiome(param1, param2);
        }
    }

    private ResourceKey<Biome> pickSlopeBiome(int param0, int param1, Climate.Parameter param2) {
        if (param0 >= 3) {
            return this.pickPlateauBiome(param0, param1, param2);
        } else {
            return param1 <= 1 ? Biomes.SNOWY_SLOPES : Biomes.GROVE;
        }
    }

    private ResourceKey<Biome> pickShatteredBiome(int param0, int param1, Climate.Parameter param2) {
        ResourceKey<Biome> var0 = this.SHATTERED_BIOMES[param0][param1];
        return var0 == null ? this.pickMiddleBiome(param0, param1, param2) : var0;
    }

    private void addSurfaceBiome(
        Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> param0,
        Climate.Parameter param1,
        Climate.Parameter param2,
        Climate.Parameter param3,
        Climate.Parameter param4,
        Climate.Parameter param5,
        float param6,
        ResourceKey<Biome> param7
    ) {
        param0.accept(Pair.of(Climate.parameters(param1, param2, param3, param4, Climate.Parameter.point(0.0F), param5, param6), param7));
        param0.accept(Pair.of(Climate.parameters(param1, param2, param3, param4, Climate.Parameter.point(1.0F), param5, param6), param7));
    }

    private void addUndergroundBiome(
        Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> param0,
        Climate.Parameter param1,
        Climate.Parameter param2,
        Climate.Parameter param3,
        Climate.Parameter param4,
        Climate.Parameter param5,
        float param6,
        ResourceKey<Biome> param7
    ) {
        param0.accept(Pair.of(Climate.parameters(param1, param2, param3, param4, Climate.Parameter.span(0.2F, 0.9F), param5, param6), param7));
    }

    private void addBottomBiome(
        Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> param0,
        Climate.Parameter param1,
        Climate.Parameter param2,
        Climate.Parameter param3,
        Climate.Parameter param4,
        Climate.Parameter param5,
        float param6,
        ResourceKey<Biome> param7
    ) {
        param0.accept(Pair.of(Climate.parameters(param1, param2, param3, param4, Climate.Parameter.point(1.1F), param5, param6), param7));
    }

    public static boolean isDeepDarkRegion(DensityFunction param0, DensityFunction param1, DensityFunction.FunctionContext param2) {
        return param0.compute(param2) < -0.225F && param1.compute(param2) > 0.9F;
    }

    public static String getDebugStringForPeaksAndValleys(double param0) {
        if (param0 < (double)NoiseRouterData.peaksAndValleys(0.05F)) {
            return "Valley";
        } else if (param0 < (double)NoiseRouterData.peaksAndValleys(0.26666668F)) {
            return "Low";
        } else if (param0 < (double)NoiseRouterData.peaksAndValleys(0.4F)) {
            return "Mid";
        } else {
            return param0 < (double)NoiseRouterData.peaksAndValleys(0.56666666F) ? "High" : "Peak";
        }
    }

    public String getDebugStringForContinentalness(double param0) {
        double var0 = (double)Climate.quantizeCoord((float)param0);
        if (var0 < (double)this.mushroomFieldsContinentalness.max()) {
            return "Mushroom fields";
        } else if (var0 < (double)this.deepOceanContinentalness.max()) {
            return "Deep ocean";
        } else if (var0 < (double)this.oceanContinentalness.max()) {
            return "Ocean";
        } else if (var0 < (double)this.coastContinentalness.max()) {
            return "Coast";
        } else if (var0 < (double)this.nearInlandContinentalness.max()) {
            return "Near inland";
        } else {
            return var0 < (double)this.midInlandContinentalness.max() ? "Mid inland" : "Far inland";
        }
    }

    public String getDebugStringForErosion(double param0) {
        return getDebugStringForNoiseValue(param0, this.erosions);
    }

    public String getDebugStringForTemperature(double param0) {
        return getDebugStringForNoiseValue(param0, this.temperatures);
    }

    public String getDebugStringForHumidity(double param0) {
        return getDebugStringForNoiseValue(param0, this.humidities);
    }

    private static String getDebugStringForNoiseValue(double param0, Climate.Parameter[] param1) {
        double var0 = (double)Climate.quantizeCoord((float)param0);

        for(int var1 = 0; var1 < param1.length; ++var1) {
            if (var0 < (double)param1[var1].max()) {
                return var1 + "";
            }
        }

        return "?";
    }

    @VisibleForDebug
    public Climate.Parameter[] getTemperatureThresholds() {
        return this.temperatures;
    }

    @VisibleForDebug
    public Climate.Parameter[] getHumidityThresholds() {
        return this.humidities;
    }

    @VisibleForDebug
    public Climate.Parameter[] getErosionThresholds() {
        return this.erosions;
    }

    @VisibleForDebug
    public Climate.Parameter[] getContinentalnessThresholds() {
        return new Climate.Parameter[]{
            this.mushroomFieldsContinentalness,
            this.deepOceanContinentalness,
            this.oceanContinentalness,
            this.coastContinentalness,
            this.nearInlandContinentalness,
            this.midInlandContinentalness,
            this.farInlandContinentalness
        };
    }

    @VisibleForDebug
    public Climate.Parameter[] getPeaksAndValleysThresholds() {
        return new Climate.Parameter[]{
            Climate.Parameter.span(-2.0F, NoiseRouterData.peaksAndValleys(0.05F)),
            Climate.Parameter.span(NoiseRouterData.peaksAndValleys(0.05F), NoiseRouterData.peaksAndValleys(0.26666668F)),
            Climate.Parameter.span(NoiseRouterData.peaksAndValleys(0.26666668F), NoiseRouterData.peaksAndValleys(0.4F)),
            Climate.Parameter.span(NoiseRouterData.peaksAndValleys(0.4F), NoiseRouterData.peaksAndValleys(0.56666666F)),
            Climate.Parameter.span(NoiseRouterData.peaksAndValleys(0.56666666F), 2.0F)
        };
    }

    @VisibleForDebug
    public Climate.Parameter[] getWeirdnessThresholds() {
        return new Climate.Parameter[]{Climate.Parameter.span(-2.0F, 0.0F), Climate.Parameter.span(0.0F, 2.0F)};
    }
}
