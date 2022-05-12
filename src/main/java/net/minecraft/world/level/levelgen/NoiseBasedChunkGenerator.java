package net.minecraft.world.level.levelgen;

import com.google.common.annotations.VisibleForTesting;
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
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.apache.commons.lang3.mutable.MutableObject;

public final class NoiseBasedChunkGenerator extends ChunkGenerator {
    public static final Codec<NoiseBasedChunkGenerator> CODEC = RecordCodecBuilder.create(
        param0 -> commonCodec(param0)
                .and(
                    param0.group(
                        RegistryOps.retrieveRegistry(Registry.NOISE_REGISTRY).forGetter(param0x -> param0x.noises),
                        BiomeSource.CODEC.fieldOf("biome_source").forGetter(param0x -> param0x.biomeSource),
                        NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(param0x -> param0x.settings)
                    )
                )
                .apply(param0, param0.stable(NoiseBasedChunkGenerator::new))
    );
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    protected final BlockState defaultBlock;
    private final Registry<NormalNoise.NoiseParameters> noises;
    protected final Holder<NoiseGeneratorSettings> settings;
    private final Aquifer.FluidPicker globalFluidPicker;

    public NoiseBasedChunkGenerator(
        Registry<StructureSet> param0, Registry<NormalNoise.NoiseParameters> param1, BiomeSource param2, Holder<NoiseGeneratorSettings> param3
    ) {
        this(param0, param1, param2, param2, param3);
    }

    private NoiseBasedChunkGenerator(
        Registry<StructureSet> param0,
        Registry<NormalNoise.NoiseParameters> param1,
        BiomeSource param2,
        BiomeSource param3,
        Holder<NoiseGeneratorSettings> param4
    ) {
        super(param0, Optional.empty(), param2, param3);
        this.noises = param1;
        this.settings = param4;
        NoiseGeneratorSettings var0 = this.settings.value();
        this.defaultBlock = var0.defaultBlock();
        Aquifer.FluidStatus var1 = new Aquifer.FluidStatus(-54, Blocks.LAVA.defaultBlockState());
        int var2 = var0.seaLevel();
        Aquifer.FluidStatus var3 = new Aquifer.FluidStatus(var2, var0.defaultFluid());
        Aquifer.FluidStatus var4 = new Aquifer.FluidStatus(DimensionType.MIN_Y * 2, Blocks.AIR.defaultBlockState());
        this.globalFluidPicker = (param4x, param5, param6) -> param5 < Math.min(-54, var2) ? var1 : var3;
    }

    @Override
    public CompletableFuture<ChunkAccess> createBiomes(
        Registry<Biome> param0, Executor param1, RandomState param2, Blender param3, StructureManager param4, ChunkAccess param5
    ) {
        return CompletableFuture.supplyAsync(Util.wrapThreadWithTaskName("init_biomes", () -> {
            this.doCreateBiomes(param3, param2, param4, param5);
            return param5;
        }), Util.backgroundExecutor());
    }

    private void doCreateBiomes(Blender param0, RandomState param1, StructureManager param2, ChunkAccess param3) {
        NoiseChunk var0 = param3.getOrCreateNoiseChunk(param3x -> this.createNoiseChunk(param3x, param2, param0, param1));
        BiomeResolver var1 = BelowZeroRetrogen.getBiomeResolver(param0.getBiomeResolver(this.runtimeBiomeSource), param3);
        param3.fillBiomesFromNoise(var1, var0.cachedClimateSampler(param1.router(), this.settings.value().spawnTarget()));
    }

    private NoiseChunk createNoiseChunk(ChunkAccess param0, StructureManager param1, Blender param2, RandomState param3) {
        return NoiseChunk.forChunk(
            param0, param3, Beardifier.forStructuresInChunk(param1, param0.getPos()), this.settings.value(), this.globalFluidPicker, param2
        );
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    public Holder<NoiseGeneratorSettings> generatorSettings() {
        return this.settings;
    }

    public boolean stable(ResourceKey<NoiseGeneratorSettings> param0) {
        return this.settings.is(param0);
    }

    @Override
    public int getBaseHeight(int param0, int param1, Heightmap.Types param2, LevelHeightAccessor param3, RandomState param4) {
        return this.iterateNoiseColumn(param3, param4, param0, param1, null, param2.isOpaque()).orElse(param3.getMinBuildHeight());
    }

    @Override
    public NoiseColumn getBaseColumn(int param0, int param1, LevelHeightAccessor param2, RandomState param3) {
        MutableObject<NoiseColumn> var0 = new MutableObject<>();
        this.iterateNoiseColumn(param2, param3, param0, param1, var0, null);
        return var0.getValue();
    }

    @Override
    public void addDebugScreenInfo(List<String> param0, RandomState param1, BlockPos param2) {
        DecimalFormat var0 = new DecimalFormat("0.000");
        NoiseRouter var1 = param1.router();
        DensityFunction.SinglePointContext var2 = new DensityFunction.SinglePointContext(param2.getX(), param2.getY(), param2.getZ());
        double var3 = var1.ridges().compute(var2);
        param0.add(
            "NoiseRouter T: "
                + var0.format(var1.temperature().compute(var2))
                + " V: "
                + var0.format(var1.vegetation().compute(var2))
                + " C: "
                + var0.format(var1.continents().compute(var2))
                + " E: "
                + var0.format(var1.erosion().compute(var2))
                + " D: "
                + var0.format(var1.depth().compute(var2))
                + " W: "
                + var0.format(var3)
                + " PV: "
                + var0.format((double)NoiseRouterData.peaksAndValleys((float)var3))
                + " AS: "
                + var0.format(var1.initialDensityWithoutJaggedness().compute(var2))
                + " N: "
                + var0.format(var1.finalDensity().compute(var2))
        );
    }

    private OptionalInt iterateNoiseColumn(
        LevelHeightAccessor param0,
        RandomState param1,
        int param2,
        int param3,
        @Nullable MutableObject<NoiseColumn> param4,
        @Nullable Predicate<BlockState> param5
    ) {
        NoiseSettings var0 = this.settings.value().noiseSettings().clampToHeightAccessor(param0);
        int var1 = var0.getCellHeight();
        int var2 = var0.minY();
        int var3 = Mth.intFloorDiv(var2, var1);
        int var4 = Mth.intFloorDiv(var0.height(), var1);
        if (var4 <= 0) {
            return OptionalInt.empty();
        } else {
            BlockState[] var5;
            if (param4 == null) {
                var5 = null;
            } else {
                var5 = new BlockState[var0.height()];
                param4.setValue(new NoiseColumn(var2, var5));
            }

            int var7 = var0.getCellWidth();
            int var8 = Math.floorDiv(param2, var7);
            int var9 = Math.floorDiv(param3, var7);
            int var10 = Math.floorMod(param2, var7);
            int var11 = Math.floorMod(param3, var7);
            int var12 = var8 * var7;
            int var13 = var9 * var7;
            double var14 = (double)var10 / (double)var7;
            double var15 = (double)var11 / (double)var7;
            NoiseChunk var16 = new NoiseChunk(
                1, param1, var12, var13, var0, DensityFunctions.BeardifierMarker.INSTANCE, this.settings.value(), this.globalFluidPicker, Blender.empty()
            );
            var16.initializeForFirstCellX();
            var16.advanceCellX(0);

            for(int var17 = var4 - 1; var17 >= 0; --var17) {
                var16.selectCellYZ(var17, 0);

                for(int var18 = var1 - 1; var18 >= 0; --var18) {
                    int var19 = (var3 + var17) * var1 + var18;
                    double var20 = (double)var18 / (double)var1;
                    var16.updateForY(var19, var20);
                    var16.updateForX(param2, var14);
                    var16.updateForZ(param3, var15);
                    BlockState var21 = var16.getInterpolatedState();
                    BlockState var22 = var21 == null ? this.defaultBlock : var21;
                    if (var5 != null) {
                        int var23 = var17 * var1 + var18;
                        var5[var23] = var22;
                    }

                    if (param5 != null && param5.test(var22)) {
                        var16.stopInterpolation();
                        return OptionalInt.of(var19 + 1);
                    }
                }
            }

            var16.stopInterpolation();
            return OptionalInt.empty();
        }
    }

    @Override
    public void buildSurface(WorldGenRegion param0, StructureManager param1, RandomState param2, ChunkAccess param3) {
        if (!SharedConstants.debugVoidTerrain(param3.getPos())) {
            WorldGenerationContext var0 = new WorldGenerationContext(this, param0);
            this.buildSurface(
                param3, var0, param2, param1, param0.getBiomeManager(), param0.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY), Blender.of(param0)
            );
        }
    }

    @VisibleForTesting
    public void buildSurface(
        ChunkAccess param0,
        WorldGenerationContext param1,
        RandomState param2,
        StructureManager param3,
        BiomeManager param4,
        Registry<Biome> param5,
        Blender param6
    ) {
        NoiseChunk var0 = param0.getOrCreateNoiseChunk(param3x -> this.createNoiseChunk(param3x, param3, param6, param2));
        NoiseGeneratorSettings var1 = this.settings.value();
        param2.surfaceSystem().buildSurface(param2, param4, param5, var1.useLegacyRandomSource(), param1, param0, var0, var1.surfaceRule());
    }

    @Override
    public void applyCarvers(
        WorldGenRegion param0, long param1, RandomState param2, BiomeManager param3, StructureManager param4, ChunkAccess param5, GenerationStep.Carving param6
    ) {
        BiomeManager var0 = param3.withDifferentSource(
            (param1x, param2x, param3x) -> this.biomeSource.getNoiseBiome(param1x, param2x, param3x, param2.sampler())
        );
        WorldgenRandom var1 = new WorldgenRandom(new LegacyRandomSource(RandomSupport.generateUniqueSeed()));
        int var2 = 8;
        ChunkPos var3 = param5.getPos();
        NoiseChunk var4 = param5.getOrCreateNoiseChunk(param3x -> this.createNoiseChunk(param3x, param4, Blender.of(param0), param2));
        Aquifer var5 = var4.aquifer();
        CarvingContext var6 = new CarvingContext(
            this, param0.registryAccess(), param5.getHeightAccessorForGeneration(), var4, param2, this.settings.value().surfaceRule()
        );
        CarvingMask var7 = ((ProtoChunk)param5).getOrCreateCarvingMask(param6);

        for(int var8 = -8; var8 <= 8; ++var8) {
            for(int var9 = -8; var9 <= 8; ++var9) {
                ChunkPos var10 = new ChunkPos(var3.x + var8, var3.z + var9);
                ChunkAccess var11 = param0.getChunk(var10.x, var10.z);
                BiomeGenerationSettings var12 = var11.carverBiome(
                        () -> this.biomeSource
                                .getNoiseBiome(QuartPos.fromBlock(var10.getMinBlockX()), 0, QuartPos.fromBlock(var10.getMinBlockZ()), param2.sampler())
                    )
                    .value()
                    .getGenerationSettings();
                Iterable<Holder<ConfiguredWorldCarver<?>>> var13 = var12.getCarvers(param6);
                int var14 = 0;

                for(Holder<ConfiguredWorldCarver<?>> var15 : var13) {
                    ConfiguredWorldCarver<?> var16 = var15.value();
                    var1.setLargeFeatureSeed(param1 + (long)var14, var10.x, var10.z);
                    if (var16.isStartChunk(var1)) {
                        var16.carve(var6, param5, var0::getBiome, var1, var5, var10, var7);
                    }

                    ++var14;
                }
            }
        }

    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Executor param0, Blender param1, RandomState param2, StructureManager param3, ChunkAccess param4) {
        NoiseSettings var0 = this.settings.value().noiseSettings().clampToHeightAccessor(param4.getHeightAccessorForGeneration());
        int var1 = var0.minY();
        int var2 = Mth.intFloorDiv(var1, var0.getCellHeight());
        int var3 = Mth.intFloorDiv(var0.height(), var0.getCellHeight());
        if (var3 <= 0) {
            return CompletableFuture.completedFuture(param4);
        } else {
            int var4 = param4.getSectionIndex(var3 * var0.getCellHeight() - 1 + var1);
            int var5 = param4.getSectionIndex(var1);
            Set<LevelChunkSection> var6 = Sets.newHashSet();

            for(int var7 = var4; var7 >= var5; --var7) {
                LevelChunkSection var8 = param4.getSection(var7);
                var8.acquire();
                var6.add(var8);
            }

            return CompletableFuture.supplyAsync(
                    Util.wrapThreadWithTaskName("wgen_fill_noise", () -> this.doFill(param1, param3, param2, param4, var2, var3)), Util.backgroundExecutor()
                )
                .whenCompleteAsync((param1x, param2x) -> {
                    for(LevelChunkSection var0x : var6) {
                        var0x.release();
                    }
    
                }, param0);
        }
    }

    private ChunkAccess doFill(Blender param0, StructureManager param1, RandomState param2, ChunkAccess param3, int param4, int param5) {
        NoiseChunk var0 = param3.getOrCreateNoiseChunk(param3x -> this.createNoiseChunk(param3x, param1, param0, param2));
        Heightmap var1 = param3.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        Heightmap var2 = param3.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
        ChunkPos var3 = param3.getPos();
        int var4 = var3.getMinBlockX();
        int var5 = var3.getMinBlockZ();
        Aquifer var6 = var0.aquifer();
        var0.initializeForFirstCellX();
        BlockPos.MutableBlockPos var7 = new BlockPos.MutableBlockPos();
        int var8 = var0.cellWidth();
        int var9 = var0.cellHeight();
        int var10 = 16 / var8;
        int var11 = 16 / var8;

        for(int var12 = 0; var12 < var10; ++var12) {
            var0.advanceCellX(var12);

            for(int var13 = 0; var13 < var11; ++var13) {
                LevelChunkSection var14 = param3.getSection(param3.getSectionsCount() - 1);

                for(int var15 = param5 - 1; var15 >= 0; --var15) {
                    var0.selectCellYZ(var15, var13);

                    for(int var16 = var9 - 1; var16 >= 0; --var16) {
                        int var17 = (param4 + var15) * var9 + var16;
                        int var18 = var17 & 15;
                        int var19 = param3.getSectionIndex(var17);
                        if (param3.getSectionIndex(var14.bottomBlockY()) != var19) {
                            var14 = param3.getSection(var19);
                        }

                        double var20 = (double)var16 / (double)var9;
                        var0.updateForY(var17, var20);

                        for(int var21 = 0; var21 < var8; ++var21) {
                            int var22 = var4 + var12 * var8 + var21;
                            int var23 = var22 & 15;
                            double var24 = (double)var21 / (double)var8;
                            var0.updateForX(var22, var24);

                            for(int var25 = 0; var25 < var8; ++var25) {
                                int var26 = var5 + var13 * var8 + var25;
                                int var27 = var26 & 15;
                                double var28 = (double)var25 / (double)var8;
                                var0.updateForZ(var26, var28);
                                BlockState var29 = var0.getInterpolatedState();
                                if (var29 == null) {
                                    var29 = this.defaultBlock;
                                }

                                var29 = this.debugPreliminarySurfaceLevel(var0, var22, var17, var26, var29);
                                if (var29 != AIR && !SharedConstants.debugVoidTerrain(param3.getPos())) {
                                    if (var29.getLightEmission() != 0 && param3 instanceof ProtoChunk) {
                                        var7.set(var22, var17, var26);
                                        ((ProtoChunk)param3).addLight(var7);
                                    }

                                    var14.setBlockState(var23, var18, var27, var29, false);
                                    var1.update(var23, var17, var27, var29);
                                    var2.update(var23, var17, var27, var29);
                                    if (var6.shouldScheduleFluidUpdate() && !var29.getFluidState().isEmpty()) {
                                        var7.set(var22, var17, var26);
                                        param3.markPosForPostprocessing(var7);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            var0.swapSlices();
        }

        var0.stopInterpolation();
        return param3;
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
            WorldgenRandom var2 = new WorldgenRandom(new LegacyRandomSource(RandomSupport.generateUniqueSeed()));
            var2.setDecorationSeed(param0.getSeed(), var0.getMinBlockX(), var0.getMinBlockZ());
            NaturalSpawner.spawnMobsForChunkGeneration(param0, var1, var0, var2);
        }
    }
}
