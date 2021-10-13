package net.minecraft.world.level.levelgen;

import com.google.common.annotations.VisibleForTesting;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.core.QuartPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.TerrainShaper;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.synth.BlendedNoise;
import net.minecraft.world.level.levelgen.synth.NoiseUtils;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;

public class NoiseSampler implements Climate.Sampler {
    private static final float ORE_VEIN_RARITY = 1.0F;
    private static final float ORE_THICKNESS = 0.08F;
    private static final float VEININESS_THRESHOLD = 0.4F;
    private static final double VEININESS_FREQUENCY = 1.5;
    private static final int EDGE_ROUNDOFF_BEGIN = 20;
    private static final double MAX_EDGE_ROUNDOFF = 0.2;
    private static final float VEIN_SOLIDNESS = 0.7F;
    private static final float MIN_RICHNESS = 0.1F;
    private static final float MAX_RICHNESS = 0.3F;
    private static final float MAX_RICHNESS_THRESHOLD = 0.6F;
    private static final float CHANCE_OF_RAW_ORE_BLOCK = 0.02F;
    private static final float SKIP_ORE_IF_GAP_NOISE_IS_BELOW = -0.3F;
    private static final double NOODLE_SPACING_AND_STRAIGHTNESS = 1.5;
    private static final NormalNoise.NoiseParameters NOISE_BARRIER = new NormalNoise.NoiseParameters(-3, 1.0);
    private static final NormalNoise.NoiseParameters NOISE_FLUID_LEVEL_FLOODEDNESS = new NormalNoise.NoiseParameters(-7, 1.0);
    private static final NormalNoise.NoiseParameters NOISE_LAVA = new NormalNoise.NoiseParameters(-1, 1.0);
    private static final NormalNoise.NoiseParameters NOISE_FLUID_LEVEL_SPREAD = new NormalNoise.NoiseParameters(-4, 1.0);
    private static final NormalNoise.NoiseParameters NOISE_SOURCE_PILLAR = new NormalNoise.NoiseParameters(-7, 1.0, 1.0);
    private static final NormalNoise.NoiseParameters NOISE_MODULATOR_PILLAR_RARENESS = new NormalNoise.NoiseParameters(-8, 1.0);
    private static final NormalNoise.NoiseParameters NOISE_MODULATOR_PILLAR_THICKNESS = new NormalNoise.NoiseParameters(-8, 1.0);
    private static final NormalNoise.NoiseParameters NOISE_SOURCE_SPAGHETTI_2D = new NormalNoise.NoiseParameters(-7, 1.0);
    private static final NormalNoise.NoiseParameters NOISE_MODULATOR_SPAGHETTI_2D_ELEVATION = new NormalNoise.NoiseParameters(-8, 1.0);
    private static final NormalNoise.NoiseParameters NOISE_MODULATOR_SPAGHETTI_2D_RARITY = new NormalNoise.NoiseParameters(-11, 1.0);
    private static final NormalNoise.NoiseParameters NOISE_MODULATOR_SPAGHETTI_2D_THICKNESS = new NormalNoise.NoiseParameters(-11, 1.0);
    private static final NormalNoise.NoiseParameters NOISE_SOURCE_SPAGHETTI_3D_1 = new NormalNoise.NoiseParameters(-7, 1.0);
    private static final NormalNoise.NoiseParameters NOISE_SOURCE_SPAGHETTI_3D_2 = new NormalNoise.NoiseParameters(-7, 1.0);
    private static final NormalNoise.NoiseParameters NOISE_MODULATOR_SPAGHETTI_3D_RARITY = new NormalNoise.NoiseParameters(-11, 1.0);
    private static final NormalNoise.NoiseParameters NOISE_MODULATOR_SPAGHETTI_3D_THICKNESS = new NormalNoise.NoiseParameters(-8, 1.0);
    private static final NormalNoise.NoiseParameters NOISE_SPAGHETTI_ROUGHNESS = new NormalNoise.NoiseParameters(-5, 1.0);
    private static final NormalNoise.NoiseParameters NOISE_MODULATOR_SPAGHETTI_ROUGHNESS = new NormalNoise.NoiseParameters(-8, 1.0);
    private static final NormalNoise.NoiseParameters NOISE_SOURCE_BIG_ENTRANCE = new NormalNoise.NoiseParameters(-7, 0.4, 0.5, 1.0);
    private static final NormalNoise.NoiseParameters NOISE_SOURCE_LAYER = new NormalNoise.NoiseParameters(-8, 1.0);
    private static final NormalNoise.NoiseParameters NOISE_SOURCE_CHEESE = new NormalNoise.NoiseParameters(-8, 0.5, 1.0, 2.0, 1.0, 2.0, 1.0, 0.0, 2.0, 0.0);
    private static final NormalNoise.NoiseParameters NOISE_JAGGED = new NormalNoise.NoiseParameters(
        -16, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0
    );
    private static final NormalNoise.NoiseParameters NOISE_VEININESS_BASE = new NormalNoise.NoiseParameters(-8, 1.0);
    private static final NormalNoise.NoiseParameters NOISE_VEIN_A_BASE = new NormalNoise.NoiseParameters(-7, 1.0);
    private static final NormalNoise.NoiseParameters NOISE_VEIN_B_BASE = new NormalNoise.NoiseParameters(-7, 1.0);
    private static final NormalNoise.NoiseParameters NOISE_GAP = new NormalNoise.NoiseParameters(-5, 1.0);
    private static final NormalNoise.NoiseParameters NOISE_NOODLE_TOGGLE_BASE = new NormalNoise.NoiseParameters(-8, 1.0);
    private static final NormalNoise.NoiseParameters NOISE_NOODLE_THICKNESS_BASE = new NormalNoise.NoiseParameters(-8, 1.0);
    private static final NormalNoise.NoiseParameters NOISE_NOODLE_RIDGE_A_BASE = new NormalNoise.NoiseParameters(-7, 1.0);
    private static final NormalNoise.NoiseParameters NOISE_NOODLE_RIDGE_B_BASE = new NormalNoise.NoiseParameters(-7, 1.0);
    private final int cellHeight;
    private final int cellCountY;
    private final NoiseSettings noiseSettings;
    private final double dimensionDensityFactor;
    private final double dimensionDensityOffset;
    private final int minCellY;
    private final TerrainShaper shaper = new TerrainShaper();
    private final boolean isNoiseCavesEnabled;
    private final NoiseChunk.InterpolatableNoise baseNoise;
    private final BlendedNoise blendedNoise;
    @Nullable
    private final SimplexNoise islandNoise;
    private final NormalNoise jaggedNoise;
    private final NormalNoise barrierNoise;
    private final NormalNoise fluidLevelFloodednessNoise;
    private final NormalNoise fluidLevelSpreadNoise;
    private final NormalNoise lavaNoise;
    private final NormalNoise layerNoiseSource;
    private final NormalNoise pillarNoiseSource;
    private final NormalNoise pillarRarenessModulator;
    private final NormalNoise pillarThicknessModulator;
    private final NormalNoise spaghetti2DNoiseSource;
    private final NormalNoise spaghetti2DElevationModulator;
    private final NormalNoise spaghetti2DRarityModulator;
    private final NormalNoise spaghetti2DThicknessModulator;
    private final NormalNoise spaghetti3DNoiseSource1;
    private final NormalNoise spaghetti3DNoiseSource2;
    private final NormalNoise spaghetti3DRarityModulator;
    private final NormalNoise spaghetti3DThicknessModulator;
    private final NormalNoise spaghettiRoughnessNoise;
    private final NormalNoise spaghettiRoughnessModulator;
    private final NormalNoise bigEntranceNoiseSource;
    private final NormalNoise cheeseNoiseSource;
    private final NormalNoise temperatureNoise;
    private final NormalNoise humidityNoise;
    private final NormalNoise continentalnessNoise;
    private final NormalNoise erosionNoise;
    private final NormalNoise weirdnessNoise;
    private final NormalNoise offsetNoise;
    private final NormalNoise gapNoise;
    private final NormalNoise veininessBaseNoise;
    private final NoiseChunk.InterpolatableNoise veininess;
    private final NormalNoise veinABaseNoise;
    private final NoiseChunk.InterpolatableNoise veinA;
    private final NormalNoise veinBBaseNoise;
    private final NoiseChunk.InterpolatableNoise veinB;
    private final NormalNoise noodleToggleBaseNoise;
    private final NoiseChunk.InterpolatableNoise noodleToggle;
    private final NormalNoise noodleThicknessBaseNoise;
    private final NoiseChunk.InterpolatableNoise noodleThickness;
    private final NormalNoise noodleRidgeABaseNoise;
    private final NoiseChunk.InterpolatableNoise noodleRidgeA;
    private final NormalNoise noodleRidgeBBaseNoise;
    private final NoiseChunk.InterpolatableNoise noodleRidgeB;
    private final PositionalRandomFactory aquiferPositionalRandomFactory;
    private final PositionalRandomFactory oreVeinsPositionalRandomFactory;
    private final PositionalRandomFactory depthBasedLayerPositionalRandomFactory;

    public NoiseSampler(
        int param0, int param1, int param2, NoiseSettings param3, NoiseOctaves param4, boolean param5, long param6, WorldgenRandom.Algorithm param7
    ) {
        this.cellHeight = param1;
        this.cellCountY = param2;
        this.noiseSettings = param3;
        this.dimensionDensityFactor = param3.densityFactor();
        this.dimensionDensityOffset = param3.densityOffset();
        int var0 = param3.minY();
        this.minCellY = Mth.intFloorDiv(var0, param1);
        this.isNoiseCavesEnabled = param5;
        this.baseNoise = param0x -> param0x.createNoiseInterpolator(
                (param1x, param2x, param3x) -> this.calculateBaseNoise(
                        param1x, param2x, param3x, param0x.terrainInfo(QuartPos.fromBlock(param1x), QuartPos.fromBlock(param3x))
                    )
            );
        if (param3.islandNoiseOverride()) {
            RandomSource var1 = param7.newInstance(param6);
            var1.consumeCount(17292);
            this.islandNoise = new SimplexNoise(var1);
        } else {
            this.islandNoise = null;
        }

        int var2 = Stream.of(NoiseSampler.VeinType.values()).mapToInt(param0x -> param0x.minY).min().orElse(var0);
        int var3 = Stream.of(NoiseSampler.VeinType.values()).mapToInt(param0x -> param0x.maxY).max().orElse(var0);
        float var4 = 4.0F;
        double var5 = 2.6666666666666665;
        int var6 = var0 + 4;
        int var7 = var0 + param3.height();
        if (param7 != WorldgenRandom.Algorithm.LEGACY) {
            PositionalRandomFactory var8 = param7.newInstance(param6).forkPositional();
            this.blendedNoise = new BlendedNoise(var8.fromHashOf("terrain"), param3.noiseSamplingSettings(), param0, param1);
            this.barrierNoise = NormalNoise.create(var8.fromHashOf("aquifer_barrier"), NOISE_BARRIER);
            this.fluidLevelFloodednessNoise = NormalNoise.create(var8.fromHashOf("aquifer_fluid_level_floodedness"), NOISE_FLUID_LEVEL_FLOODEDNESS);
            this.lavaNoise = NormalNoise.create(var8.fromHashOf("aquifer_lava"), NOISE_LAVA);
            this.aquiferPositionalRandomFactory = var8.fromHashOf("aquifer").forkPositional();
            this.fluidLevelSpreadNoise = NormalNoise.create(var8.fromHashOf("aquifer_fluid_level_spread"), NOISE_FLUID_LEVEL_SPREAD);
            this.pillarNoiseSource = NormalNoise.create(var8.fromHashOf("pillar"), NOISE_SOURCE_PILLAR);
            this.pillarRarenessModulator = NormalNoise.create(var8.fromHashOf("pillar_rareness"), NOISE_MODULATOR_PILLAR_RARENESS);
            this.pillarThicknessModulator = NormalNoise.create(var8.fromHashOf("pillar_thickness"), NOISE_MODULATOR_PILLAR_THICKNESS);
            this.spaghetti2DNoiseSource = NormalNoise.create(var8.fromHashOf("spaghetti_2d"), NOISE_SOURCE_SPAGHETTI_2D);
            this.spaghetti2DElevationModulator = NormalNoise.create(var8.fromHashOf("spaghetti_2d_elevation"), NOISE_MODULATOR_SPAGHETTI_2D_ELEVATION);
            this.spaghetti2DRarityModulator = NormalNoise.create(var8.fromHashOf("spaghetti_2d_modulator"), NOISE_MODULATOR_SPAGHETTI_2D_RARITY);
            this.spaghetti2DThicknessModulator = NormalNoise.create(var8.fromHashOf("spaghetti_2d_thickness"), NOISE_MODULATOR_SPAGHETTI_2D_THICKNESS);
            this.spaghetti3DNoiseSource1 = NormalNoise.create(var8.fromHashOf("spaghetti_3d_1"), NOISE_SOURCE_SPAGHETTI_3D_1);
            this.spaghetti3DNoiseSource2 = NormalNoise.create(var8.fromHashOf("spaghetti_3d_2"), NOISE_SOURCE_SPAGHETTI_3D_2);
            this.spaghetti3DRarityModulator = NormalNoise.create(var8.fromHashOf("spaghetti_3d_rarity"), NOISE_MODULATOR_SPAGHETTI_3D_RARITY);
            this.spaghetti3DThicknessModulator = NormalNoise.create(var8.fromHashOf("spaghetti_3d_thickness"), NOISE_MODULATOR_SPAGHETTI_3D_THICKNESS);
            this.spaghettiRoughnessNoise = NormalNoise.create(var8.fromHashOf("spaghetti_roughness"), NOISE_SPAGHETTI_ROUGHNESS);
            this.spaghettiRoughnessModulator = NormalNoise.create(var8.fromHashOf("spaghetti_roughness_modulator"), NOISE_MODULATOR_SPAGHETTI_ROUGHNESS);
            this.bigEntranceNoiseSource = NormalNoise.create(var8.fromHashOf("cave_entrance"), NOISE_SOURCE_BIG_ENTRANCE);
            this.layerNoiseSource = NormalNoise.create(var8.fromHashOf("cave_layer"), NOISE_SOURCE_LAYER);
            this.cheeseNoiseSource = NormalNoise.create(var8.fromHashOf("cave_cheese"), NOISE_SOURCE_CHEESE);
            this.temperatureNoise = NormalNoise.create(var8.fromHashOf("temperature"), param4.temperature());
            this.humidityNoise = NormalNoise.create(var8.fromHashOf("vegetation"), param4.humidity());
            this.continentalnessNoise = NormalNoise.create(var8.fromHashOf("continentalness"), param4.continentalness());
            this.erosionNoise = NormalNoise.create(var8.fromHashOf("erosion"), param4.erosion());
            this.weirdnessNoise = NormalNoise.create(var8.fromHashOf("ridge"), param4.weirdness());
            this.offsetNoise = NormalNoise.create(var8.fromHashOf("offset"), param4.shift());
            this.veininessBaseNoise = NormalNoise.create(var8.fromHashOf("ore_veininess"), NOISE_VEININESS_BASE);
            this.veininess = yLimitedInterpolatableNoise(this.veininessBaseNoise, var2, var3, 0, 1.5);
            this.veinABaseNoise = NormalNoise.create(var8.fromHashOf("ore_vein_a"), NOISE_VEIN_A_BASE);
            this.veinA = yLimitedInterpolatableNoise(this.veinABaseNoise, var2, var3, 0, 4.0);
            this.veinBBaseNoise = NormalNoise.create(var8.fromHashOf("ore_vein_b"), NOISE_VEIN_B_BASE);
            this.veinB = yLimitedInterpolatableNoise(this.veinBBaseNoise, var2, var3, 0, 4.0);
            this.gapNoise = NormalNoise.create(var8.fromHashOf("ore_gap"), NOISE_GAP);
            this.oreVeinsPositionalRandomFactory = var8.fromHashOf("ore").forkPositional();
            this.noodleToggleBaseNoise = NormalNoise.create(var8.fromHashOf("noodle"), NOISE_NOODLE_TOGGLE_BASE);
            this.noodleToggle = yLimitedInterpolatableNoise(this.noodleToggleBaseNoise, var6, var7, -1, 1.0);
            this.noodleThicknessBaseNoise = NormalNoise.create(var8.fromHashOf("noodle_thickness"), NOISE_NOODLE_THICKNESS_BASE);
            this.noodleThickness = yLimitedInterpolatableNoise(this.noodleThicknessBaseNoise, var6, var7, 0, 1.0);
            this.noodleRidgeABaseNoise = NormalNoise.create(var8.fromHashOf("noodle_ridge_a"), NOISE_NOODLE_RIDGE_A_BASE);
            this.noodleRidgeA = yLimitedInterpolatableNoise(this.noodleRidgeABaseNoise, var6, var7, 0, 2.6666666666666665);
            this.noodleRidgeBBaseNoise = NormalNoise.create(var8.fromHashOf("noodle_ridge_b"), NOISE_NOODLE_RIDGE_B_BASE);
            this.noodleRidgeB = yLimitedInterpolatableNoise(this.noodleRidgeBBaseNoise, var6, var7, 0, 2.6666666666666665);
            this.jaggedNoise = NormalNoise.create(var8.fromHashOf("jagged"), NOISE_JAGGED);
            this.depthBasedLayerPositionalRandomFactory = var8.fromHashOf("depth_based_layer").forkPositional();
        } else {
            RandomSource var9 = param7.newInstance(param6);
            RandomSource var10 = param7.newInstance(param6);
            RandomSource var11 = param3.useLegacyRandom() ? var10 : var9.fork();
            this.blendedNoise = new BlendedNoise(var11, param3.noiseSamplingSettings(), param0, param1);
            RandomSource var12 = var9.fork();
            this.barrierNoise = NormalNoise.createLegacy(var12.fork(), NOISE_BARRIER);
            this.fluidLevelFloodednessNoise = NormalNoise.createLegacy(var12.fork(), NOISE_FLUID_LEVEL_FLOODEDNESS);
            this.lavaNoise = NormalNoise.createLegacy(var12.fork(), NOISE_LAVA);
            this.aquiferPositionalRandomFactory = var12.forkPositional();
            this.fluidLevelSpreadNoise = NormalNoise.createLegacy(var12.fork(), NOISE_FLUID_LEVEL_SPREAD);
            RandomSource var13 = var9.fork();
            this.pillarNoiseSource = NormalNoise.createLegacy(var13.fork(), NOISE_SOURCE_PILLAR);
            this.pillarRarenessModulator = NormalNoise.createLegacy(var13.fork(), NOISE_MODULATOR_PILLAR_RARENESS);
            this.pillarThicknessModulator = NormalNoise.createLegacy(var13.fork(), NOISE_MODULATOR_PILLAR_THICKNESS);
            this.spaghetti2DNoiseSource = NormalNoise.createLegacy(var13.fork(), NOISE_SOURCE_SPAGHETTI_2D);
            this.spaghetti2DElevationModulator = NormalNoise.createLegacy(var13.fork(), NOISE_MODULATOR_SPAGHETTI_2D_ELEVATION);
            this.spaghetti2DRarityModulator = NormalNoise.createLegacy(var13.fork(), NOISE_MODULATOR_SPAGHETTI_2D_RARITY);
            this.spaghetti2DThicknessModulator = NormalNoise.createLegacy(var13.fork(), NOISE_MODULATOR_SPAGHETTI_2D_THICKNESS);
            this.spaghetti3DNoiseSource1 = NormalNoise.createLegacy(var13.fork(), NOISE_SOURCE_SPAGHETTI_3D_1);
            this.spaghetti3DNoiseSource2 = NormalNoise.createLegacy(var13.fork(), NOISE_SOURCE_SPAGHETTI_3D_2);
            this.spaghetti3DRarityModulator = NormalNoise.createLegacy(var13.fork(), NOISE_MODULATOR_SPAGHETTI_3D_RARITY);
            this.spaghetti3DThicknessModulator = NormalNoise.createLegacy(var13.fork(), NOISE_MODULATOR_SPAGHETTI_3D_THICKNESS);
            this.spaghettiRoughnessNoise = NormalNoise.createLegacy(var13.fork(), NOISE_SPAGHETTI_ROUGHNESS);
            this.spaghettiRoughnessModulator = NormalNoise.createLegacy(var13.fork(), NOISE_MODULATOR_SPAGHETTI_ROUGHNESS);
            this.bigEntranceNoiseSource = NormalNoise.createLegacy(var13.fork(), NOISE_SOURCE_BIG_ENTRANCE);
            this.layerNoiseSource = NormalNoise.createLegacy(var13.fork(), NOISE_SOURCE_LAYER);
            this.cheeseNoiseSource = NormalNoise.createLegacy(var13.fork(), NOISE_SOURCE_CHEESE);
            this.temperatureNoise = NormalNoise.createLegacy(param7.newInstance(param6), param4.temperature());
            this.humidityNoise = NormalNoise.createLegacy(param7.newInstance(param6 + 1L), param4.humidity());
            this.continentalnessNoise = NormalNoise.createLegacy(param7.newInstance(param6 + 2L), param4.continentalness());
            this.erosionNoise = NormalNoise.createLegacy(param7.newInstance(param6 + 3L), param4.erosion());
            this.weirdnessNoise = NormalNoise.createLegacy(param7.newInstance(param6 + 4L), param4.weirdness());
            this.offsetNoise = NormalNoise.createLegacy(param7.newInstance(param6 + 5L), param4.shift());
            this.jaggedNoise = NormalNoise.createLegacy(param7.newInstance(param6 + 6L), NOISE_JAGGED);
            RandomSource var14 = var9.fork();
            this.veininessBaseNoise = NormalNoise.createLegacy(var14.fork(), NOISE_VEININESS_BASE);
            this.veininess = yLimitedInterpolatableNoise(this.veininessBaseNoise, var2, var3, 0, 1.5);
            this.veinABaseNoise = NormalNoise.createLegacy(var14.fork(), NOISE_VEIN_A_BASE);
            this.veinA = yLimitedInterpolatableNoise(this.veinABaseNoise, var2, var3, 0, 4.0);
            this.veinBBaseNoise = NormalNoise.createLegacy(var14.fork(), NOISE_VEIN_B_BASE);
            this.veinB = yLimitedInterpolatableNoise(this.veinBBaseNoise, var2, var3, 0, 4.0);
            this.gapNoise = NormalNoise.createLegacy(var14.fork(), NOISE_GAP);
            this.oreVeinsPositionalRandomFactory = var14.forkPositional();
            RandomSource var15 = var9.fork();
            this.noodleToggleBaseNoise = NormalNoise.createLegacy(var15.fork(), NOISE_NOODLE_TOGGLE_BASE);
            this.noodleToggle = yLimitedInterpolatableNoise(this.noodleToggleBaseNoise, var6, var7, -1, 1.0);
            this.noodleThicknessBaseNoise = NormalNoise.createLegacy(var15.fork(), NOISE_NOODLE_THICKNESS_BASE);
            this.noodleThickness = yLimitedInterpolatableNoise(this.noodleThicknessBaseNoise, var6, var7, 0, 1.0);
            this.noodleRidgeABaseNoise = NormalNoise.createLegacy(var15.fork(), NOISE_NOODLE_RIDGE_A_BASE);
            this.noodleRidgeA = yLimitedInterpolatableNoise(this.noodleRidgeABaseNoise, var6, var7, 0, 2.6666666666666665);
            this.noodleRidgeBBaseNoise = NormalNoise.createLegacy(var15.fork(), NOISE_NOODLE_RIDGE_B_BASE);
            this.noodleRidgeB = yLimitedInterpolatableNoise(this.noodleRidgeBBaseNoise, var6, var7, 0, 2.6666666666666665);
            this.depthBasedLayerPositionalRandomFactory = param7.newInstance(param6).forkPositional();
        }

    }

    private static NoiseChunk.InterpolatableNoise yLimitedInterpolatableNoise(NormalNoise param0, int param1, int param2, int param3, double param4) {
        NoiseChunk.NoiseFiller var0 = (param5, param6, param7) -> param6 <= param2 && param6 >= param1
                ? param0.getValue((double)param5 * param4, (double)param6 * param4, (double)param7 * param4)
                : (double)param3;
        return param1x -> param1x.createNoiseInterpolator(var0);
    }

    private double calculateBaseNoise(int param0, int param1, int param2, TerrainInfo param3) {
        double var0 = this.blendedNoise.calculateNoise(param0, param1, param2);
        boolean var1 = !this.isNoiseCavesEnabled;
        return this.calculateBaseNoise(param0, param1, param2, param3, var0, var1, true);
    }

    private double calculateBaseNoise(int param0, int param1, int param2, TerrainInfo param3, double param4, boolean param5, boolean param6) {
        double var0;
        if (this.dimensionDensityFactor == 0.0 && this.dimensionDensityOffset == -0.030078125) {
            var0 = 0.0;
        } else {
            double var1 = param6 ? this.sampleJaggedNoise(param3.jaggedness(), (double)param0, (double)param2) : 0.0;
            double var2 = this.computeDimensionDensity((double)param1);
            double var3 = (var2 + param3.offset() + var1) * param3.factor();
            var0 = var3 * (double)(var3 > 0.0 ? 4 : 1);
        }

        double var5 = var0 + param4;
        double var6 = 1.5625;
        double var17;
        double var18;
        double var16;
        if (!param5 && !(var5 < -64.0)) {
            double var10 = var5 - 1.5625;
            boolean var11 = var10 < 0.0;
            double var12 = this.getBigEntrances(param0, param1, param2);
            double var13 = this.spaghettiRoughness(param0, param1, param2);
            double var14 = this.getSpaghetti3D(param0, param1, param2);
            double var15 = Math.min(var12, var14 + var13);
            if (var11) {
                var16 = var5;
                var17 = var15 * 5.0;
                var18 = -64.0;
            } else {
                double var19 = this.getLayerizedCaverns(param0, param1, param2);
                if (var19 > 64.0) {
                    var16 = 64.0;
                } else {
                    double var21 = this.cheeseNoiseSource.getValue((double)param0, (double)param1 / 1.5, (double)param2);
                    double var22 = Mth.clamp(var21 + 0.27, -1.0, 1.0);
                    double var23 = var10 * 1.28;
                    double var24 = var22 + Mth.clampedLerp(0.5, 0.0, var23);
                    var16 = var24 + var19;
                }

                double var26 = this.getSpaghetti2D(param0, param1, param2);
                var17 = Math.min(var15, var26 + var13);
                var18 = this.getPillars(param0, param1, param2);
            }
        } else {
            var16 = var5;
            var17 = 64.0;
            var18 = -64.0;
        }

        double var29 = Math.max(Math.min(var16, var17), var18);
        var29 = this.applySlide(var29, param1 / this.cellHeight);
        return Mth.clamp(var29, -64.0, 64.0);
    }

    private double sampleJaggedNoise(double param0, double param1, double param2) {
        if (param0 == 0.0) {
            return 0.0;
        } else {
            float var0 = 1500.0F;
            double var1 = this.jaggedNoise.getValue(param1 * 1500.0, 0.0, param2 * 1500.0);
            return var1 > 0.0 ? param0 * var1 : param0 / 2.0 * var1;
        }
    }

    private double computeDimensionDensity(double param0) {
        double var0 = 1.0 - param0 / 128.0;
        return var0 * this.dimensionDensityFactor + this.dimensionDensityOffset;
    }

    private double applySlide(double param0, int param1) {
        int var0 = param1 - this.minCellY;
        param0 = this.noiseSettings.topSlideSettings().applySlide(param0, this.cellCountY - var0);
        return this.noiseSettings.bottomSlideSettings().applySlide(param0, var0);
    }

    protected NoiseChunk.BlockStateFiller makeBaseNoiseFiller(NoiseChunk param0, NoiseChunk.NoiseFiller param1, boolean param2) {
        NoiseChunk.Sampler var0 = this.baseNoise.instantiate(param0);
        NoiseChunk.Sampler var1 = param2 ? this.noodleToggle.instantiate(param0) : () -> -1.0;
        NoiseChunk.Sampler var2 = param2 ? this.noodleThickness.instantiate(param0) : () -> 0.0;
        NoiseChunk.Sampler var3 = param2 ? this.noodleRidgeA.instantiate(param0) : () -> 0.0;
        NoiseChunk.Sampler var4 = param2 ? this.noodleRidgeB.instantiate(param0) : () -> 0.0;
        return (param7, param8, param9) -> {
            double var0x = var0.sample();
            double var1x = Mth.clamp(var0x * 0.64, -1.0, 1.0);
            var1x = var1x / 2.0 - var1x * var1x * var1x / 24.0;
            if (var1.sample() >= 0.0) {
                double var2x = 0.05;
                double var3x = 0.1;
                double var4x = Mth.clampedMap(var2.sample(), -1.0, 1.0, 0.05, 0.1);
                double var5x = Math.abs(1.5 * var3.sample()) - var4x;
                double var6x = Math.abs(1.5 * var4.sample()) - var4x;
                var1x = Math.min(var1x, Math.max(var5x, var6x));
            }

            var1x += param1.calculateNoise(param7, param8, param9);
            return param0.aquifer().computeSubstance(param7, param8, param9, var0x, var1x);
        };
    }

    protected NoiseChunk.BlockStateFiller makeOreVeinifier(NoiseChunk param0, boolean param1) {
        if (!param1) {
            return (param0x, param1x, param2) -> null;
        } else {
            NoiseChunk.Sampler var0 = this.veininess.instantiate(param0);
            NoiseChunk.Sampler var1 = this.veinA.instantiate(param0);
            NoiseChunk.Sampler var2 = this.veinB.instantiate(param0);
            BlockState var3 = null;
            return (param4, param5, param6) -> {
                RandomSource var0x = this.oreVeinsPositionalRandomFactory.at(param4, param5, param6);
                double var1x = var0.sample();
                NoiseSampler.VeinType var2x = this.getVeinType(var1x, param5);
                if (var2x == null) {
                    return var3;
                } else if (var0x.nextFloat() > 0.7F) {
                    return var3;
                } else if (this.isVein(var1.sample(), var2.sample())) {
                    double var3x = Mth.clampedMap(Math.abs(var1x), 0.4F, 0.6F, 0.1F, 0.3F);
                    if ((double)var0x.nextFloat() < var3x && this.gapNoise.getValue((double)param4, (double)param5, (double)param6) > -0.3F) {
                        return var0x.nextFloat() < 0.02F ? var2x.rawOreBlock : var2x.ore;
                    } else {
                        return var2x.filler;
                    }
                } else {
                    return var3;
                }
            };
        }
    }

    protected int getPreliminarySurfaceLevel(int param0, int param1, TerrainInfo param2) {
        for(int var0 = this.minCellY + this.cellCountY; var0 >= this.minCellY; --var0) {
            int var1 = var0 * this.cellHeight;
            double var2 = -0.703125;
            double var3 = this.calculateBaseNoise(param0, var1, param1, param2, -0.703125, true, false);
            if (var3 > 0.390625) {
                return var1;
            }
        }

        return Integer.MAX_VALUE;
    }

    protected Aquifer createAquifer(NoiseChunk param0, int param1, int param2, int param3, int param4, Aquifer.FluidPicker param5, boolean param6) {
        if (!param6) {
            return Aquifer.createDisabled(param5);
        } else {
            int var0 = SectionPos.blockToSectionCoord(param1);
            int var1 = SectionPos.blockToSectionCoord(param2);
            return Aquifer.create(
                param0,
                new ChunkPos(var0, var1),
                this.barrierNoise,
                this.fluidLevelFloodednessNoise,
                this.fluidLevelSpreadNoise,
                this.lavaNoise,
                this.aquiferPositionalRandomFactory,
                this,
                param3 * this.cellHeight,
                param4 * this.cellHeight,
                param5
            );
        }
    }

    @Override
    public Climate.TargetPoint sample(int param0, int param1, int param2) {
        double var0 = (double)param0 + this.getOffset(param0, 0, param2);
        double var1 = (double)param2 + this.getOffset(param2, param0, 0);
        float var2 = (float)this.getContinentalness(var0, 0.0, var1);
        float var3 = (float)this.getErosion(var0, 0.0, var1);
        float var4 = (float)this.getWeirdness(var0, 0.0, var1);
        double var5 = (double)this.shaper.offset(this.shaper.makePoint(var2, var3, var4));
        return this.target(param0, param1, param2, var0, var1, var2, var3, var4, var5);
    }

    protected Climate.TargetPoint target(
        int param0, int param1, int param2, double param3, double param4, float param5, float param6, float param7, double param8
    ) {
        double var0 = (double)param1 + this.getOffset(param1, param2, param0);
        double var1 = this.computeDimensionDensity((double)QuartPos.toBlock(param1)) + param8;
        return Climate.target(
            (float)this.getTemperature(param3, var0, param4), (float)this.getHumidity(param3, var0, param4), param5, param6, (float)var1, param7
        );
    }

    public TerrainInfo terrainInfo(int param0, int param1, float param2, float param3, float param4) {
        if (this.islandNoise != null) {
            double var0 = (double)(TheEndBiomeSource.getHeightValue(this.islandNoise, param0 / 8, param1 / 8) - 8.0F);
            double var1;
            if (var0 > 0.0) {
                var1 = 0.001953125;
            } else {
                var1 = 0.0078125;
            }

            return new TerrainInfo(var0, var1, 0.0);
        } else {
            TerrainShaper.Point var3 = this.shaper.makePoint(param2, param4, param3);
            return new TerrainInfo((double)this.shaper.offset(var3), (double)this.shaper.factor(var3), (double)this.shaper.jaggedness(var3));
        }
    }

    public double getOffset(int param0, int param1, int param2) {
        return this.offsetNoise.getValue((double)param0, (double)param1, (double)param2) * 4.0;
    }

    public double getTemperature(double param0, double param1, double param2) {
        return this.temperatureNoise.getValue(param0, 0.0, param2);
    }

    public double getHumidity(double param0, double param1, double param2) {
        return this.humidityNoise.getValue(param0, 0.0, param2);
    }

    public double getContinentalness(double param0, double param1, double param2) {
        if (SharedConstants.debugGenerateSquareTerrainWithoutNoise) {
            if (SharedConstants.debugVoidTerrain((int)param0 * 4, (int)param2 * 4)) {
                return -1.0;
            } else {
                double var0 = Mth.frac(param0 / 2048.0) * 2.0 - 1.0;
                return var0 * var0 * (double)(var0 < 0.0 ? -1 : 1);
            }
        } else if (SharedConstants.debugGenerateStripedTerrainWithoutNoise) {
            double var1 = param0 * 0.005;
            return Math.sin(var1 + 0.5 * Math.sin(var1));
        } else {
            return this.continentalnessNoise.getValue(param0, param1, param2);
        }
    }

    public double getErosion(double param0, double param1, double param2) {
        if (SharedConstants.debugGenerateSquareTerrainWithoutNoise) {
            if (SharedConstants.debugVoidTerrain((int)param0 * 4, (int)param2 * 4)) {
                return -1.0;
            } else {
                double var0 = Mth.frac(param2 / 256.0) * 2.0 - 1.0;
                return var0 * var0 * (double)(var0 < 0.0 ? -1 : 1);
            }
        } else if (SharedConstants.debugGenerateStripedTerrainWithoutNoise) {
            double var1 = param2 * 0.005;
            return Math.sin(var1 + 0.5 * Math.sin(var1));
        } else {
            return this.erosionNoise.getValue(param0, param1, param2);
        }
    }

    public double getWeirdness(double param0, double param1, double param2) {
        return this.weirdnessNoise.getValue(param0, param1, param2);
    }

    private double getBigEntrances(int param0, int param1, int param2) {
        double var0 = 0.75;
        double var1 = 0.5;
        double var2 = 0.37;
        double var3 = this.bigEntranceNoiseSource.getValue((double)param0 * 0.75, (double)param1 * 0.5, (double)param2 * 0.75) + 0.37;
        int var4 = -10;
        double var5 = (double)(param1 - -10) / 40.0;
        double var6 = 0.3;
        return var3 + Mth.clampedLerp(0.3, 0.0, var5);
    }

    private double getPillars(int param0, int param1, int param2) {
        double var0 = 0.0;
        double var1 = 2.0;
        double var2 = NoiseUtils.sampleNoiseAndMapToRange(this.pillarRarenessModulator, (double)param0, (double)param1, (double)param2, 0.0, 2.0);
        double var3 = 0.0;
        double var4 = 1.1;
        double var5 = NoiseUtils.sampleNoiseAndMapToRange(this.pillarThicknessModulator, (double)param0, (double)param1, (double)param2, 0.0, 1.1);
        var5 = Math.pow(var5, 3.0);
        double var6 = 25.0;
        double var7 = 0.3;
        double var8 = this.pillarNoiseSource.getValue((double)param0 * 25.0, (double)param1 * 0.3, (double)param2 * 25.0);
        var8 = var5 * (var8 * 2.0 - var2);
        return var8 > 0.03 ? var8 : Double.NEGATIVE_INFINITY;
    }

    private double getLayerizedCaverns(int param0, int param1, int param2) {
        double var0 = this.layerNoiseSource.getValue((double)param0, (double)(param1 * 8), (double)param2);
        return Mth.square(var0) * 4.0;
    }

    private double getSpaghetti3D(int param0, int param1, int param2) {
        double var0 = this.spaghetti3DRarityModulator.getValue((double)(param0 * 2), (double)param1, (double)(param2 * 2));
        double var1 = NoiseSampler.QuantizedSpaghettiRarity.getSpaghettiRarity3D(var0);
        double var2 = 0.065;
        double var3 = 0.088;
        double var4 = NoiseUtils.sampleNoiseAndMapToRange(this.spaghetti3DThicknessModulator, (double)param0, (double)param1, (double)param2, 0.065, 0.088);
        double var5 = sampleWithRarity(this.spaghetti3DNoiseSource1, (double)param0, (double)param1, (double)param2, var1);
        double var6 = Math.abs(var1 * var5) - var4;
        double var7 = sampleWithRarity(this.spaghetti3DNoiseSource2, (double)param0, (double)param1, (double)param2, var1);
        double var8 = Math.abs(var1 * var7) - var4;
        return clampToUnit(Math.max(var6, var8));
    }

    private double getSpaghetti2D(int param0, int param1, int param2) {
        double var0 = this.spaghetti2DRarityModulator.getValue((double)(param0 * 2), (double)param1, (double)(param2 * 2));
        double var1 = NoiseSampler.QuantizedSpaghettiRarity.getSphaghettiRarity2D(var0);
        double var2 = 0.6;
        double var3 = 1.3;
        double var4 = NoiseUtils.sampleNoiseAndMapToRange(
            this.spaghetti2DThicknessModulator, (double)(param0 * 2), (double)param1, (double)(param2 * 2), 0.6, 1.3
        );
        double var5 = sampleWithRarity(this.spaghetti2DNoiseSource, (double)param0, (double)param1, (double)param2, var1);
        double var6 = 0.083;
        double var7 = Math.abs(var1 * var5) - 0.083 * var4;
        int var8 = this.minCellY;
        int var9 = 8;
        double var10 = NoiseUtils.sampleNoiseAndMapToRange(this.spaghetti2DElevationModulator, (double)param0, 0.0, (double)param2, (double)var8, 8.0);
        double var11 = Math.abs(var10 - (double)param1 / 8.0) - 1.0 * var4;
        var11 = var11 * var11 * var11;
        return clampToUnit(Math.max(var11, var7));
    }

    private double spaghettiRoughness(int param0, int param1, int param2) {
        double var0 = NoiseUtils.sampleNoiseAndMapToRange(this.spaghettiRoughnessModulator, (double)param0, (double)param1, (double)param2, 0.0, 0.1);
        return (0.4 - Math.abs(this.spaghettiRoughnessNoise.getValue((double)param0, (double)param1, (double)param2))) * var0;
    }

    @VisibleForTesting
    public void parityConfigString(StringBuilder param0) {
        param0.append("blended: ");
        this.blendedNoise.parityConfigString(param0);
        param0.append("\n").append("jagged: ");
        this.jaggedNoise.parityConfigString(param0);
        param0.append("\n").append("barrier: ");
        this.barrierNoise.parityConfigString(param0);
        param0.append("\n").append("fluid level floodedness: ");
        this.fluidLevelFloodednessNoise.parityConfigString(param0);
        param0.append("\n").append("lava: ");
        this.lavaNoise.parityConfigString(param0);
        param0.append("\n").append("aquifer positional: ");
        this.aquiferPositionalRandomFactory.parityConfigString(param0);
        param0.append("\n").append("fluid level spread: ");
        this.fluidLevelSpreadNoise.parityConfigString(param0);
        param0.append("\n").append("layer: ");
        this.layerNoiseSource.parityConfigString(param0);
        param0.append("\n").append("pillar: ");
        this.pillarNoiseSource.parityConfigString(param0);
        param0.append("\n").append("pillar rareness: ");
        this.pillarRarenessModulator.parityConfigString(param0);
        param0.append("\n").append("pillar thickness: ");
        this.pillarThicknessModulator.parityConfigString(param0);
        param0.append("\n").append("spaghetti 2d: ");
        this.spaghetti2DNoiseSource.parityConfigString(param0);
        param0.append("\n").append("spaghetti 2d elev: ");
        this.spaghetti2DElevationModulator.parityConfigString(param0);
        param0.append("\n").append("spaghetti 2d rare: ");
        this.spaghetti2DRarityModulator.parityConfigString(param0);
        param0.append("\n").append("spaghetti 2d thick: ");
        this.spaghetti2DThicknessModulator.parityConfigString(param0);
        param0.append("\n").append("spaghetti 3d 1: ");
        this.spaghetti3DNoiseSource1.parityConfigString(param0);
        param0.append("\n").append("spaghetti 3d 2: ");
        this.spaghetti3DNoiseSource2.parityConfigString(param0);
        param0.append("\n").append("spaghetti 3d rarity: ");
        this.spaghetti3DRarityModulator.parityConfigString(param0);
        param0.append("\n").append("spaghetti 3d thickness: ");
        this.spaghetti3DThicknessModulator.parityConfigString(param0);
        param0.append("\n").append("spaghetti roughness: ");
        this.spaghettiRoughnessNoise.parityConfigString(param0);
        param0.append("\n").append("spaghetti roughness modulator: ");
        this.spaghettiRoughnessModulator.parityConfigString(param0);
        param0.append("\n").append("big entrance: ");
        this.bigEntranceNoiseSource.parityConfigString(param0);
        param0.append("\n").append("cheese: ");
        this.cheeseNoiseSource.parityConfigString(param0);
        param0.append("\n").append("temp: ");
        this.temperatureNoise.parityConfigString(param0);
        param0.append("\n").append("humidity: ");
        this.humidityNoise.parityConfigString(param0);
        param0.append("\n").append("continentalness: ");
        this.continentalnessNoise.parityConfigString(param0);
        param0.append("\n").append("erosion: ");
        this.erosionNoise.parityConfigString(param0);
        param0.append("\n").append("weirdness: ");
        this.weirdnessNoise.parityConfigString(param0);
        param0.append("\n").append("offset: ");
        this.offsetNoise.parityConfigString(param0);
        param0.append("\n").append("gap noise: ");
        this.gapNoise.parityConfigString(param0);
        param0.append("\n").append("veininess: ");
        this.veininessBaseNoise.parityConfigString(param0);
        param0.append("\n").append("vein a: ");
        this.veinABaseNoise.parityConfigString(param0);
        param0.append("\n").append("vein b: ");
        this.veinBBaseNoise.parityConfigString(param0);
        param0.append("\n").append("vein positional: ");
        this.oreVeinsPositionalRandomFactory.parityConfigString(param0);
        param0.append("\n").append("noodle toggle: ");
        this.noodleToggleBaseNoise.parityConfigString(param0);
        param0.append("\n").append("noodle thickness: ");
        this.noodleThicknessBaseNoise.parityConfigString(param0);
        param0.append("\n").append("noodle ridge a: ");
        this.noodleRidgeABaseNoise.parityConfigString(param0);
        param0.append("\n").append("noodle ridge b: ");
        this.noodleRidgeBBaseNoise.parityConfigString(param0);
        param0.append("\n").append("depth based layer: ");
        this.depthBasedLayerPositionalRandomFactory.parityConfigString(param0);
        param0.append("\n");
    }

    public PositionalRandomFactory getDepthBasedLayerPositionalRandom() {
        return this.depthBasedLayerPositionalRandomFactory;
    }

    private static double clampToUnit(double param0) {
        return Mth.clamp(param0, -1.0, 1.0);
    }

    private static double sampleWithRarity(NormalNoise param0, double param1, double param2, double param3, double param4) {
        return param0.getValue(param1 / param4, param2 / param4, param3 / param4);
    }

    private boolean isVein(double param0, double param1) {
        double var0 = Math.abs(1.0 * param0) - 0.08F;
        double var1 = Math.abs(1.0 * param1) - 0.08F;
        return Math.max(var0, var1) < 0.0;
    }

    @Nullable
    private NoiseSampler.VeinType getVeinType(double param0, int param1) {
        NoiseSampler.VeinType var0 = param0 > 0.0 ? NoiseSampler.VeinType.COPPER : NoiseSampler.VeinType.IRON;
        int var1 = var0.maxY - param1;
        int var2 = param1 - var0.minY;
        if (var2 >= 0 && var1 >= 0) {
            int var3 = Math.min(var1, var2);
            double var4 = Mth.clampedMap((double)var3, 0.0, 20.0, -0.2, 0.0);
            return Math.abs(param0) + var4 < 0.4F ? null : var0;
        } else {
            return null;
        }
    }

    static final class QuantizedSpaghettiRarity {
        private QuantizedSpaghettiRarity() {
        }

        static double getSphaghettiRarity2D(double param0) {
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

        static double getSpaghettiRarity3D(double param0) {
            if (param0 < -0.5) {
                return 0.75;
            } else if (param0 < 0.0) {
                return 1.0;
            } else {
                return param0 < 0.5 ? 1.5 : 2.0;
            }
        }
    }

    static enum VeinType {
        COPPER(Blocks.COPPER_ORE.defaultBlockState(), Blocks.RAW_COPPER_BLOCK.defaultBlockState(), Blocks.GRANITE.defaultBlockState(), 0, 50),
        IRON(Blocks.DEEPSLATE_IRON_ORE.defaultBlockState(), Blocks.RAW_IRON_BLOCK.defaultBlockState(), Blocks.TUFF.defaultBlockState(), -60, -8);

        final BlockState ore;
        final BlockState rawOreBlock;
        final BlockState filler;
        final int minY;
        final int maxY;

        private VeinType(BlockState param0, BlockState param1, BlockState param2, int param3, int param4) {
            this.ore = param0;
            this.rawOreBlock = param1;
            this.filler = param2;
            this.minY = param3;
            this.maxY = param4;
        }
    }
}
