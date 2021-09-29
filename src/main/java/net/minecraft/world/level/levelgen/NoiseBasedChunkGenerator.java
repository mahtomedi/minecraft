package net.minecraft.world.level.levelgen;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.ListIterator;
import java.util.OptionalInt;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BlockColumn;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.NetherFortressFeature;
import net.minecraft.world.level.levelgen.feature.OceanMonumentFeature;
import net.minecraft.world.level.levelgen.feature.PillagerOutpostFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.SwamplandHutFeature;
import net.minecraft.world.level.levelgen.material.MaterialRuleList;
import net.minecraft.world.level.levelgen.material.WorldGenMaterialRule;

public final class NoiseBasedChunkGenerator extends ChunkGenerator {
    public static final Codec<NoiseBasedChunkGenerator> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    BiomeSource.CODEC.fieldOf("biome_source").forGetter(param0x -> param0x.biomeSource),
                    Codec.LONG.fieldOf("seed").stable().forGetter(param0x -> param0x.seed),
                    NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(param0x -> param0x.settings)
                )
                .apply(param0, param0.stable(NoiseBasedChunkGenerator::new))
    );
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private static final BlockState[] EMPTY_COLUMN = new BlockState[0];
    private static final int HOW_FAR_BELOW_PRELIMINARY_SURFACE_LEVEL_TO_BUILD_SURFACE = 8;
    private final int cellHeight;
    private final int cellWidth;
    private final int cellCountX;
    private final int cellCountY;
    private final int cellCountZ;
    protected final BlockState defaultBlock;
    private final long seed;
    protected final Supplier<NoiseGeneratorSettings> settings;
    private final int height;
    private final NoiseSampler sampler;
    private final WorldGenMaterialRule materialRule;
    private final Aquifer.FluidPicker globalFluidPicker;

    public NoiseBasedChunkGenerator(BiomeSource param0, long param1, Supplier<NoiseGeneratorSettings> param2) {
        this(param0, param0, param1, param2);
    }

    private NoiseBasedChunkGenerator(BiomeSource param0, BiomeSource param1, long param2, Supplier<NoiseGeneratorSettings> param3) {
        super(param0, param1, param3.get().structureSettings(), param2);
        this.seed = param2;
        NoiseGeneratorSettings var0 = param3.get();
        this.settings = param3;
        NoiseSettings var1 = var0.noiseSettings();
        this.height = var1.height();
        this.cellHeight = QuartPos.toBlock(var1.noiseSizeVertical());
        this.cellWidth = QuartPos.toBlock(var1.noiseSizeHorizontal());
        this.defaultBlock = var0.getDefaultBlock();
        this.cellCountX = 16 / this.cellWidth;
        this.cellCountY = var1.height() / this.cellHeight;
        this.cellCountZ = 16 / this.cellWidth;
        this.sampler = new NoiseSampler(this.cellWidth, this.cellHeight, this.cellCountY, var1, var0.noiseOctaves(), var0.isNoiseCavesEnabled(), param2);
        Builder<WorldGenMaterialRule> var2 = ImmutableList.builder();
        var2.add(NoiseChunk::updateNoiseAndGenerateBaseState);
        var2.add(NoiseChunk::oreVeinify);
        if (var0.isDeepslateEnabled()) {
            SimpleRandomSource var3 = new SimpleRandomSource(param2);
            var2.add(new DepthBasedRule(var3.forkPositional(), Blocks.DEEPSLATE.defaultBlockState()));
        }

        this.materialRule = new MaterialRuleList(var2.build());
        Aquifer.FluidStatus var4 = new Aquifer.FluidStatus(-54, Blocks.LAVA.defaultBlockState());
        Aquifer.FluidStatus var5 = new Aquifer.FluidStatus(var0.seaLevel(), var0.getDefaultFluid());
        Aquifer.FluidStatus var6 = new Aquifer.FluidStatus(var0.noiseSettings().minY() - 1, Blocks.AIR.defaultBlockState());
        this.globalFluidPicker = (param3x, param4, param5) -> param4 < -54 ? var4 : var5;
    }

    @Override
    public CompletableFuture<ChunkAccess> createBiomes(Executor param0, Registry<Biome> param1, StructureFeatureManager param2, ChunkAccess param3) {
        return CompletableFuture.supplyAsync(Util.wrapThreadWithTaskName("init_biomes", () -> {
            this.doCreateBiomes(param1, param2, param3);
            return param3;
        }), Util.backgroundExecutor());
    }

    private void doCreateBiomes(Registry<Biome> param0, StructureFeatureManager param1, ChunkAccess param2) {
        ChunkPos var0 = param2.getPos();
        int var1 = Math.max(this.settings.get().noiseSettings().minY(), param2.getMinBuildHeight());
        int var2 = Math.min(this.settings.get().noiseSettings().minY() + this.settings.get().noiseSettings().height(), param2.getMaxBuildHeight());
        int var3 = Mth.intFloorDiv(var1, this.cellHeight);
        int var4 = Mth.intFloorDiv(var2 - var1, this.cellHeight);
        NoiseChunk var5 = param2.noiseChunk(
            var3,
            var4,
            var0.getMinBlockX(),
            var0.getMinBlockZ(),
            this.cellWidth,
            this.cellHeight,
            this.sampler,
            () -> new Beardifier(param1, param2),
            this.settings,
            this.globalFluidPicker
        );
        param2.fillBiomesFromNoise(this.runtimeBiomeSource, (param1x, param2x, param3) -> {
            double var0x = var5.shiftedX(param1x, param3);
            double var1x = var5.shiftedZ(param1x, param3);
            float var2x = (float)var5.continentalness(param1x, param3);
            float var3x = (float)var5.erosion(param1x, param3);
            float var4x = (float)var5.weirdness(param1x, param3);
            double var5x = var5.terrainInfo(param1x, param3).offset();
            return this.sampler.target(param1x, param2x, param3, var0x, var1x, var2x, var3x, var4x, var5x);
        });
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
        return new NoiseBasedChunkGenerator(this.biomeSource.withSeed(param0), param0, this.settings);
    }

    public boolean stable(long param0, ResourceKey<NoiseGeneratorSettings> param1) {
        return this.seed == param0 && this.settings.get().stable(param1);
    }

    @Override
    public int getBaseHeight(int param0, int param1, Heightmap.Types param2, LevelHeightAccessor param3) {
        int var0 = Math.max(this.settings.get().noiseSettings().minY(), param3.getMinBuildHeight());
        int var1 = Math.min(this.settings.get().noiseSettings().minY() + this.settings.get().noiseSettings().height(), param3.getMaxBuildHeight());
        int var2 = Mth.intFloorDiv(var0, this.cellHeight);
        int var3 = Mth.intFloorDiv(var1 - var0, this.cellHeight);
        return var3 <= 0
            ? param3.getMinBuildHeight()
            : this.iterateNoiseColumn(param0, param1, null, param2.isOpaque(), var2, var3).orElse(param3.getMinBuildHeight());
    }

    @Override
    public NoiseColumn getBaseColumn(int param0, int param1, LevelHeightAccessor param2) {
        int var0 = Math.max(this.settings.get().noiseSettings().minY(), param2.getMinBuildHeight());
        int var1 = Math.min(this.settings.get().noiseSettings().minY() + this.settings.get().noiseSettings().height(), param2.getMaxBuildHeight());
        int var2 = Mth.intFloorDiv(var0, this.cellHeight);
        int var3 = Mth.intFloorDiv(var1 - var0, this.cellHeight);
        if (var3 <= 0) {
            return new NoiseColumn(var0, EMPTY_COLUMN);
        } else {
            BlockState[] var4 = new BlockState[var3 * this.cellHeight];
            this.iterateNoiseColumn(param0, param1, var4, null, var2, var3);
            return new NoiseColumn(var0, var4);
        }
    }

    private OptionalInt iterateNoiseColumn(
        int param0, int param1, @Nullable BlockState[] param2, @Nullable Predicate<BlockState> param3, int param4, int param5
    ) {
        int var0 = Math.floorDiv(param0, this.cellWidth);
        int var1 = Math.floorDiv(param1, this.cellWidth);
        int var2 = Math.floorMod(param0, this.cellWidth);
        int var3 = Math.floorMod(param1, this.cellWidth);
        int var4 = var0 * this.cellWidth;
        int var5 = var1 * this.cellWidth;
        double var6 = (double)var2 / (double)this.cellWidth;
        double var7 = (double)var3 / (double)this.cellWidth;
        NoiseChunk var8 = new NoiseChunk(
            this.cellWidth,
            this.cellHeight,
            1,
            param5,
            param4,
            this.sampler,
            var4,
            var5,
            (param0x, param1x, param2x) -> 0.0,
            this.settings,
            this.globalFluidPicker
        );
        var8.initializeForFirstCellX();
        var8.advanceCellX(0);

        for(int var9 = param5 - 1; var9 >= 0; --var9) {
            var8.selectCellYZ(var9, 0);

            for(int var10 = this.cellHeight - 1; var10 >= 0; --var10) {
                int var11 = (param4 + var9) * this.cellHeight + var10;
                double var12 = (double)var10 / (double)this.cellHeight;
                var8.updateForY(var12);
                var8.updateForX(var6);
                var8.updateForZ(var7);
                BlockState var13 = this.materialRule.apply(var8, param0, var11, param1);
                BlockState var14 = var13 == null ? this.defaultBlock : var13;
                if (param2 != null) {
                    int var15 = var9 * this.cellHeight + var10;
                    param2[var15] = var14;
                }

                if (param3 != null && param3.test(var14)) {
                    return OptionalInt.of(var11 + 1);
                }
            }
        }

        return OptionalInt.empty();
    }

    @Override
    public void buildSurfaceAndBedrock(WorldGenRegion param0, StructureFeatureManager param1, final ChunkAccess param2) {
        ChunkPos var0 = param2.getPos();
        int var1 = var0.x;
        int var2 = var0.z;
        if (!SharedConstants.debugVoidTerrain(var0.getMinBlockX(), var0.getMinBlockZ())) {
            WorldgenRandom var3 = new WorldgenRandom();
            var3.setBaseChunkSeed(var1, var2);
            final ChunkPos var4 = param2.getPos();
            int var5 = var4.getMinBlockX();
            int var6 = var4.getMinBlockZ();
            double var7 = 0.0625;
            BlockPos.MutableBlockPos var8 = new BlockPos.MutableBlockPos();
            final BlockPos.MutableBlockPos var9 = new BlockPos.MutableBlockPos();
            int var10 = Math.max(this.settings.get().noiseSettings().minY(), param2.getMinBuildHeight());
            int var11 = Math.min(this.settings.get().noiseSettings().minY() + this.settings.get().noiseSettings().height(), param2.getMaxBuildHeight());
            int var12 = Mth.intFloorDiv(var10, this.cellHeight);
            int var13 = Mth.intFloorDiv(var11 - var10, this.cellHeight);
            NoiseChunk var14 = param2.noiseChunk(
                var12,
                var13,
                var5,
                var6,
                this.cellWidth,
                this.cellHeight,
                this.sampler,
                () -> new Beardifier(param1, param2),
                this.settings,
                this.globalFluidPicker
            );
            BlockState var15 = this.settings.get().getDefaultFluid();
            BlockColumn var16 = new BlockColumn() {
                @Override
                public BlockState getBlock(int param0) {
                    return param2.getBlockState(var9.setY(param0));
                }

                @Override
                public void setBlock(int param0, BlockState param1) {
                    param2.setBlockState(var9.setY(param0), param1, false);
                }

                @Override
                public String toString() {
                    return "ChunkBlockColumn " + var4;
                }
            };

            for(int var17 = 0; var17 < 16; ++var17) {
                for(int var18 = 0; var18 < 16; ++var18) {
                    int var19 = var5 + var17;
                    int var20 = var6 + var18;
                    int var21 = param2.getHeight(Heightmap.Types.WORLD_SURFACE_WG, var17, var18) + 1;
                    double var22 = this.sampler
                            .getSurfaceNoise()
                            .getSurfaceNoiseValue((double)var19 * 0.0625, (double)var20 * 0.0625, 0.0625, (double)var17 * 0.0625)
                        * 15.0;
                    var9.setX(var19).setZ(var20);
                    int var23 = this.sampler.getPreliminarySurfaceLevel(var19, var20, var14.terrainInfoInterpolated(var19, var20));
                    int var24 = var23 - 8;
                    Biome var25 = param0.getBiome(var8.set(var19, var21, var20));
                    var25.buildSurfaceAt(var3, var16, var19, var20, var21, var22, this.defaultBlock, var15, this.getSeaLevel(), var24, param0.getSeed());
                }
            }

            this.setBedrock(param2, var3);
        }
    }

    private void setBedrock(ChunkAccess param0, Random param1) {
        BlockPos.MutableBlockPos var0 = new BlockPos.MutableBlockPos();
        int var1 = param0.getPos().getMinBlockX();
        int var2 = param0.getPos().getMinBlockZ();
        NoiseGeneratorSettings var3 = this.settings.get();
        int var4 = var3.noiseSettings().minY();
        int var5 = var4 + var3.getBedrockFloorPosition();
        int var6 = this.height - 1 + var4 - var3.getBedrockRoofPosition();
        int var7 = 5;
        int var8 = param0.getMinBuildHeight();
        int var9 = param0.getMaxBuildHeight();
        boolean var10 = var6 + 5 - 1 >= var8 && var6 < var9;
        boolean var11 = var5 + 5 - 1 >= var8 && var5 < var9;
        if (var10 || var11) {
            for(BlockPos var12 : BlockPos.betweenClosed(var1, 0, var2, var1 + 15, 0, var2 + 15)) {
                if (var10) {
                    for(int var13 = 0; var13 < 5; ++var13) {
                        if (var13 <= param1.nextInt(5)) {
                            param0.setBlockState(var0.set(var12.getX(), var6 - var13, var12.getZ()), Blocks.BEDROCK.defaultBlockState(), false);
                        }
                    }
                }

                if (var11) {
                    for(int var14 = 4; var14 >= 0; --var14) {
                        if (var14 <= param1.nextInt(5)) {
                            param0.setBlockState(var0.set(var12.getX(), var5 + var14, var12.getZ()), Blocks.BEDROCK.defaultBlockState(), false);
                        }
                    }
                }
            }

        }
    }

    @Override
    public void applyCarvers(
        WorldGenRegion param0, long param1, BiomeManager param2, StructureFeatureManager param3, ChunkAccess param4, GenerationStep.Carving param5
    ) {
        BiomeManager var0 = param2.withDifferentSource(
            (param0x, param1x, param2x) -> this.biomeSource.getNoiseBiome(param0x, param1x, param2x, this.climateSampler())
        );
        WorldgenRandom var1 = new WorldgenRandom();
        int var2 = 8;
        ChunkPos var3 = param4.getPos();
        CarvingContext var4 = new CarvingContext(this, param4);
        ChunkPos var5 = param4.getPos();
        NoiseSettings var6 = this.settings.get().noiseSettings();
        int var7 = Math.max(var6.minY(), param4.getMinBuildHeight());
        int var8 = Math.min(var6.minY() + var6.height(), param4.getMaxBuildHeight());
        int var9 = Mth.intFloorDiv(var7, this.cellHeight);
        int var10 = Mth.intFloorDiv(var8 - var7, this.cellHeight);
        NoiseChunk var11 = param4.noiseChunk(
            var9,
            var10,
            var5.getMinBlockX(),
            var5.getMinBlockZ(),
            this.cellWidth,
            this.cellHeight,
            this.sampler,
            () -> new Beardifier(param3, param4),
            this.settings,
            this.globalFluidPicker
        );
        Aquifer var12 = var11.aquifer();
        CarvingMask var13 = ((ProtoChunk)param4).getOrCreateCarvingMask(param5);

        for(int var14 = -8; var14 <= 8; ++var14) {
            for(int var15 = -8; var15 <= 8; ++var15) {
                ChunkPos var16 = new ChunkPos(var3.x + var14, var3.z + var15);
                ChunkAccess var17 = param0.getChunk(var16.x, var16.z);
                BiomeGenerationSettings var18 = var17.carverBiome(
                        () -> this.biomeSource
                                .getNoiseBiome(QuartPos.fromBlock(var16.getMinBlockX()), 0, QuartPos.fromBlock(var16.getMinBlockZ()), this.climateSampler())
                    )
                    .getGenerationSettings();
                List<Supplier<ConfiguredWorldCarver<?>>> var19 = var18.getCarvers(param5);
                ListIterator<Supplier<ConfiguredWorldCarver<?>>> var20 = var19.listIterator();

                while(var20.hasNext()) {
                    int var21 = var20.nextIndex();
                    ConfiguredWorldCarver<?> var22 = var20.next().get();
                    var1.setLargeFeatureSeed(param1 + (long)var21, var16.x, var16.z);
                    if (var22.isStartChunk(var1)) {
                        var22.carve(var4, param4, var0::getBiome, var1, var12, var16, var13);
                    }
                }
            }
        }

    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Executor param0, StructureFeatureManager param1, ChunkAccess param2) {
        NoiseSettings var0 = this.settings.get().noiseSettings();
        int var1 = Math.max(var0.minY(), param2.getMinBuildHeight());
        int var2 = Math.min(var0.minY() + var0.height(), param2.getMaxBuildHeight());
        int var3 = Mth.intFloorDiv(var1, this.cellHeight);
        int var4 = Mth.intFloorDiv(var2 - var1, this.cellHeight);
        if (var4 <= 0) {
            return CompletableFuture.completedFuture(param2);
        } else {
            int var5 = param2.getSectionIndex(var4 * this.cellHeight - 1 + var1);
            int var6 = param2.getSectionIndex(var1);
            Set<LevelChunkSection> var7 = Sets.newHashSet();

            for(int var8 = var5; var8 >= var6; --var8) {
                LevelChunkSection var9 = param2.getSection(var8);
                var9.acquire();
                var7.add(var9);
            }

            return CompletableFuture.supplyAsync(
                    Util.wrapThreadWithTaskName("wgen_fill_noise", () -> this.doFill(param1, param2, var3, var4)), Util.backgroundExecutor()
                )
                .whenCompleteAsync((param1x, param2x) -> {
                    for(LevelChunkSection var0x : var7) {
                        var0x.release();
                    }
    
                }, param0);
        }
    }

    private ChunkAccess doFill(StructureFeatureManager param0, ChunkAccess param1, int param2, int param3) {
        Heightmap var0 = param1.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        Heightmap var1 = param1.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
        ChunkPos var2 = param1.getPos();
        int var3 = var2.getMinBlockX();
        int var4 = var2.getMinBlockZ();
        NoiseChunk var5 = param1.noiseChunk(
            param2,
            param3,
            var3,
            var4,
            this.cellWidth,
            this.cellHeight,
            this.sampler,
            () -> new Beardifier(param0, param1),
            this.settings,
            this.globalFluidPicker
        );
        Aquifer var6 = var5.aquifer();
        var5.initializeForFirstCellX();
        BlockPos.MutableBlockPos var7 = new BlockPos.MutableBlockPos();

        for(int var8 = 0; var8 < this.cellCountX; ++var8) {
            var5.advanceCellX(var8);

            for(int var9 = 0; var9 < this.cellCountZ; ++var9) {
                LevelChunkSection var10 = param1.getSection(param1.getSectionsCount() - 1);

                for(int var11 = param3 - 1; var11 >= 0; --var11) {
                    var5.selectCellYZ(var11, var9);

                    for(int var12 = this.cellHeight - 1; var12 >= 0; --var12) {
                        int var13 = (param2 + var11) * this.cellHeight + var12;
                        int var14 = var13 & 15;
                        int var15 = param1.getSectionIndex(var13);
                        if (param1.getSectionIndex(var10.bottomBlockY()) != var15) {
                            var10 = param1.getSection(var15);
                        }

                        double var16 = (double)var12 / (double)this.cellHeight;
                        var5.updateForY(var16);

                        for(int var17 = 0; var17 < this.cellWidth; ++var17) {
                            int var18 = var3 + var8 * this.cellWidth + var17;
                            int var19 = var18 & 15;
                            double var20 = (double)var17 / (double)this.cellWidth;
                            var5.updateForX(var20);

                            for(int var21 = 0; var21 < this.cellWidth; ++var21) {
                                int var22 = var4 + var9 * this.cellWidth + var21;
                                int var23 = var22 & 15;
                                double var24 = (double)var21 / (double)this.cellWidth;
                                var5.updateForZ(var24);
                                BlockState var25 = this.materialRule.apply(var5, var18, var13, var22);
                                if (var25 == null) {
                                    var25 = this.defaultBlock;
                                }

                                var25 = this.debugPreliminarySurfaceLevel(var13, var18, var22, var25);
                                if (var25 != AIR && !SharedConstants.debugVoidTerrain(var18, var22)) {
                                    if (var25.getLightEmission() != 0 && param1 instanceof ProtoChunk) {
                                        var7.set(var18, var13, var22);
                                        ((ProtoChunk)param1).addLight(var7);
                                    }

                                    var10.setBlockState(var19, var14, var23, var25, false);
                                    var0.update(var19, var13, var23, var25);
                                    var1.update(var19, var13, var23, var25);
                                    if (var6.shouldScheduleFluidUpdate() && !var25.getFluidState().isEmpty()) {
                                        var7.set(var18, var13, var22);
                                        param1.getLiquidTicks().scheduleTick(var7, var25.getFluidState().getType(), 0);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            var5.swapSlices();
        }

        return param1;
    }

    private BlockState debugPreliminarySurfaceLevel(int param0, int param1, int param2, BlockState param3) {
        return param3;
    }

    @Override
    public int getGenDepth() {
        return this.height;
    }

    @Override
    public int getSeaLevel() {
        return this.settings.get().seaLevel();
    }

    @Override
    public int getMinY() {
        return this.settings.get().noiseSettings().minY();
    }

    @Override
    public WeightedRandomList<MobSpawnSettings.SpawnerData> getMobsAt(Biome param0, StructureFeatureManager param1, MobCategory param2, BlockPos param3) {
        if (!param1.hasAnyStructureAt(param3)) {
            return super.getMobsAt(param0, param1, param2, param3);
        } else {
            if (param1.getStructureWithPieceAt(param3, StructureFeature.SWAMP_HUT).isValid()) {
                if (param2 == MobCategory.MONSTER) {
                    return SwamplandHutFeature.SWAMPHUT_ENEMIES;
                }

                if (param2 == MobCategory.CREATURE) {
                    return SwamplandHutFeature.SWAMPHUT_ANIMALS;
                }
            }

            if (param2 == MobCategory.MONSTER) {
                if (param1.getStructureAt(param3, StructureFeature.PILLAGER_OUTPOST).isValid()) {
                    return PillagerOutpostFeature.OUTPOST_ENEMIES;
                }

                if (param1.getStructureAt(param3, StructureFeature.OCEAN_MONUMENT).isValid()) {
                    return OceanMonumentFeature.MONUMENT_ENEMIES;
                }

                if (param1.getStructureWithPieceAt(param3, StructureFeature.NETHER_BRIDGE).isValid()) {
                    return NetherFortressFeature.FORTRESS_ENEMIES;
                }
            }

            return (param2 == MobCategory.UNDERGROUND_WATER_CREATURE || param2 == MobCategory.AXOLOTLS)
                    && param1.getStructureAt(param3, StructureFeature.OCEAN_MONUMENT).isValid()
                ? MobSpawnSettings.EMPTY_MOB_LIST
                : super.getMobsAt(param0, param1, param2, param3);
        }
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion param0) {
        if (!this.settings.get().disableMobGeneration()) {
            ChunkPos var0 = param0.getCenter();
            Biome var1 = param0.getBiome(var0.getWorldPosition());
            WorldgenRandom var2 = new WorldgenRandom();
            var2.setDecorationSeed(param0.getSeed(), var0.getMinBlockX(), var0.getMinBlockZ());
            NaturalSpawner.spawnMobsForChunkGeneration(param0, var1, var0, var2);
        }
    }
}
