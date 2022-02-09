package net.minecraft.world.level.chunk;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import java.util.ArrayList;
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
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.LevelReader;
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
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.StructureSettings;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureCheckResult;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.placement.ConcentricRingsStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public abstract class ChunkGenerator implements BiomeManager.NoiseBiomeSource {
    public static final Codec<ChunkGenerator> CODEC = Registry.CHUNK_GENERATOR.byNameCodec().dispatchStable(ChunkGenerator::codec, Function.identity());
    protected final BiomeSource biomeSource;
    protected final BiomeSource runtimeBiomeSource;
    private final StructureSettings settings;
    private final Map<ConcentricRingsStructurePlacement, ArrayList<ChunkPos>> ringPositions;
    private final long seed;

    public ChunkGenerator(BiomeSource param0, StructureSettings param1) {
        this(param0, param0, param1, 0L);
    }

    public ChunkGenerator(BiomeSource param0, BiomeSource param1, StructureSettings param2, long param3) {
        this.biomeSource = param0;
        this.runtimeBiomeSource = param1;
        this.settings = param2;
        this.seed = param3;
        this.ringPositions = new Object2ObjectArrayMap<>();

        for(Entry<StructureFeature<?>, StructurePlacement> var0 : param2.structureConfig().entrySet()) {
            Object var9 = var0.getValue();
            if (var9 instanceof ConcentricRingsStructurePlacement var1) {
                this.ringPositions.put(var1, new ArrayList<>());
            }
        }

    }

    protected void postInit() {
        for(Entry<StructureFeature<?>, StructurePlacement> var0 : this.settings.structureConfig().entrySet()) {
            Object var4 = var0.getValue();
            if (var4 instanceof ConcentricRingsStructurePlacement var1) {
                this.generateRingPositions(var0.getKey(), var1);
            }
        }

    }

    private void generateRingPositions(StructureFeature<?> param0, ConcentricRingsStructurePlacement param1) {
        if (param1.count() != 0) {
            Predicate<ResourceKey<Biome>> var0 = this.settings.structures(param0).values().stream().collect(Collectors.toUnmodifiableSet())::contains;
            List<ChunkPos> var1 = this.getRingPositionsFor(param1);
            int var2 = param1.distance();
            int var3 = param1.count();
            int var4 = param1.spread();
            Random var5 = new Random();
            var5.setSeed(this.seed);
            double var6 = var5.nextDouble() * Math.PI * 2.0;
            int var7 = 0;
            int var8 = 0;

            for(int var9 = 0; var9 < var3; ++var9) {
                double var10 = (double)(4 * var2 + var2 * var8 * 6) + (var5.nextDouble() - 0.5) * (double)var2 * 2.5;
                int var11 = (int)Math.round(Math.cos(var6) * var10);
                int var12 = (int)Math.round(Math.sin(var6) * var10);
                BlockPos var13 = this.biomeSource
                    .findBiomeHorizontal(
                        SectionPos.sectionToBlockCoord(var11, 8),
                        0,
                        SectionPos.sectionToBlockCoord(var12, 8),
                        112,
                        param1x -> param1x.is(var0),
                        var5,
                        this.climateSampler()
                    );
                if (var13 != null) {
                    var11 = SectionPos.blockToSectionCoord(var13.getX());
                    var12 = SectionPos.blockToSectionCoord(var13.getZ());
                }

                var1.add(new ChunkPos(var11, var12));
                var6 += (Math.PI * 2) / (double)var4;
                if (++var7 == var4) {
                    ++var8;
                    var7 = 0;
                    var4 += 2 * var4 / (var8 + 1);
                    var4 = Math.min(var4, var3 - var9);
                    var6 += var5.nextDouble() * Math.PI * 2.0;
                }
            }

        }
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
    public Holder<Biome> getNoiseBiome(int param0, int param1, int param2) {
        return this.getBiomeSource().getNoiseBiome(param0, param1, param2, this.climateSampler());
    }

    public abstract void applyCarvers(
        WorldGenRegion var1, long var2, BiomeManager var4, StructureFeatureManager var5, ChunkAccess var6, GenerationStep.Carving var7
    );

    @Nullable
    public BlockPos findNearestMapFeature(ServerLevel param0, StructureFeature<?> param1, BlockPos param2, int param3, boolean param4) {
        StructurePlacement var0 = this.settings.getConfig(param1);
        Collection<ResourceKey<Biome>> var1 = this.settings.structures(param1).values();
        if (var0 != null && !var1.isEmpty()) {
            Set<ResourceKey<Biome>> var2 = this.runtimeBiomeSource
                .possibleBiomes()
                .flatMap(param0x -> param0x.unwrapKey().stream())
                .collect(Collectors.toSet());
            if (var1.stream().noneMatch(var2::contains)) {
                return null;
            } else if (var0 instanceof ConcentricRingsStructurePlacement var3) {
                return this.getNearestGeneratedStructure(param2, var3);
            } else if (var0 instanceof RandomSpreadStructurePlacement var4) {
                return getNearestGeneratedStructure(param1, param0, param0.structureFeatureManager(), param2, param3, param4, param0.getSeed(), var4);
            } else {
                throw new IllegalStateException("Invalid structure placement type");
            }
        } else {
            return null;
        }
    }

    @Nullable
    private BlockPos getNearestGeneratedStructure(BlockPos param0, ConcentricRingsStructurePlacement param1) {
        List<ChunkPos> var0 = this.getRingPositionsFor(param1);
        BlockPos var1 = null;
        double var2 = Double.MAX_VALUE;
        BlockPos.MutableBlockPos var3 = new BlockPos.MutableBlockPos();

        for(ChunkPos var4 : var0) {
            var3.set(SectionPos.sectionToBlockCoord(var4.x, 8), 32, SectionPos.sectionToBlockCoord(var4.z, 8));
            double var5 = var3.distSqr(param0);
            if (var1 == null) {
                var1 = new BlockPos(var3);
                var2 = var5;
            } else if (var5 < var2) {
                var1 = new BlockPos(var3);
                var2 = var5;
            }
        }

        return var1;
    }

    @Nullable
    private static BlockPos getNearestGeneratedStructure(
        StructureFeature<?> param0,
        LevelReader param1,
        StructureFeatureManager param2,
        BlockPos param3,
        int param4,
        boolean param5,
        long param6,
        RandomSpreadStructurePlacement param7
    ) {
        int var0 = param7.spacing();
        int var1 = SectionPos.blockToSectionCoord(param3.getX());
        int var2 = SectionPos.blockToSectionCoord(param3.getZ());

        for(int var3 = 0; var3 <= param4; ++var3) {
            for(int var4 = -var3; var4 <= var3; ++var4) {
                boolean var5 = var4 == -var3 || var4 == var3;

                for(int var6 = -var3; var6 <= var3; ++var6) {
                    boolean var7 = var6 == -var3 || var6 == var3;
                    if (var5 || var7) {
                        int var8 = var1 + var0 * var4;
                        int var9 = var2 + var0 * var6;
                        ChunkPos var10 = param7.getPotentialFeatureChunk(param6, var8, var9);
                        StructureCheckResult var11 = param2.checkStructurePresence(var10, param0, param5);
                        if (var11 != StructureCheckResult.START_NOT_PRESENT) {
                            if (!param5 && var11 == StructureCheckResult.START_PRESENT) {
                                return StructureFeature.getLocatePos(param7, var10);
                            }

                            ChunkAccess var12 = param1.getChunk(var10.x, var10.z, ChunkStatus.STRUCTURE_STARTS);
                            StructureStart<?> var13 = param2.getStartForFeature(SectionPos.bottomOf(var12), param0, var12);
                            if (var13 != null && var13.isValid()) {
                                if (param5 && var13.canBeReferenced()) {
                                    param2.addReference(var13);
                                    return StructureFeature.getLocatePos(param7, var13.getChunkPos());
                                }

                                if (!param5) {
                                    return StructureFeature.getLocatePos(param7, var13.getChunkPos());
                                }
                            }

                            if (var3 == 0) {
                                break;
                            }
                        }
                    }
                }

                if (var3 == 0) {
                    break;
                }
            }
        }

        return null;
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
            WorldgenRandom var5 = new WorldgenRandom(new XoroshiroRandomSource(RandomSupport.seedUniquifier()));
            long var6 = var5.setDecorationSeed(param0.getSeed(), var2.getX(), var2.getZ());
            Set<Biome> var7 = new ObjectArraySet<>();
            if (this instanceof FlatLevelSource) {
                this.biomeSource.possibleBiomes().map(Holder::value).forEach(var7::add);
            } else {
                ChunkPos.rangeClosed(var1.chunk(), 1).forEach(param2x -> {
                    ChunkAccess var0x = param0.getChunk(param2x.x, param2x.z);

                    for(LevelChunkSection var1x : var0x.getSections()) {
                        var1x.getBiomes().getAll(param1x -> var7.add(param1x.value()));
                    }

                });
                var7.retainAll(this.biomeSource.possibleBiomes().map(Holder::value).collect(Collectors.toSet()));
            }

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
                            } catch (Exception var291) {
                                CrashReport var18 = CrashReport.forThrowable(var291, "Feature placement");
                                var18.addCategory("Feature").setDetail("Description", var16::get);
                                throw new ReportedException(var18);
                            }

                            ++var13;
                        }
                    }

                    if (var12 < var8) {
                        IntSet var19 = new IntArraySet();

                        for(Biome var20 : var7) {
                            List<HolderSet<PlacedFeature>> var21 = var20.getGenerationSettings().features();
                            if (var12 < var21.size()) {
                                HolderSet<PlacedFeature> var22 = var21.get(var12);
                                BiomeSource.StepFeatureData var23 = var4.get(var12);
                                var22.stream().map(Holder::value).forEach(param2x -> var19.add(var23.indexMapping().applyAsInt(param2x)));
                            }
                        }

                        int var24 = var19.size();
                        int[] var25 = var19.toIntArray();
                        Arrays.sort(var25);
                        BiomeSource.StepFeatureData var26 = var4.get(var12);

                        for(int var27 = 0; var27 < var24; ++var27) {
                            int var28 = var25[var27];
                            PlacedFeature var29 = var26.features().get(var28);
                            Supplier<String> var30 = () -> var9.getResourceKey(var29).map(Object::toString).orElseGet(var29::toString);
                            var5.setFeatureSeed(var6, var28, var12);

                            try {
                                param0.setCurrentlyGenerating(var30);
                                var29.placeWithBiomeCheck(param0, this, var5, var2);
                            } catch (Exception var301) {
                                CrashReport var32 = CrashReport.forThrowable(var301, "Feature placement");
                                var32.addCategory("Feature").setDetail("Description", var30::get);
                                throw new ReportedException(var32);
                            }
                        }
                    }
                }

                param0.setCurrentlyGenerating(null);
            } catch (Exception var31) {
                CrashReport var34 = CrashReport.forThrowable(var31, "Biome decoration");
                var34.addCategory("Generation").setDetail("CenterX", var0.x).setDetail("CenterZ", var0.z).setDetail("Seed", var6);
                throw new ReportedException(var34);
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

    public WeightedRandomList<MobSpawnSettings.SpawnerData> getMobsAt(Holder<Biome> param0, StructureFeatureManager param1, MobCategory param2, BlockPos param3) {
        return param0.value().getMobSettings().getMobs(param2);
    }

    public void createStructures(RegistryAccess param0, StructureFeatureManager param1, ChunkAccess param2, StructureManager param3, long param4) {
        ChunkPos var0 = param2.getPos();
        SectionPos var1 = SectionPos.bottomOf(param2);
        Registry<ConfiguredStructureFeature<?, ?>> var2 = param0.registryOrThrow(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY);

        label45:
        for(StructureFeature<?> var3 : Registry.STRUCTURE_FEATURE) {
            StructurePlacement var4 = this.settings.getConfig(var3);
            if (var4 != null) {
                StructureStart<?> var5 = param1.getStartForFeature(var1, var3, param2);
                if (var5 == null || !var5.isValid()) {
                    int var6 = fetchReferences(param1, param2, var1, var3);
                    if (var4.isFeatureChunk(this, var0.x, var0.z)) {
                        for(Entry<ResourceKey<ConfiguredStructureFeature<?, ?>>, Collection<ResourceKey<Biome>>> var7 : this.settings
                            .structures(var3)
                            .asMap()
                            .entrySet()) {
                            Optional<ConfiguredStructureFeature<?, ?>> var8 = var2.getOptional(var7.getKey());
                            if (!var8.isEmpty()) {
                                Predicate<ResourceKey<Biome>> var9 = Set.copyOf(var7.getValue())::contains;
                                StructureStart<?> var10 = var8.get()
                                    .generate(param0, this, this.biomeSource, param3, param4, var0, var6, param2, param1x -> this.adjustBiome(param1x).is(var9));
                                if (var10.isValid()) {
                                    param1.setStartForFeature(var1, var3, var10, param2);
                                    continue label45;
                                }
                            }
                        }
                    }

                    param1.setStartForFeature(var1, var3, StructureStart.INVALID_START, param2);
                }
            }
        }

    }

    private static int fetchReferences(StructureFeatureManager param0, ChunkAccess param1, SectionPos param2, StructureFeature<?> param3) {
        StructureStart<?> var0 = param0.getStartForFeature(param2, param3, param1);
        return var0 != null ? var0.getReferences() : 0;
    }

    protected Holder<Biome> adjustBiome(Holder<Biome> param0) {
        return param0;
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

    public List<ChunkPos> getRingPositionsFor(ConcentricRingsStructurePlacement param0) {
        return this.ringPositions.get(param0);
    }

    public long seed() {
        return this.seed;
    }

    static {
        Registry.register(Registry.CHUNK_GENERATOR, "noise", NoiseBasedChunkGenerator.CODEC);
        Registry.register(Registry.CHUNK_GENERATOR, "flat", FlatLevelSource.CODEC);
        Registry.register(Registry.CHUNK_GENERATOR, "debug", DebugLevelSource.CODEC);
    }
}
