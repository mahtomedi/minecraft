package net.minecraft.world.level.levelgen;

import java.util.List;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.OverworldBiomeBuilder;
import net.minecraft.world.level.biome.TerrainShaper;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.blending.Blender;
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
    private final NoiseChunk.InterpolatableNoise veininess;
    private final NoiseChunk.InterpolatableNoise veinA;
    private final NoiseChunk.InterpolatableNoise veinB;
    private final NoiseChunk.InterpolatableNoise noodleToggle;
    private final NoiseChunk.InterpolatableNoise noodleThickness;
    private final NoiseChunk.InterpolatableNoise noodleRidgeA;
    private final NoiseChunk.InterpolatableNoise noodleRidgeB;
    private final PositionalRandomFactory aquiferPositionalRandomFactory;
    private final PositionalRandomFactory oreVeinsPositionalRandomFactory;
    private final PositionalRandomFactory depthBasedLayerPositionalRandomFactory;
    private final List<Climate.ParameterPoint> spawnTarget = new OverworldBiomeBuilder().spawnTarget();

    public NoiseSampler(
        int param0,
        int param1,
        int param2,
        NoiseSettings param3,
        boolean param4,
        long param5,
        Registry<NormalNoise.NoiseParameters> param6,
        WorldgenRandom.Algorithm param7
    ) {
        this.cellHeight = param1;
        this.cellCountY = param2;
        this.noiseSettings = param3;
        this.dimensionDensityFactor = param3.densityFactor();
        this.dimensionDensityOffset = param3.densityOffset();
        int var0 = param3.minY();
        this.minCellY = Mth.intFloorDiv(var0, param1);
        this.isNoiseCavesEnabled = param4;
        this.baseNoise = param0x -> param0x.createNoiseInterpolator(
                (param1x, param2x, param3x) -> this.calculateBaseNoise(
                        param1x,
                        param2x,
                        param3x,
                        param0x.noiseData(QuartPos.fromBlock(param1x), QuartPos.fromBlock(param3x)).terrainInfo(),
                        param0x.getBlender()
                    )
            );
        if (param3.islandNoiseOverride()) {
            RandomSource var1 = param7.newInstance(param5);
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
        PositionalRandomFactory var8 = param7.newInstance(param5).forkPositional();
        if (param7 != WorldgenRandom.Algorithm.LEGACY) {
            this.blendedNoise = new BlendedNoise(var8.fromHashOf(new ResourceLocation("terrain")), param3.noiseSamplingSettings(), param0, param1);
            this.temperatureNoise = Noises.instantiate(param6, var8, Noises.TEMPERATURE);
            this.humidityNoise = Noises.instantiate(param6, var8, Noises.VEGETATION);
            this.offsetNoise = Noises.instantiate(param6, var8, Noises.SHIFT);
        } else {
            RandomSource var9 = param7.newInstance(param5);
            this.blendedNoise = new BlendedNoise(param7.newInstance(param5), param3.noiseSamplingSettings(), param0, param1);
            this.temperatureNoise = NormalNoise.createLegacyNetherBiome(param7.newInstance(param5), new NormalNoise.NoiseParameters(-7, 1.0, 1.0));
            this.humidityNoise = NormalNoise.createLegacyNetherBiome(param7.newInstance(param5 + 1L), new NormalNoise.NoiseParameters(-7, 1.0, 1.0));
            this.offsetNoise = NormalNoise.create(var8.fromHashOf(Noises.SHIFT.location()), new NormalNoise.NoiseParameters(0, 0.0));
        }

        this.aquiferPositionalRandomFactory = var8.fromHashOf(new ResourceLocation("aquifer")).forkPositional();
        this.oreVeinsPositionalRandomFactory = var8.fromHashOf(new ResourceLocation("ore")).forkPositional();
        this.depthBasedLayerPositionalRandomFactory = var8.fromHashOf(new ResourceLocation("depth_based_layer")).forkPositional();
        this.barrierNoise = Noises.instantiate(param6, var8, Noises.AQUIFER_BARRIER);
        this.fluidLevelFloodednessNoise = Noises.instantiate(param6, var8, Noises.AQUIFER_FLUID_LEVEL_FLOODEDNESS);
        this.lavaNoise = Noises.instantiate(param6, var8, Noises.AQUIFER_LAVA);
        this.fluidLevelSpreadNoise = Noises.instantiate(param6, var8, Noises.AQUIFER_FLUID_LEVEL_SPREAD);
        this.pillarNoiseSource = Noises.instantiate(param6, var8, Noises.PILLAR);
        this.pillarRarenessModulator = Noises.instantiate(param6, var8, Noises.PILLAR_RARENESS);
        this.pillarThicknessModulator = Noises.instantiate(param6, var8, Noises.PILLAR_THICKNESS);
        this.spaghetti2DNoiseSource = Noises.instantiate(param6, var8, Noises.SPAGHETTI_2D);
        this.spaghetti2DElevationModulator = Noises.instantiate(param6, var8, Noises.SPAGHETTI_2D_ELEVATION);
        this.spaghetti2DRarityModulator = Noises.instantiate(param6, var8, Noises.SPAGHETTI_2D_MODULATOR);
        this.spaghetti2DThicknessModulator = Noises.instantiate(param6, var8, Noises.SPAGHETTI_2D_THICKNESS);
        this.spaghetti3DNoiseSource1 = Noises.instantiate(param6, var8, Noises.SPAGHETTI_3D_1);
        this.spaghetti3DNoiseSource2 = Noises.instantiate(param6, var8, Noises.SPAGHETTI_3D_2);
        this.spaghetti3DRarityModulator = Noises.instantiate(param6, var8, Noises.SPAGHETTI_3D_RARITY);
        this.spaghetti3DThicknessModulator = Noises.instantiate(param6, var8, Noises.SPAGHETTI_3D_THICKNESS);
        this.spaghettiRoughnessNoise = Noises.instantiate(param6, var8, Noises.SPAGHETTI_ROUGHNESS);
        this.spaghettiRoughnessModulator = Noises.instantiate(param6, var8, Noises.SPAGHETTI_ROUGHNESS_MODULATOR);
        this.bigEntranceNoiseSource = Noises.instantiate(param6, var8, Noises.CAVE_ENTRANCE);
        this.layerNoiseSource = Noises.instantiate(param6, var8, Noises.CAVE_LAYER);
        this.cheeseNoiseSource = Noises.instantiate(param6, var8, Noises.CAVE_CHEESE);
        this.continentalnessNoise = Noises.instantiate(param6, var8, Noises.CONTINENTALNESS);
        this.erosionNoise = Noises.instantiate(param6, var8, Noises.EROSION);
        this.weirdnessNoise = Noises.instantiate(param6, var8, Noises.RIDGE);
        this.veininess = yLimitedInterpolatableNoise(Noises.instantiate(param6, var8, Noises.ORE_VEININESS), var2, var3, 0, 1.5);
        this.veinA = yLimitedInterpolatableNoise(Noises.instantiate(param6, var8, Noises.ORE_VEIN_A), var2, var3, 0, 4.0);
        this.veinB = yLimitedInterpolatableNoise(Noises.instantiate(param6, var8, Noises.ORE_VEIN_B), var2, var3, 0, 4.0);
        this.gapNoise = Noises.instantiate(param6, var8, Noises.ORE_GAP);
        this.noodleToggle = yLimitedInterpolatableNoise(Noises.instantiate(param6, var8, Noises.NOODLE), var6, var7, -1, 1.0);
        this.noodleThickness = yLimitedInterpolatableNoise(Noises.instantiate(param6, var8, Noises.NOODLE_THICKNESS), var6, var7, 0, 1.0);
        this.noodleRidgeA = yLimitedInterpolatableNoise(Noises.instantiate(param6, var8, Noises.NOODLE_RIDGE_A), var6, var7, 0, 2.6666666666666665);
        this.noodleRidgeB = yLimitedInterpolatableNoise(Noises.instantiate(param6, var8, Noises.NOODLE_RIDGE_B), var6, var7, 0, 2.6666666666666665);
        this.jaggedNoise = Noises.instantiate(param6, var8, Noises.JAGGED);
    }

    private static NoiseChunk.InterpolatableNoise yLimitedInterpolatableNoise(NormalNoise param0, int param1, int param2, int param3, double param4) {
        NoiseChunk.NoiseFiller var0 = (param5, param6, param7) -> param6 <= param2 && param6 >= param1
                ? param0.getValue((double)param5 * param4, (double)param6 * param4, (double)param7 * param4)
                : (double)param3;
        return param1x -> param1x.createNoiseInterpolator(var0);
    }

    private double calculateBaseNoise(int param0, int param1, int param2, TerrainInfo param3, Blender param4) {
        double var0 = this.blendedNoise.calculateNoise(param0, param1, param2);
        boolean var1 = !this.isNoiseCavesEnabled;
        return this.calculateBaseNoise(param0, param1, param2, param3, var0, var1, true, param4);
    }

    private double calculateBaseNoise(int param0, int param1, int param2, TerrainInfo param3, double param4, boolean param5, boolean param6, Blender param7) {
        double var0;
        if (this.dimensionDensityFactor == 0.0 && this.dimensionDensityOffset == -0.030078125) {
            var0 = 0.0;
        } else {
            double var1 = param6 ? this.sampleJaggedNoise(param3.jaggedness(), (double)param0, (double)param2) : 0.0;
            double var2 = (this.computeBaseDensity(param1, param3) + var1) * param3.factor();
            var0 = var2 * (double)(var2 > 0.0 ? 4 : 1);
        }

        double var4 = var0 + param4;
        double var5 = 1.5625;
        double var15;
        double var16;
        double var17;
        if (!param5 && !(var4 < -64.0)) {
            double var9 = var4 - 1.5625;
            boolean var10 = var9 < 0.0;
            double var11 = this.getBigEntrances(param0, param1, param2);
            double var12 = this.spaghettiRoughness(param0, param1, param2);
            double var13 = this.getSpaghetti3D(param0, param1, param2);
            double var14 = Math.min(var11, var13 + var12);
            if (var10) {
                var15 = var4;
                var16 = var14 * 5.0;
                var17 = -64.0;
            } else {
                double var18 = this.getLayerizedCaverns(param0, param1, param2);
                if (var18 > 64.0) {
                    var15 = 64.0;
                } else {
                    double var20 = this.cheeseNoiseSource.getValue((double)param0, (double)param1 / 1.5, (double)param2);
                    double var21 = Mth.clamp(var20 + 0.27, -1.0, 1.0);
                    double var22 = var9 * 1.28;
                    double var23 = var21 + Mth.clampedLerp(0.5, 0.0, var22);
                    var15 = var23 + var18;
                }

                double var25 = this.getSpaghetti2D(param0, param1, param2);
                var16 = Math.min(var14, var25 + var12);
                var17 = this.getPillars(param0, param1, param2);
            }
        } else {
            var15 = var4;
            var16 = 64.0;
            var17 = -64.0;
        }

        double var28 = Math.max(Math.min(var15, var16), var17);
        var28 = this.applySlide(var28, param1 / this.cellHeight);
        var28 = param7.blendDensity(param0, param1, param2, var28);
        return Mth.clamp(var28, -64.0, 64.0);
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

    private double computeBaseDensity(int param0, TerrainInfo param1) {
        double var0 = 1.0 - (double)param0 / 128.0;
        return var0 * this.dimensionDensityFactor + param1.offset();
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
            double var3 = this.calculateBaseNoise(param0, var1, param1, param2, -0.703125, true, false, Blender.empty());
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

    @VisibleForDebug
    public NoiseSampler.FlatNoiseData noiseData(int param0, int param1, Blender param2) {
        double var0 = (double)param0 + this.getOffset(param0, 0, param1);
        double var1 = (double)param1 + this.getOffset(param1, param0, 0);
        double var2 = this.getContinentalness(var0, 0.0, var1);
        double var3 = this.getWeirdness(var0, 0.0, var1);
        double var4 = this.getErosion(var0, 0.0, var1);
        TerrainInfo var5 = this.terrainInfo(QuartPos.toBlock(param0), QuartPos.toBlock(param1), (float)var2, (float)var3, (float)var4, param2);
        return new NoiseSampler.FlatNoiseData(var0, var1, var2, var3, var4, var5);
    }

    @Override
    public Climate.TargetPoint sample(int param0, int param1, int param2) {
        return this.target(param0, param1, param2, this.noiseData(param0, param2, Blender.empty()));
    }

    @VisibleForDebug
    public Climate.TargetPoint target(int param0, int param1, int param2, NoiseSampler.FlatNoiseData param3) {
        double var0 = param3.shiftedX();
        double var1 = (double)param1 + this.getOffset(param1, param2, param0);
        double var2 = param3.shiftedZ();
        double var3 = this.computeBaseDensity(QuartPos.toBlock(param1), param3.terrainInfo());
        return Climate.target(
            (float)this.getTemperature(var0, var1, var2),
            (float)this.getHumidity(var0, var1, var2),
            (float)param3.continentalness(),
            (float)param3.erosion(),
            (float)var3,
            (float)param3.weirdness()
        );
    }

    public TerrainInfo terrainInfo(int param0, int param1, float param2, float param3, float param4, Blender param5) {
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
            return param5.blendOffsetAndFactor(
                param0, param1, new TerrainInfo((double)this.shaper.offset(var3), (double)this.shaper.factor(var3), (double)this.shaper.jaggedness(var3))
            );
        }
    }

    @Override
    public BlockPos findSpawnPosition() {
        return Climate.findSpawnPosition(this.spawnTarget, this);
    }

    @VisibleForDebug
    public double getOffset(int param0, int param1, int param2) {
        return this.offsetNoise.getValue((double)param0, (double)param1, (double)param2) * 4.0;
    }

    private double getTemperature(double param0, double param1, double param2) {
        return this.temperatureNoise.getValue(param0, 0.0, param2);
    }

    private double getHumidity(double param0, double param1, double param2) {
        return this.humidityNoise.getValue(param0, 0.0, param2);
    }

    @VisibleForDebug
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

    @VisibleForDebug
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

    @VisibleForDebug
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

    public static record FlatNoiseData(double shiftedX, double shiftedZ, double continentalness, double weirdness, double erosion, TerrainInfo terrainInfo) {
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
