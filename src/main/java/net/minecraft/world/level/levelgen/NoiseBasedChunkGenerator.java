package net.minecraft.world.level.levelgen;

import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.TerrainShaper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public final class NoiseBasedChunkGenerator extends ChunkGenerator {
    public static final Codec<NoiseBasedChunkGenerator> CODEC = RecordCodecBuilder.create(
        param0 -> commonCodec(param0)
                .and(
                    param0.group(
                        RegistryOps.retrieveRegistry(Registry.NOISE_REGISTRY).forGetter(param0x -> param0x.noises),
                        BiomeSource.CODEC.fieldOf("biome_source").forGetter(param0x -> param0x.biomeSource),
                        Codec.LONG.fieldOf("seed").stable().forGetter(param0x -> param0x.seed),
                        NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(param0x -> param0x.settings)
                    )
                )
                .apply(param0, param0.stable(NoiseBasedChunkGenerator::new))
    );
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private static final BlockState[] EMPTY_COLUMN = new BlockState[0];
    protected final BlockState defaultBlock;
    private final Registry<NormalNoise.NoiseParameters> noises;
    private final long seed;
    protected final Holder<NoiseGeneratorSettings> settings;
    private final NoiseRouter router;
    private final Climate.Sampler sampler;
    private final SurfaceSystem surfaceSystem;
    private final Aquifer.FluidPicker globalFluidPicker;

    public NoiseBasedChunkGenerator(
        Registry<StructureSet> param0, Registry<NormalNoise.NoiseParameters> param1, BiomeSource param2, long param3, Holder<NoiseGeneratorSettings> param4
    ) {
        this(param0, param1, param2, param2, param3, param4);
    }

    private NoiseBasedChunkGenerator(
        Registry<StructureSet> param0,
        Registry<NormalNoise.NoiseParameters> param1,
        BiomeSource param2,
        BiomeSource param3,
        long param4,
        Holder<NoiseGeneratorSettings> param5
    ) {
        super(param0, Optional.empty(), param2, param3, param4);
        this.noises = param1;
        this.seed = param4;
        this.settings = param5;
        NoiseGeneratorSettings var0 = this.settings.value();
        this.defaultBlock = var0.defaultBlock();
        NoiseSettings var1 = var0.noiseSettings();
        this.router = var0.createNoiseRouter(param1, param4);
        this.sampler = new Climate.Sampler(
            this.router.temperature(),
            this.router.humidity(),
            this.router.continents(),
            this.router.erosion(),
            this.router.depth(),
            this.router.ridges(),
            this.router.spawnTarget()
        );
        Aquifer.FluidStatus var2 = new Aquifer.FluidStatus(-54, Blocks.LAVA.defaultBlockState());
        int var3 = var0.seaLevel();
        Aquifer.FluidStatus var4 = new Aquifer.FluidStatus(var3, var0.defaultFluid());
        Aquifer.FluidStatus var5 = new Aquifer.FluidStatus(var1.minY() - 1, Blocks.AIR.defaultBlockState());
        this.globalFluidPicker = (param4x, param5x, param6) -> param5x < Math.min(-54, var3) ? var2 : var4;
        this.surfaceSystem = new SurfaceSystem(param1, this.defaultBlock, var3, param4, var0.getRandomSource());
    }

    @Override
    public CompletableFuture<ChunkAccess> createBiomes(
        Registry<Biome> param0, Executor param1, Blender param2, StructureFeatureManager param3, ChunkAccess param4
    ) {
        return CompletableFuture.supplyAsync(Util.wrapThreadWithTaskName("init_biomes", () -> {
            this.doCreateBiomes(param2, param3, param4);
            return param4;
        }), Util.backgroundExecutor());
    }

    private void doCreateBiomes(Blender param0, StructureFeatureManager param1, ChunkAccess param2) {
        NoiseChunk var0 = param2.getOrCreateNoiseChunk(this.router, () -> new Beardifier(param1, param2), this.settings.value(), this.globalFluidPicker, param0);
        BiomeResolver var1 = BelowZeroRetrogen.getBiomeResolver(param0.getBiomeResolver(this.runtimeBiomeSource), param2);
        param2.fillBiomesFromNoise(var1, var0.cachedClimateSampler(this.router));
    }

    @VisibleForDebug
    public NoiseRouter router() {
        return this.router;
    }

    @Override
    public Climate.Sampler climateSampler() {
        return this.sampler;
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public ChunkGenerator withSeed(long param0) {
        return new NoiseBasedChunkGenerator(this.structureSets, this.noises, this.biomeSource.withSeed(param0), param0, this.settings);
    }

    public boolean stable(long param0, ResourceKey<NoiseGeneratorSettings> param1) {
        return this.seed == param0 && this.settings.is(param1);
    }

    @Override
    public int getBaseHeight(int param0, int param1, Heightmap.Types param2, LevelHeightAccessor param3) {
        NoiseSettings var0 = this.settings.value().noiseSettings();
        int var1 = Math.max(var0.minY(), param3.getMinBuildHeight());
        int var2 = Math.min(var0.minY() + var0.height(), param3.getMaxBuildHeight());
        int var3 = Mth.intFloorDiv(var1, var0.getCellHeight());
        int var4 = Mth.intFloorDiv(var2 - var1, var0.getCellHeight());
        return var4 <= 0
            ? param3.getMinBuildHeight()
            : this.iterateNoiseColumn(param0, param1, null, param2.isOpaque(), var3, var4).orElse(param3.getMinBuildHeight());
    }

    @Override
    public NoiseColumn getBaseColumn(int param0, int param1, LevelHeightAccessor param2) {
        NoiseSettings var0 = this.settings.value().noiseSettings();
        int var1 = Math.max(var0.minY(), param2.getMinBuildHeight());
        int var2 = Math.min(var0.minY() + var0.height(), param2.getMaxBuildHeight());
        int var3 = Mth.intFloorDiv(var1, var0.getCellHeight());
        int var4 = Mth.intFloorDiv(var2 - var1, var0.getCellHeight());
        if (var4 <= 0) {
            return new NoiseColumn(var1, EMPTY_COLUMN);
        } else {
            BlockState[] var5 = new BlockState[var4 * var0.getCellHeight()];
            this.iterateNoiseColumn(param0, param1, var5, null, var3, var4);
            return new NoiseColumn(var1, var5);
        }
    }

    @Override
    public void addDebugScreenInfo(List<String> param0, BlockPos param1) {
        DecimalFormat var0 = new DecimalFormat("0.000");
        DensityFunction.SinglePointContext var1 = new DensityFunction.SinglePointContext(param1.getX(), param1.getY(), param1.getZ());
        double var2 = this.router.ridges().compute(var1);
        param0.add(
            "NoiseRouter T: "
                + var0.format(this.router.temperature().compute(var1))
                + " H: "
                + var0.format(this.router.humidity().compute(var1))
                + " C: "
                + var0.format(this.router.continents().compute(var1))
                + " E: "
                + var0.format(this.router.erosion().compute(var1))
                + " D: "
                + var0.format(this.router.depth().compute(var1))
                + " W: "
                + var0.format(var2)
                + " PV: "
                + var0.format((double)TerrainShaper.peaksAndValleys((float)var2))
                + " AS: "
                + var0.format(this.router.initialDensityWithoutJaggedness().compute(var1))
                + " N: "
                + var0.format(this.router.finalDensity().compute(var1))
        );
    }

    private OptionalInt iterateNoiseColumn(
        int param0, int param1, @Nullable BlockState[] param2, @Nullable Predicate<BlockState> param3, int param4, int param5
    ) {
        NoiseSettings var0 = this.settings.value().noiseSettings();
        int var1 = var0.getCellWidth();
        int var2 = var0.getCellHeight();
        int var3 = Math.floorDiv(param0, var1);
        int var4 = Math.floorDiv(param1, var1);
        int var5 = Math.floorMod(param0, var1);
        int var6 = Math.floorMod(param1, var1);
        int var7 = var3 * var1;
        int var8 = var4 * var1;
        double var9 = (double)var5 / (double)var1;
        double var10 = (double)var6 / (double)var1;
        NoiseChunk var11 = NoiseChunk.forColumn(var7, var8, param4, param5, this.router, this.settings.value(), this.globalFluidPicker);
        var11.initializeForFirstCellX();
        var11.advanceCellX(0);

        for(int var12 = param5 - 1; var12 >= 0; --var12) {
            var11.selectCellYZ(var12, 0);

            for(int var13 = var2 - 1; var13 >= 0; --var13) {
                int var14 = (param4 + var12) * var2 + var13;
                double var15 = (double)var13 / (double)var2;
                var11.updateForY(var14, var15);
                var11.updateForX(param0, var9);
                var11.updateForZ(param1, var10);
                BlockState var16 = var11.getInterpolatedState();
                BlockState var17 = var16 == null ? this.defaultBlock : var16;
                if (param2 != null) {
                    int var18 = var12 * var2 + var13;
                    param2[var18] = var17;
                }

                if (param3 != null && param3.test(var17)) {
                    var11.stopInterpolation();
                    return OptionalInt.of(var14 + 1);
                }
            }
        }

        var11.stopInterpolation();
        return OptionalInt.empty();
    }

    @Override
    public void buildSurface(WorldGenRegion param0, StructureFeatureManager param1, ChunkAccess param2) {
        if (!SharedConstants.debugVoidTerrain(param2.getPos())) {
            WorldGenerationContext var0 = new WorldGenerationContext(this, param0);
            NoiseGeneratorSettings var1 = this.settings.value();
            NoiseChunk var2 = param2.getOrCreateNoiseChunk(this.router, () -> new Beardifier(param1, param2), var1, this.globalFluidPicker, Blender.of(param0));
            this.surfaceSystem
                .buildSurface(
                    param0.getBiomeManager(),
                    param0.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY),
                    var1.useLegacyRandomSource(),
                    var0,
                    param2,
                    var2,
                    var1.surfaceRule()
                );
        }
    }

    @Override
    public void applyCarvers(
        WorldGenRegion param0, long param1, BiomeManager param2, StructureFeatureManager param3, ChunkAccess param4, GenerationStep.Carving param5
    ) {
        BiomeManager var0 = param2.withDifferentSource(
            (param0x, param1x, param2x) -> this.biomeSource.getNoiseBiome(param0x, param1x, param2x, this.climateSampler())
        );
        WorldgenRandom var1 = new WorldgenRandom(new LegacyRandomSource(RandomSupport.seedUniquifier()));
        int var2 = 8;
        ChunkPos var3 = param4.getPos();
        NoiseChunk var4 = param4.getOrCreateNoiseChunk(
            this.router, () -> new Beardifier(param3, param4), this.settings.value(), this.globalFluidPicker, Blender.of(param0)
        );
        Aquifer var5 = var4.aquifer();
        CarvingContext var6 = new CarvingContext(this, param0.registryAccess(), param4.getHeightAccessorForGeneration(), var4);
        CarvingMask var7 = ((ProtoChunk)param4).getOrCreateCarvingMask(param5);

        for(int var8 = -8; var8 <= 8; ++var8) {
            for(int var9 = -8; var9 <= 8; ++var9) {
                ChunkPos var10 = new ChunkPos(var3.x + var8, var3.z + var9);
                ChunkAccess var11 = param0.getChunk(var10.x, var10.z);
                BiomeGenerationSettings var12 = var11.carverBiome(
                        () -> this.biomeSource
                                .getNoiseBiome(QuartPos.fromBlock(var10.getMinBlockX()), 0, QuartPos.fromBlock(var10.getMinBlockZ()), this.climateSampler())
                    )
                    .value()
                    .getGenerationSettings();
                Iterable<Holder<ConfiguredWorldCarver<?>>> var13 = var12.getCarvers(param5);
                int var14 = 0;

                for(Holder<ConfiguredWorldCarver<?>> var15 : var13) {
                    ConfiguredWorldCarver<?> var16 = var15.value();
                    var1.setLargeFeatureSeed(param1 + (long)var14, var10.x, var10.z);
                    if (var16.isStartChunk(var1)) {
                        var16.carve(var6, param4, var0::getBiome, var1, var5, var10, var7);
                    }

                    ++var14;
                }
            }
        }

    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Executor param0, Blender param1, StructureFeatureManager param2, ChunkAccess param3) {
        NoiseSettings var0 = this.settings.value().noiseSettings();
        LevelHeightAccessor var1 = param3.getHeightAccessorForGeneration();
        int var2 = Math.max(var0.minY(), var1.getMinBuildHeight());
        int var3 = Math.min(var0.minY() + var0.height(), var1.getMaxBuildHeight());
        int var4 = Mth.intFloorDiv(var2, var0.getCellHeight());
        int var5 = Mth.intFloorDiv(var3 - var2, var0.getCellHeight());
        if (var5 <= 0) {
            return CompletableFuture.completedFuture(param3);
        } else {
            int var6 = param3.getSectionIndex(var5 * var0.getCellHeight() - 1 + var2);
            int var7 = param3.getSectionIndex(var2);
            Set<LevelChunkSection> var8 = Sets.newHashSet();

            for(int var9 = var6; var9 >= var7; --var9) {
                LevelChunkSection var10 = param3.getSection(var9);
                var10.acquire();
                var8.add(var10);
            }

            return CompletableFuture.supplyAsync(
                    Util.wrapThreadWithTaskName("wgen_fill_noise", () -> this.doFill(param1, param2, param3, var4, var5)), Util.backgroundExecutor()
                )
                .whenCompleteAsync((param1x, param2x) -> {
                    for(LevelChunkSection var0x : var8) {
                        var0x.release();
                    }
    
                }, param0);
        }
    }

    private ChunkAccess doFill(Blender param0, StructureFeatureManager param1, ChunkAccess param2, int param3, int param4) {
        NoiseGeneratorSettings var0 = this.settings.value();
        NoiseChunk var1 = param2.getOrCreateNoiseChunk(this.router, () -> new Beardifier(param1, param2), var0, this.globalFluidPicker, param0);
        Heightmap var2 = param2.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        Heightmap var3 = param2.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
        ChunkPos var4 = param2.getPos();
        int var5 = var4.getMinBlockX();
        int var6 = var4.getMinBlockZ();
        Aquifer var7 = var1.aquifer();
        var1.initializeForFirstCellX();
        BlockPos.MutableBlockPos var8 = new BlockPos.MutableBlockPos();
        NoiseSettings var9 = var0.noiseSettings();
        int var10 = var9.getCellWidth();
        int var11 = var9.getCellHeight();
        int var12 = 16 / var10;
        int var13 = 16 / var10;

        for(int var14 = 0; var14 < var12; ++var14) {
            var1.advanceCellX(var14);

            for(int var15 = 0; var15 < var13; ++var15) {
                LevelChunkSection var16 = param2.getSection(param2.getSectionsCount() - 1);

                for(int var17 = param4 - 1; var17 >= 0; --var17) {
                    var1.selectCellYZ(var17, var15);

                    for(int var18 = var11 - 1; var18 >= 0; --var18) {
                        int var19 = (param3 + var17) * var11 + var18;
                        int var20 = var19 & 15;
                        int var21 = param2.getSectionIndex(var19);
                        if (param2.getSectionIndex(var16.bottomBlockY()) != var21) {
                            var16 = param2.getSection(var21);
                        }

                        double var22 = (double)var18 / (double)var11;
                        var1.updateForY(var19, var22);

                        for(int var23 = 0; var23 < var10; ++var23) {
                            int var24 = var5 + var14 * var10 + var23;
                            int var25 = var24 & 15;
                            double var26 = (double)var23 / (double)var10;
                            var1.updateForX(var24, var26);

                            for(int var27 = 0; var27 < var10; ++var27) {
                                int var28 = var6 + var15 * var10 + var27;
                                int var29 = var28 & 15;
                                double var30 = (double)var27 / (double)var10;
                                var1.updateForZ(var28, var30);
                                BlockState var31 = var1.getInterpolatedState();
                                if (var31 == null) {
                                    var31 = this.defaultBlock;
                                }

                                var31 = this.debugPreliminarySurfaceLevel(var1, var24, var19, var28, var31);
                                if (var31 != AIR && !SharedConstants.debugVoidTerrain(param2.getPos())) {
                                    if (var31.getLightEmission() != 0 && param2 instanceof ProtoChunk) {
                                        var8.set(var24, var19, var28);
                                        ((ProtoChunk)param2).addLight(var8);
                                    }

                                    var16.setBlockState(var25, var20, var29, var31, false);
                                    var2.update(var25, var19, var29, var31);
                                    var3.update(var25, var19, var29, var31);
                                    if (var7.shouldScheduleFluidUpdate() && !var31.getFluidState().isEmpty()) {
                                        var8.set(var24, var19, var28);
                                        param2.markPosForPostprocessing(var8);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            var1.swapSlices();
        }

        var1.stopInterpolation();
        return param2;
    }

    private BlockState debugPreliminarySurfaceLevel(NoiseChunk param0, int param1, int param2, int param3, BlockState param4) {
        return param4;
    }

    @Override
    public int getGenDepth() {
        return this.settings.value().noiseSettings().height();
    }

    @Override
    public int getSeaLevel() {
        return this.settings.value().seaLevel();
    }

    @Override
    public int getMinY() {
        return this.settings.value().noiseSettings().minY();
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion param0) {
        if (!this.settings.value().disableMobGeneration()) {
            ChunkPos var0 = param0.getCenter();
            Holder<Biome> var1 = param0.getBiome(var0.getWorldPosition().atY(param0.getMaxBuildHeight() - 1));
            WorldgenRandom var2 = new WorldgenRandom(new LegacyRandomSource(RandomSupport.seedUniquifier()));
            var2.setDecorationSeed(param0.getSeed(), var0.getMinBlockX(), var0.getMinBlockZ());
            NaturalSpawner.spawnMobsForChunkGeneration(param0, var1, var0, var2);
        }
    }

    @Deprecated
    public Optional<BlockState> topMaterial(
        CarvingContext param0, Function<BlockPos, Holder<Biome>> param1, ChunkAccess param2, NoiseChunk param3, BlockPos param4, boolean param5
    ) {
        return this.surfaceSystem.topMaterial(this.settings.value().surfaceRule(), param0, param1, param2, param3, param4, param5);
    }
}
