package net.minecraft.world.level.levelgen;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.OptionalInt;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.core.SectionPos;
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
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.synth.BlendedNoise;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import net.minecraft.world.level.levelgen.synth.SurfaceNoise;

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
    private final int cellHeight;
    private final int cellWidth;
    private final int cellCountX;
    private final int cellCountY;
    private final int cellCountZ;
    private final SurfaceNoise surfaceNoise;
    private final NormalNoise barrierNoise;
    private final NormalNoise waterLevelNoise;
    protected final BlockState defaultBlock;
    protected final BlockState defaultFluid;
    private final long seed;
    protected final Supplier<NoiseGeneratorSettings> settings;
    private final int height;
    private final NoiseSampler sampler;
    private final boolean aquifersEnabled;
    private final BaseStoneSource baseStoneSource;

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
        this.defaultFluid = var0.getDefaultFluid();
        this.cellCountX = 16 / this.cellWidth;
        this.cellCountY = var1.height() / this.cellHeight;
        this.cellCountZ = 16 / this.cellWidth;
        WorldgenRandom var2 = new WorldgenRandom(param2);
        BlendedNoise var3 = new BlendedNoise(var2);
        this.surfaceNoise = (SurfaceNoise)(var1.useSimplexSurfaceNoise()
            ? new PerlinSimplexNoise(var2, IntStream.rangeClosed(-3, 0))
            : new PerlinNoise(var2, IntStream.rangeClosed(-3, 0)));
        var2.consumeCount(2620);
        PerlinNoise var4 = new PerlinNoise(var2, IntStream.rangeClosed(-15, 0));
        SimplexNoise var6;
        if (var1.islandNoiseOverride()) {
            WorldgenRandom var5 = new WorldgenRandom(param2);
            var5.consumeCount(17292);
            var6 = new SimplexNoise(var5);
        } else {
            var6 = null;
        }

        this.barrierNoise = NormalNoise.create(new SimpleRandomSource(var2.nextLong()), -3, 1.0);
        this.waterLevelNoise = NormalNoise.create(new SimpleRandomSource(var2.nextLong()), -3, 1.0, 0.0, 2.0);
        Cavifier var8 = var0.isNoiseCavesEnabled() ? new Cavifier(var2, var1.minY() / this.cellHeight) : null;
        this.sampler = new NoiseSampler(param0, this.cellWidth, this.cellHeight, this.cellCountY, var1, var3, var6, var4, var8);
        this.aquifersEnabled = var0.isAquifersEnabled();
        this.baseStoneSource = new DepthBasedReplacingBaseStoneSource(param2, this.defaultBlock, Blocks.DEEPSLATE.defaultBlockState(), this.settings);
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

    private double[] makeAndFillNoiseColumn(int param0, int param1, int param2, int param3) {
        double[] var0 = new double[param3 + 1];
        this.fillNoiseColumn(var0, param0, param1, param2, param3);
        return var0;
    }

    private void fillNoiseColumn(double[] param0, int param1, int param2, int param3, int param4) {
        NoiseSettings var0 = this.settings.get().noiseSettings();
        this.sampler.fillNoiseColumn(param0, param1, param2, var0, this.getSeaLevel(), param3, param4);
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

    @Override
    public BaseStoneSource getBaseStoneSource() {
        return this.baseStoneSource;
    }

    private OptionalInt iterateNoiseColumn(
        int param0, int param1, @Nullable BlockState[] param2, @Nullable Predicate<BlockState> param3, int param4, int param5
    ) {
        int var0 = SectionPos.blockToSectionCoord(param0);
        int var1 = SectionPos.blockToSectionCoord(param1);
        int var2 = Math.floorDiv(param0, this.cellWidth);
        int var3 = Math.floorDiv(param1, this.cellWidth);
        int var4 = Math.floorMod(param0, this.cellWidth);
        int var5 = Math.floorMod(param1, this.cellWidth);
        double var6 = (double)var4 / (double)this.cellWidth;
        double var7 = (double)var5 / (double)this.cellWidth;
        double[][] var8 = new double[][]{
            this.makeAndFillNoiseColumn(var2, var3, param4, param5),
            this.makeAndFillNoiseColumn(var2, var3 + 1, param4, param5),
            this.makeAndFillNoiseColumn(var2 + 1, var3, param4, param5),
            this.makeAndFillNoiseColumn(var2 + 1, var3 + 1, param4, param5)
        };
        Aquifer var9 = this.aquifersEnabled
            ? new Aquifer(
                var0, var1, this.barrierNoise, this.waterLevelNoise, this.settings.get(), this.sampler, param4 * this.cellHeight, param5 * this.cellHeight
            )
            : null;

        for(int var10 = param5 - 1; var10 >= 0; --var10) {
            double var11 = var8[0][var10];
            double var12 = var8[1][var10];
            double var13 = var8[2][var10];
            double var14 = var8[3][var10];
            double var15 = var8[0][var10 + 1];
            double var16 = var8[1][var10 + 1];
            double var17 = var8[2][var10 + 1];
            double var18 = var8[3][var10 + 1];

            for(int var19 = this.cellHeight - 1; var19 >= 0; --var19) {
                double var20 = (double)var19 / (double)this.cellHeight;
                double var21 = Mth.lerp3(var20, var6, var7, var11, var15, var13, var17, var12, var16, var14, var18);
                int var22 = var10 * this.cellHeight + var19;
                int var23 = var22 + param4 * this.cellHeight;
                BlockState var24 = this.updateNoiseAndGenerateBaseState(Beardifier.NO_BEARDS, var9, this.baseStoneSource, param0, var23, param1, var21);
                if (param2 != null) {
                    param2[var22] = var24;
                }

                if (param3 != null && param3.test(var24)) {
                    return OptionalInt.of(var23 + 1);
                }
            }
        }

        return OptionalInt.empty();
    }

    protected BlockState updateNoiseAndGenerateBaseState(
        Beardifier param0, @Nullable Aquifer param1, BaseStoneSource param2, int param3, int param4, int param5, double param6
    ) {
        double var0 = Mth.clamp(param6 / 200.0, -1.0, 1.0);
        var0 = var0 / 2.0 - var0 * var0 * var0 / 24.0;
        var0 += param0.beardifyOrBury(param3, param4, param5);
        if (param1 != null && var0 < 0.0) {
            param1.computeAt(param3, param4, param5);
            var0 += param1.getLastBarrierDensity();
        }

        BlockState var1;
        if (var0 > 0.0) {
            var1 = param2.getBaseStone(param3, param4, param5);
        } else if (this.aquifersEnabled && Aquifer.isLavaLevel(param4 - this.getMinY())) {
            var1 = Blocks.LAVA.defaultBlockState();
        } else {
            int var3 = param1 == null ? this.getSeaLevel() : param1.getLastWaterLevel();
            if (param4 < var3) {
                var1 = this.defaultFluid;
            } else {
                var1 = AIR;
            }
        }

        return var1;
    }

    @Override
    public void buildSurfaceAndBedrock(WorldGenRegion param0, ChunkAccess param1) {
        ChunkPos var0 = param1.getPos();
        int var1 = var0.x;
        int var2 = var0.z;
        WorldgenRandom var3 = new WorldgenRandom();
        var3.setBaseChunkSeed(var1, var2);
        ChunkPos var4 = param1.getPos();
        int var5 = var4.getMinBlockX();
        int var6 = var4.getMinBlockZ();
        double var7 = 0.0625;
        BlockPos.MutableBlockPos var8 = new BlockPos.MutableBlockPos();

        for(int var9 = 0; var9 < 16; ++var9) {
            for(int var10 = 0; var10 < 16; ++var10) {
                int var11 = var5 + var9;
                int var12 = var6 + var10;
                int var13 = param1.getHeight(Heightmap.Types.WORLD_SURFACE_WG, var9, var10) + 1;
                double var14 = this.surfaceNoise.getSurfaceNoiseValue((double)var11 * 0.0625, (double)var12 * 0.0625, 0.0625, (double)var9 * 0.0625) * 15.0;
                int var15 = this.settings.get().getMinSurfaceLevel();
                param0.getBiome(var8.set(var5 + var9, var13, var6 + var10))
                    .buildSurfaceAt(var3, param1, var11, var12, var13, var14, this.defaultBlock, this.defaultFluid, this.getSeaLevel(), var15, param0.getSeed());
            }
        }

        this.setBedrock(param1, var3);
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
                LevelChunkSection var9 = param2.getOrCreateSection(var8);
                var9.acquire();
                var7.add(var9);
            }

            return CompletableFuture.<ChunkAccess>supplyAsync(() -> this.doFill(param1, param2, var3, var4), Util.backgroundExecutor())
                .thenApplyAsync(param1x -> {
                    for(LevelChunkSection var0x : var7) {
                        var0x.release();
                    }
    
                    return param1x;
                }, param0);
        }
    }

    private ChunkAccess doFill(StructureFeatureManager param0, ChunkAccess param1, int param2, int param3) {
        Heightmap var0 = param1.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        Heightmap var1 = param1.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
        ChunkPos var2 = param1.getPos();
        int var3 = var2.x;
        int var4 = var2.z;
        int var5 = var2.getMinBlockX();
        int var6 = var2.getMinBlockZ();
        Beardifier var7 = new Beardifier(param0, param1);
        Aquifer var8 = this.aquifersEnabled
            ? new Aquifer(
                var3, var4, this.barrierNoise, this.waterLevelNoise, this.settings.get(), this.sampler, param2 * this.cellHeight, param3 * this.cellHeight
            )
            : null;
        NoiseInterpolator var9 = new NoiseInterpolator(this.cellCountX, param3, this.cellCountZ, var3, var4, param2, this::fillNoiseColumn);
        List<NoiseInterpolator> var10 = ImmutableList.of(var9);
        var10.forEach(NoiseInterpolator::initializeForFirstCellX);
        BlockPos.MutableBlockPos var11 = new BlockPos.MutableBlockPos();

        for(int var12 = 0; var12 < this.cellCountX; ++var12) {
            int var13 = var12;
            var10.forEach(param1x -> param1x.advanceCellX(var13));

            for(int var14 = 0; var14 < this.cellCountZ; ++var14) {
                LevelChunkSection var15 = param1.getOrCreateSection(param1.getSectionsCount() - 1);

                for(int var16 = param3 - 1; var16 >= 0; --var16) {
                    int var17 = var14;
                    int var18 = var16;
                    var10.forEach(param2x -> param2x.selectCellYZ(var18, var17));

                    for(int var19 = this.cellHeight - 1; var19 >= 0; --var19) {
                        int var20 = (param2 + var16) * this.cellHeight + var19;
                        int var21 = var20 & 15;
                        int var22 = param1.getSectionIndex(var20);
                        if (param1.getSectionIndex(var15.bottomBlockY()) != var22) {
                            var15 = param1.getOrCreateSection(var22);
                        }

                        double var23 = (double)var19 / (double)this.cellHeight;
                        var10.forEach(param1x -> param1x.updateForY(var23));

                        for(int var24 = 0; var24 < this.cellWidth; ++var24) {
                            int var25 = var5 + var12 * this.cellWidth + var24;
                            int var26 = var25 & 15;
                            double var27 = (double)var24 / (double)this.cellWidth;
                            var10.forEach(param1x -> param1x.updateForX(var27));

                            for(int var28 = 0; var28 < this.cellWidth; ++var28) {
                                int var29 = var6 + var14 * this.cellWidth + var28;
                                int var30 = var29 & 15;
                                double var31 = (double)var28 / (double)this.cellWidth;
                                double var32 = var9.calculateValue(var31);
                                BlockState var33 = this.updateNoiseAndGenerateBaseState(var7, var8, this.baseStoneSource, var25, var20, var29, var32);
                                if (var33 != AIR) {
                                    if (var33.getLightEmission() != 0 && param1 instanceof ProtoChunk) {
                                        var11.set(var25, var20, var29);
                                        ((ProtoChunk)param1).addLight(var11);
                                    }

                                    var15.setBlockState(var26, var21, var30, var33, false);
                                    var0.update(var26, var20, var30, var33);
                                    var1.update(var26, var20, var30, var33);
                                    if (var8 != null && var8.shouldScheduleWaterUpdate() && !var33.getFluidState().isEmpty()) {
                                        var11.set(var25, var20, var29);
                                        param1.getLiquidTicks().scheduleTick(var11, var33.getFluidState().getType(), 0);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            var10.forEach(NoiseInterpolator::swapSlices);
        }

        return param1;
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
        if (param1.getStructureAt(param3, true, StructureFeature.SWAMP_HUT).isValid()) {
            if (param2 == MobCategory.MONSTER) {
                return StructureFeature.SWAMP_HUT.getSpecialEnemies();
            }

            if (param2 == MobCategory.CREATURE) {
                return StructureFeature.SWAMP_HUT.getSpecialAnimals();
            }
        }

        if (param2 == MobCategory.MONSTER) {
            if (param1.getStructureAt(param3, false, StructureFeature.PILLAGER_OUTPOST).isValid()) {
                return StructureFeature.PILLAGER_OUTPOST.getSpecialEnemies();
            }

            if (param1.getStructureAt(param3, false, StructureFeature.OCEAN_MONUMENT).isValid()) {
                return StructureFeature.OCEAN_MONUMENT.getSpecialEnemies();
            }

            if (param1.getStructureAt(param3, true, StructureFeature.NETHER_BRIDGE).isValid()) {
                return StructureFeature.NETHER_BRIDGE.getSpecialEnemies();
            }
        }

        return super.getMobsAt(param0, param1, param2, param3);
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
