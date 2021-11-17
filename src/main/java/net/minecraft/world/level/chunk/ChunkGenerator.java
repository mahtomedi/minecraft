package net.minecraft.world.level.chunk;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.data.worldgen.StructureFeatures;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.DebugLevelSource;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.StructureSettings;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.StrongholdConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public abstract class ChunkGenerator implements BiomeManager.NoiseBiomeSource {
    public static final Codec<ChunkGenerator> CODEC = Registry.CHUNK_GENERATOR.byNameCodec().dispatchStable(ChunkGenerator::codec, Function.identity());
    protected final BiomeSource biomeSource;
    protected final BiomeSource runtimeBiomeSource;
    private final StructureSettings settings;
    private final long strongholdSeed;
    private final List<ChunkPos> strongholdPositions = Lists.newArrayList();

    public ChunkGenerator(BiomeSource param0, StructureSettings param1) {
        this(param0, param0, param1, 0L);
    }

    public ChunkGenerator(BiomeSource param0, BiomeSource param1, StructureSettings param2, long param3) {
        this.biomeSource = param0;
        this.runtimeBiomeSource = param1;
        this.settings = param2;
        this.strongholdSeed = param3;
    }

    private void generateStrongholds() {
        if (this.strongholdPositions.isEmpty()) {
            StrongholdConfiguration var0 = this.settings.stronghold();
            if (var0 != null && var0.count() != 0) {
                List<Biome> var1 = Lists.newArrayList();

                for(Biome var2 : this.biomeSource.possibleBiomes()) {
                    if (validStrongholdBiome(var2)) {
                        var1.add(var2);
                    }
                }

                int var3 = var0.distance();
                int var4 = var0.count();
                int var5 = var0.spread();
                Random var6 = new Random();
                var6.setSeed(this.strongholdSeed);
                double var7 = var6.nextDouble() * Math.PI * 2.0;
                int var8 = 0;
                int var9 = 0;

                for(int var10 = 0; var10 < var4; ++var10) {
                    double var11 = (double)(4 * var3 + var3 * var9 * 6) + (var6.nextDouble() - 0.5) * (double)var3 * 2.5;
                    int var12 = (int)Math.round(Math.cos(var7) * var11);
                    int var13 = (int)Math.round(Math.sin(var7) * var11);
                    BlockPos var14 = this.biomeSource
                        .findBiomeHorizontal(
                            SectionPos.sectionToBlockCoord(var12, 8),
                            0,
                            SectionPos.sectionToBlockCoord(var13, 8),
                            112,
                            var1::contains,
                            var6,
                            this.climateSampler()
                        );
                    if (var14 != null) {
                        var12 = SectionPos.blockToSectionCoord(var14.getX());
                        var13 = SectionPos.blockToSectionCoord(var14.getZ());
                    }

                    this.strongholdPositions.add(new ChunkPos(var12, var13));
                    var7 += (Math.PI * 2) / (double)var5;
                    if (++var8 == var5) {
                        ++var9;
                        var8 = 0;
                        var5 += 2 * var5 / (var9 + 1);
                        var5 = Math.min(var5, var4 - var10);
                        var7 += var6.nextDouble() * Math.PI * 2.0;
                    }
                }

            }
        }
    }

    private static boolean validStrongholdBiome(Biome param0) {
        Biome.BiomeCategory var0 = param0.getBiomeCategory();
        return var0 != Biome.BiomeCategory.OCEAN
            && var0 != Biome.BiomeCategory.RIVER
            && var0 != Biome.BiomeCategory.BEACH
            && var0 != Biome.BiomeCategory.SWAMP
            && var0 != Biome.BiomeCategory.NETHER
            && var0 != Biome.BiomeCategory.THEEND;
    }

    protected abstract Codec<? extends ChunkGenerator> codec();

    public Optional<ResourceKey<Codec<? extends ChunkGenerator>>> getTypeNameForDataFixer() {
        return Registry.CHUNK_GENERATOR.getResourceKey(this.codec());
    }

    public abstract ChunkGenerator withSeed(long var1);

    public CompletableFuture<ChunkAccess> createBiomes(
        Registry<Biome> param0, Executor param1, Blender param2, StructureFeatureManager param3, ChunkAccess param4
    ) {
        return CompletableFuture.supplyAsync(Util.wrapThreadWithTaskName("init_biomes", () -> {
            param4.fillBiomesFromNoise(this.runtimeBiomeSource::getNoiseBiome, this.climateSampler());
            return param4;
        }), Util.backgroundExecutor());
    }

    public abstract Climate.Sampler climateSampler();

    @Override
    public Biome getNoiseBiome(int param0, int param1, int param2) {
        return this.getBiomeSource().getNoiseBiome(param0, param1, param2, this.climateSampler());
    }

    public abstract void applyCarvers(
        WorldGenRegion var1, long var2, BiomeManager var4, StructureFeatureManager var5, ChunkAccess var6, GenerationStep.Carving var7
    );

    @Nullable
    public BlockPos findNearestMapFeature(ServerLevel param0, StructureFeature<?> param1, BlockPos param2, int param3, boolean param4) {
        if (param1 == StructureFeature.STRONGHOLD) {
            this.generateStrongholds();
            BlockPos var0 = null;
            double var1 = Double.MAX_VALUE;
            BlockPos.MutableBlockPos var2 = new BlockPos.MutableBlockPos();

            for(ChunkPos var3 : this.strongholdPositions) {
                var2.set(SectionPos.sectionToBlockCoord(var3.x, 8), 32, SectionPos.sectionToBlockCoord(var3.z, 8));
                double var4 = var2.distSqr(param2);
                if (var0 == null) {
                    var0 = new BlockPos(var2);
                    var1 = var4;
                } else if (var4 < var1) {
                    var0 = new BlockPos(var2);
                    var1 = var4;
                }
            }

            return var0;
        } else {
            StructureFeatureConfiguration var5 = this.settings.getConfig(param1);
            ImmutableMultimap<ConfiguredStructureFeature<?, ?>, ResourceKey<Biome>> var6 = this.settings.structures(param1);
            if (var5 != null && !var6.isEmpty()) {
                Registry<Biome> var7 = param0.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY);
                Set<ResourceKey<Biome>> var8 = this.runtimeBiomeSource
                    .possibleBiomes()
                    .stream()
                    .flatMap(param1x -> var7.getResourceKey(param1x).stream())
                    .collect(Collectors.toSet());
                return var6.values().stream().noneMatch(var8::contains)
                    ? null
                    : param1.getNearestGeneratedFeature(param0, param0.structureFeatureManager(), param2, param3, param4, param0.getSeed(), var5);
            } else {
                return null;
            }
        }
    }

    public void applyBiomeDecoration(WorldGenLevel param0, ChunkAccess param1, StructureFeatureManager param2) {
        ChunkPos var0 = param1.getPos();
        if (!SharedConstants.debugVoidTerrain(var0)) {
            SectionPos var1 = SectionPos.of(var0, param0.getMinSection());
            BlockPos var2 = var1.origin();
            Map<Integer, List<StructureFeature<?>>> var3 = Registry.STRUCTURE_FEATURE
                .stream()
                .collect(Collectors.groupingBy(param0x -> param0x.step().ordinal()));
            List<BiomeSource.StepFeatureData> var4 = this.biomeSource.featuresPerStep();
            WorldgenRandom var5 = new WorldgenRandom(new LegacyRandomSource(RandomSupport.seedUniquifier()));
            long var6 = var5.setDecorationSeed(param0.getSeed(), var2.getX(), var2.getZ());
            Set<Biome> var7 = new ObjectArraySet<>();
            ChunkPos.rangeClosed(var1.chunk(), 1).forEach(param2x -> {
                ChunkAccess var0x = param0.getChunk(param2x.x, param2x.z);

                for(LevelChunkSection var1x : var0x.getSections()) {
                    var1x.getBiomes().getAll(var7::add);
                }

            });
            var7.retainAll(this.biomeSource.possibleBiomes());
            int var8 = var4.size();

            try {
                Registry<PlacedFeature> var9 = param0.registryAccess().registryOrThrow(Registry.PLACED_FEATURE_REGISTRY);
                Registry<StructureFeature<?>> var10 = param0.registryAccess().registryOrThrow(Registry.STRUCTURE_FEATURE_REGISTRY);
                int var11 = Math.max(GenerationStep.Decoration.values().length, var8);

                for(int var12 = 0; var12 < var11; ++var12) {
                    int var13 = 0;
                    if (param2.shouldGenerateFeatures()) {
                        for(StructureFeature<?> var15 : var3.getOrDefault(var12, Collections.emptyList())) {
                            var5.setFeatureSeed(var6, var13, var12);
                            Supplier<String> var16 = () -> var10.getResourceKey(var15).map(Object::toString).orElseGet(var15::toString);

                            try {
                                param0.setCurrentlyGenerating(var16);
                                param2.startsForFeature(var1, var15)
                                    .forEach(param5 -> param5.placeInChunk(param0, param2, this, var5, getWritableArea(param1), var0));
                            } catch (Exception var281) {
                                CrashReport var18 = CrashReport.forThrowable(var281, "Feature placement");
                                var18.addCategory("Feature").setDetail("Description", var16::get);
                                throw new ReportedException(var18);
                            }

                            ++var13;
                        }
                    }

                    if (var12 < var8) {
                        IntSet var19 = new IntArraySet();

                        for(Biome var20 : var7) {
                            List<List<Supplier<PlacedFeature>>> var21 = var20.getGenerationSettings().features();
                            if (var12 < var21.size()) {
                                List<Supplier<PlacedFeature>> var22 = var21.get(var12);
                                BiomeSource.StepFeatureData var23 = var4.get(var12);
                                var22.stream().map(Supplier::get).forEach(param2x -> var19.add(var23.indexMapping().applyAsInt(param2x)));
                            }
                        }

                        int var24 = var19.size();
                        int[] var25 = var19.toIntArray();
                        Arrays.sort(var25);
                        BiomeSource.StepFeatureData var26 = var4.get(var12);

                        for(int var27 = 0; var27 < var24; ++var27) {
                            PlacedFeature var28 = var26.features().get(var25[var27]);
                            Supplier<String> var29 = () -> var9.getResourceKey(var28).map(Object::toString).orElseGet(var28::toString);
                            var5.setFeatureSeed(var6, var13, var12);

                            try {
                                param0.setCurrentlyGenerating(var29);
                                var28.placeWithBiomeCheck(param0, this, var5, var2);
                            } catch (Exception var291) {
                                CrashReport var31 = CrashReport.forThrowable(var291, "Feature placement");
                                var31.addCategory("Feature").setDetail("Description", var29::get);
                                throw new ReportedException(var31);
                            }

                            ++var13;
                        }
                    }
                }

                param0.setCurrentlyGenerating(null);
            } catch (Exception var30) {
                CrashReport var33 = CrashReport.forThrowable(var30, "Biome decoration");
                var33.addCategory("Generation").setDetail("CenterX", var0.x).setDetail("CenterZ", var0.z).setDetail("Seed", var6);
                throw new ReportedException(var33);
            }
        }
    }

    private static BoundingBox getWritableArea(ChunkAccess param0) {
        ChunkPos var0 = param0.getPos();
        int var1 = var0.getMinBlockX();
        int var2 = var0.getMinBlockZ();
        LevelHeightAccessor var3 = param0.getHeightAccessorForGeneration();
        int var4 = var3.getMinBuildHeight() + 1;
        int var5 = var3.getMaxBuildHeight() - 1;
        return new BoundingBox(var1, var4, var2, var1 + 15, var5, var2 + 15);
    }

    public abstract void buildSurface(WorldGenRegion var1, StructureFeatureManager var2, ChunkAccess var3);

    public abstract void spawnOriginalMobs(WorldGenRegion var1);

    public StructureSettings getSettings() {
        return this.settings;
    }

    public int getSpawnHeight(LevelHeightAccessor param0) {
        return 64;
    }

    public BiomeSource getBiomeSource() {
        return this.runtimeBiomeSource;
    }

    public abstract int getGenDepth();

    public WeightedRandomList<MobSpawnSettings.SpawnerData> getMobsAt(Biome param0, StructureFeatureManager param1, MobCategory param2, BlockPos param3) {
        return param0.getMobSettings().getMobs(param2);
    }

    public void createStructures(RegistryAccess param0, StructureFeatureManager param1, ChunkAccess param2, StructureManager param3, long param4) {
        ChunkPos var0 = param2.getPos();
        SectionPos var1 = SectionPos.bottomOf(param2);
        StructureFeatureConfiguration var2 = this.settings.getConfig(StructureFeature.STRONGHOLD);
        if (var2 != null) {
            StructureStart<?> var3 = param1.getStartForFeature(var1, StructureFeature.STRONGHOLD, param2);
            if (var3 == null || !var3.isValid()) {
                StructureStart<?> var4 = StructureFeatures.STRONGHOLD
                    .generate(
                        param0,
                        this,
                        this.biomeSource,
                        param3,
                        param4,
                        var0,
                        fetchReferences(param1, param2, var1, StructureFeature.STRONGHOLD),
                        var2,
                        param2,
                        ChunkGenerator::validStrongholdBiome
                    );
                param1.setStartForFeature(var1, StructureFeature.STRONGHOLD, var4, param2);
            }
        }

        Registry<Biome> var5 = param0.registryOrThrow(Registry.BIOME_REGISTRY);

        label48:
        for(StructureFeature<?> var6 : Registry.STRUCTURE_FEATURE) {
            if (var6 != StructureFeature.STRONGHOLD) {
                StructureFeatureConfiguration var7 = this.settings.getConfig(var6);
                if (var7 != null) {
                    StructureStart<?> var8 = param1.getStartForFeature(var1, var6, param2);
                    if (var8 == null || !var8.isValid()) {
                        int var9 = fetchReferences(param1, param2, var1, var6);

                        for(Entry<ConfiguredStructureFeature<?, ?>, Collection<ResourceKey<Biome>>> var10 : this.settings.structures(var6).asMap().entrySet()) {
                            StructureStart<?> var11 = var10.getKey()
                                .generate(
                                    param0,
                                    this,
                                    this.biomeSource,
                                    param3,
                                    param4,
                                    var0,
                                    var9,
                                    var7,
                                    param2,
                                    param2x -> this.validBiome(var5, var10.getValue()::contains, param2x)
                                );
                            if (var11.isValid()) {
                                param1.setStartForFeature(var1, var6, var11, param2);
                                continue label48;
                            }
                        }

                        param1.setStartForFeature(var1, var6, StructureStart.INVALID_START, param2);
                    }
                }
            }
        }

    }

    private static int fetchReferences(StructureFeatureManager param0, ChunkAccess param1, SectionPos param2, StructureFeature<?> param3) {
        StructureStart<?> var0 = param0.getStartForFeature(param2, param3, param1);
        return var0 != null ? var0.getReferences() : 0;
    }

    protected boolean validBiome(Registry<Biome> param0, Predicate<ResourceKey<Biome>> param1, Biome param2) {
        return param0.getResourceKey(param2).filter(param1).isPresent();
    }

    public void createReferences(WorldGenLevel param0, StructureFeatureManager param1, ChunkAccess param2) {
        int var0 = 8;
        ChunkPos var1 = param2.getPos();
        int var2 = var1.x;
        int var3 = var1.z;
        int var4 = var1.getMinBlockX();
        int var5 = var1.getMinBlockZ();
        SectionPos var6 = SectionPos.bottomOf(param2);

        for(int var7 = var2 - 8; var7 <= var2 + 8; ++var7) {
            for(int var8 = var3 - 8; var8 <= var3 + 8; ++var8) {
                long var9 = ChunkPos.asLong(var7, var8);

                for(StructureStart<?> var10 : param0.getChunk(var7, var8).getAllStarts().values()) {
                    try {
                        if (var10.isValid() && var10.getBoundingBox().intersects(var4, var5, var4 + 15, var5 + 15)) {
                            param1.addReferenceForFeature(var6, var10.getFeature(), var9, param2);
                            DebugPackets.sendStructurePacket(param0, var10);
                        }
                    } catch (Exception var20) {
                        CrashReport var12 = CrashReport.forThrowable(var20, "Generating structure reference");
                        CrashReportCategory var13 = var12.addCategory("Structure");
                        var13.setDetail("Id", () -> Registry.STRUCTURE_FEATURE.getKey(var10.getFeature()).toString());
                        var13.setDetail("Name", () -> var10.getFeature().getFeatureName());
                        var13.setDetail("Class", () -> var10.getFeature().getClass().getCanonicalName());
                        throw new ReportedException(var12);
                    }
                }
            }
        }

    }

    public abstract CompletableFuture<ChunkAccess> fillFromNoise(Executor var1, Blender var2, StructureFeatureManager var3, ChunkAccess var4);

    public abstract int getSeaLevel();

    public abstract int getMinY();

    public abstract int getBaseHeight(int var1, int var2, Heightmap.Types var3, LevelHeightAccessor var4);

    public abstract NoiseColumn getBaseColumn(int var1, int var2, LevelHeightAccessor var3);

    public int getFirstFreeHeight(int param0, int param1, Heightmap.Types param2, LevelHeightAccessor param3) {
        return this.getBaseHeight(param0, param1, param2, param3);
    }

    public int getFirstOccupiedHeight(int param0, int param1, Heightmap.Types param2, LevelHeightAccessor param3) {
        return this.getBaseHeight(param0, param1, param2, param3) - 1;
    }

    public boolean hasStronghold(ChunkPos param0) {
        this.generateStrongholds();
        return this.strongholdPositions.contains(param0);
    }

    static {
        Registry.register(Registry.CHUNK_GENERATOR, "noise", NoiseBasedChunkGenerator.CODEC);
        Registry.register(Registry.CHUNK_GENERATOR, "flat", FlatLevelSource.CODEC);
        Registry.register(Registry.CHUNK_GENERATOR, "debug", DebugLevelSource.CODEC);
    }
}
