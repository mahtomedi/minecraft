package net.minecraft.world.level.chunk;

import com.mojang.datafixers.Products.P1;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import java.util.ArrayList;
import java.util.Arrays;
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
import java.util.stream.Stream;
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
import net.minecraft.resources.RegistryOps;
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
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureCheckResult;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.placement.ConcentricRingsStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import org.apache.commons.lang3.mutable.MutableBoolean;

public abstract class ChunkGenerator implements BiomeManager.NoiseBiomeSource {
    public static final Codec<ChunkGenerator> CODEC = Registry.CHUNK_GENERATOR.byNameCodec().dispatchStable(ChunkGenerator::codec, Function.identity());
    protected final Registry<StructureSet> structureSets;
    protected final BiomeSource biomeSource;
    protected final BiomeSource runtimeBiomeSource;
    protected final Optional<HolderSet<StructureSet>> structureOverrides;
    private final Map<ConfiguredStructureFeature<?, ?>, List<StructurePlacement>> placementsForFeature;
    private final Map<ConcentricRingsStructurePlacement, ArrayList<ChunkPos>> ringPositions;
    private boolean hasGeneratedPositions;
    private final long seed;

    protected static final <T extends ChunkGenerator> P1<Mu<T>, Registry<StructureSet>> commonCodec(Instance<T> param0) {
        return param0.group(RegistryOps.retrieveRegistry(Registry.STRUCTURE_SET_REGISTRY).forGetter(param0x -> param0x.structureSets));
    }

    public ChunkGenerator(Registry<StructureSet> param0, Optional<HolderSet<StructureSet>> param1, BiomeSource param2) {
        this(param0, param1, param2, param2, 0L);
    }

    public ChunkGenerator(Registry<StructureSet> param0, Optional<HolderSet<StructureSet>> param1, BiomeSource param2, BiomeSource param3, long param4) {
        this.structureSets = param0;
        this.biomeSource = param2;
        this.runtimeBiomeSource = param3;
        this.structureOverrides = param1;
        this.seed = param4;
        this.placementsForFeature = new Object2ObjectOpenHashMap<>();
        this.ringPositions = new Object2ObjectArrayMap<>();
    }

    public Stream<Holder<StructureSet>> possibleStructureSets() {
        return this.structureOverrides.isPresent() ? this.structureOverrides.get().stream() : this.structureSets.holders().map(Holder::hackyErase);
    }

    private void generatePositions() {
        Set<Holder<Biome>> var0 = this.runtimeBiomeSource.possibleBiomes();
        this.possibleStructureSets()
            .forEach(
                param1 -> {
                    StructureSet var0x = param1.value();
        
                    for(StructureSet.StructureSelectionEntry var1 : var0x.structures()) {
                        this.placementsForFeature.computeIfAbsent(var1.structure().value(), param0x -> new ArrayList()).add(var0x.placement());
                    }
        
                    StructurePlacement var2 = var0x.placement();
                    if (var2 instanceof ConcentricRingsStructurePlacement var3
                        && var0x.structures().stream().anyMatch(param1x -> param1x.generatesInMatchingBiome(var0::contains))) {
                        this.generateRingPositions(var0x, var3);
                    }
        
                }
            );
    }

    private void generateRingPositions(StructureSet param0, ConcentricRingsStructurePlacement param1) {
        ArrayList<ChunkPos> var0 = new ArrayList<>();
        this.ringPositions.put(param1, var0);
        if (param1.count() != 0) {
            Set<Holder<Biome>> var1 = param0.structures()
                .stream()
                .flatMap(param0x -> param0x.structure().value().biomes().stream())
                .collect(Collectors.toSet());
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
                Pair<BlockPos, Holder<Biome>> var13 = this.biomeSource
                    .findBiomeHorizontal(
                        SectionPos.sectionToBlockCoord(var11, 8), 0, SectionPos.sectionToBlockCoord(var12, 8), 112, var1::contains, var5, this.climateSampler()
                    );
                if (var13 != null) {
                    BlockPos var14 = var13.getFirst();
                    var11 = SectionPos.blockToSectionCoord(var14.getX());
                    var12 = SectionPos.blockToSectionCoord(var14.getZ());
                }

                var0.add(new ChunkPos(var11, var12));
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
    public Pair<BlockPos, Holder<ConfiguredStructureFeature<?, ?>>> findNearestMapFeature(
        ServerLevel param0, HolderSet<ConfiguredStructureFeature<?, ?>> param1, BlockPos param2, int param3, boolean param4
    ) {
        Set<Holder<Biome>> var0 = param1.stream().flatMap(param0x -> param0x.value().biomes().stream()).collect(Collectors.toSet());
        if (var0.isEmpty()) {
            return null;
        } else {
            Set<Holder<Biome>> var1 = this.runtimeBiomeSource.possibleBiomes();
            if (Collections.disjoint(var1, var0)) {
                return null;
            } else {
                Pair<BlockPos, Holder<ConfiguredStructureFeature<?, ?>>> var2 = null;
                double var3 = Double.MAX_VALUE;
                Map<StructurePlacement, Set<Holder<ConfiguredStructureFeature<?, ?>>>> var4 = new Object2ObjectArrayMap<>();

                for(Holder<ConfiguredStructureFeature<?, ?>> var5 : param1) {
                    if (!var1.stream().noneMatch(var5.value().biomes()::contains)) {
                        for(StructurePlacement var6 : this.getPlacementsForFeature(var5)) {
                            var4.computeIfAbsent(var6, param0x -> new ObjectArraySet()).add(var5);
                        }
                    }
                }

                List<Entry<StructurePlacement, Set<Holder<ConfiguredStructureFeature<?, ?>>>>> var7 = new ArrayList<>(var4.size());

                for(Entry<StructurePlacement, Set<Holder<ConfiguredStructureFeature<?, ?>>>> var8 : var4.entrySet()) {
                    StructurePlacement var9 = var8.getKey();
                    if (var9 instanceof ConcentricRingsStructurePlacement var10) {
                        BlockPos var11 = this.getNearestGeneratedStructure(param2, var10);
                        double var12 = param2.distSqr(var11);
                        if (var12 < var3) {
                            var3 = var12;
                            var2 = Pair.of(var11, var8.getValue().iterator().next());
                        }
                    } else if (var9 instanceof RandomSpreadStructurePlacement) {
                        var7.add(var8);
                    }
                }

                if (!var7.isEmpty()) {
                    int var13 = SectionPos.blockToSectionCoord(param2.getX());
                    int var14 = SectionPos.blockToSectionCoord(param2.getZ());

                    for(int var15 = 0; var15 <= param3; ++var15) {
                        boolean var16 = false;

                        for(Entry<StructurePlacement, Set<Holder<ConfiguredStructureFeature<?, ?>>>> var17 : var7) {
                            RandomSpreadStructurePlacement var18 = (RandomSpreadStructurePlacement)var17.getKey();
                            Pair<BlockPos, Holder<ConfiguredStructureFeature<?, ?>>> var19 = getNearestGeneratedStructure(
                                var17.getValue(), param0, param0.structureFeatureManager(), var13, var14, var15, param4, param0.getSeed(), var18
                            );
                            if (var19 != null) {
                                var16 = true;
                                double var20 = param2.distSqr(var19.getFirst());
                                if (var20 < var3) {
                                    var3 = var20;
                                    var2 = var19;
                                }
                            }
                        }

                        if (var16) {
                            return var2;
                        }
                    }
                }

                return var2;
            }
        }
    }

    @Nullable
    private BlockPos getNearestGeneratedStructure(BlockPos param0, ConcentricRingsStructurePlacement param1) {
        List<ChunkPos> var0 = this.getRingPositionsFor(param1);
        if (var0 == null) {
            throw new IllegalStateException("Somehow tried to find structures for a placement that doesn't exist");
        } else {
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
    }

    @Nullable
    private static Pair<BlockPos, Holder<ConfiguredStructureFeature<?, ?>>> getNearestGeneratedStructure(
        Set<Holder<ConfiguredStructureFeature<?, ?>>> param0,
        LevelReader param1,
        StructureFeatureManager param2,
        int param3,
        int param4,
        int param5,
        boolean param6,
        long param7,
        RandomSpreadStructurePlacement param8
    ) {
        int var0 = param8.spacing();

        for(int var1 = -param5; var1 <= param5; ++var1) {
            boolean var2 = var1 == -param5 || var1 == param5;

            for(int var3 = -param5; var3 <= param5; ++var3) {
                boolean var4 = var3 == -param5 || var3 == param5;
                if (var2 || var4) {
                    int var5 = param3 + var0 * var1;
                    int var6 = param4 + var0 * var3;
                    ChunkPos var7 = param8.getPotentialFeatureChunk(param7, var5, var6);

                    for(Holder<ConfiguredStructureFeature<?, ?>> var8 : param0) {
                        StructureCheckResult var9 = param2.checkStructurePresence(var7, var8.value(), param6);
                        if (var9 != StructureCheckResult.START_NOT_PRESENT) {
                            if (!param6 && var9 == StructureCheckResult.START_PRESENT) {
                                return Pair.of(StructureFeature.getLocatePos(param8, var7), var8);
                            }

                            ChunkAccess var10 = param1.getChunk(var7.x, var7.z, ChunkStatus.STRUCTURE_STARTS);
                            StructureStart var11 = param2.getStartForFeature(SectionPos.bottomOf(var10), var8.value(), var10);
                            if (var11 != null && var11.isValid()) {
                                if (param6 && var11.canBeReferenced()) {
                                    param2.addReference(var11);
                                    return Pair.of(StructureFeature.getLocatePos(param8, var11.getChunkPos()), var8);
                                }

                                if (!param6) {
                                    return Pair.of(StructureFeature.getLocatePos(param8, var11.getChunkPos()), var8);
                                }
                            }
                        }
                    }
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
            Registry<ConfiguredStructureFeature<?, ?>> var3 = param0.registryAccess().registryOrThrow(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY);
            Map<Integer, List<ConfiguredStructureFeature<?, ?>>> var4 = var3.stream()
                .collect(Collectors.groupingBy(param0x -> param0x.feature.step().ordinal()));
            List<BiomeSource.StepFeatureData> var5 = this.biomeSource.featuresPerStep();
            WorldgenRandom var6 = new WorldgenRandom(new XoroshiroRandomSource(RandomSupport.seedUniquifier()));
            long var7 = var6.setDecorationSeed(param0.getSeed(), var2.getX(), var2.getZ());
            Set<Biome> var8 = new ObjectArraySet<>();
            if (this instanceof FlatLevelSource) {
                this.biomeSource.possibleBiomes().stream().map(Holder::value).forEach(var8::add);
            } else {
                ChunkPos.rangeClosed(var1.chunk(), 1).forEach(param2x -> {
                    ChunkAccess var0x = param0.getChunk(param2x.x, param2x.z);

                    for(LevelChunkSection var1x : var0x.getSections()) {
                        var1x.getBiomes().getAll(param1x -> var8.add(param1x.value()));
                    }

                });
                var8.retainAll(this.biomeSource.possibleBiomes().stream().map(Holder::value).collect(Collectors.toSet()));
            }

            int var9 = var5.size();

            try {
                Registry<PlacedFeature> var10 = param0.registryAccess().registryOrThrow(Registry.PLACED_FEATURE_REGISTRY);
                int var11 = Math.max(GenerationStep.Decoration.values().length, var9);

                for(int var12 = 0; var12 < var11; ++var12) {
                    int var13 = 0;
                    if (param2.shouldGenerateFeatures()) {
                        for(ConfiguredStructureFeature<?, ?> var15 : var4.getOrDefault(var12, Collections.emptyList())) {
                            var6.setFeatureSeed(var7, var13, var12);
                            Supplier<String> var16 = () -> var3.getResourceKey(var15).map(Object::toString).orElseGet(var15::toString);

                            try {
                                param0.setCurrentlyGenerating(var16);
                                param2.startsForFeature(var1, var15)
                                    .forEach(param5 -> param5.placeInChunk(param0, param2, this, var6, getWritableArea(param1), var0));
                            } catch (Exception var291) {
                                CrashReport var18 = CrashReport.forThrowable(var291, "Feature placement");
                                var18.addCategory("Feature").setDetail("Description", var16::get);
                                throw new ReportedException(var18);
                            }

                            ++var13;
                        }
                    }

                    if (var12 < var9) {
                        IntSet var19 = new IntArraySet();

                        for(Biome var20 : var8) {
                            List<HolderSet<PlacedFeature>> var21 = var20.getGenerationSettings().features();
                            if (var12 < var21.size()) {
                                HolderSet<PlacedFeature> var22 = var21.get(var12);
                                BiomeSource.StepFeatureData var23 = var5.get(var12);
                                var22.stream().map(Holder::value).forEach(param2x -> var19.add(var23.indexMapping().applyAsInt(param2x)));
                            }
                        }

                        int var24 = var19.size();
                        int[] var25 = var19.toIntArray();
                        Arrays.sort(var25);
                        BiomeSource.StepFeatureData var26 = var5.get(var12);

                        for(int var27 = 0; var27 < var24; ++var27) {
                            int var28 = var25[var27];
                            PlacedFeature var29 = var26.features().get(var28);
                            Supplier<String> var30 = () -> var10.getResourceKey(var29).map(Object::toString).orElseGet(var29::toString);
                            var6.setFeatureSeed(var7, var28, var12);

                            try {
                                param0.setCurrentlyGenerating(var30);
                                var29.placeWithBiomeCheck(param0, this, var6, var2);
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
                var34.addCategory("Generation").setDetail("CenterX", var0.x).setDetail("CenterZ", var0.z).setDetail("Seed", var7);
                throw new ReportedException(var34);
            }
        }
    }

    public boolean hasFeatureChunkInRange(ResourceKey<StructureSet> param0, int param1, int param2, int param3) {
        StructureSet var0 = this.structureSets.get(param0);
        if (var0 == null) {
            return false;
        } else {
            StructurePlacement var1 = var0.placement();

            for(int var2 = param1 - param3; var2 <= param1 + param3; ++var2) {
                for(int var3 = param2 - param3; var3 <= param2 + param3; ++var3) {
                    if (var1.isFeatureChunk(this, var2, var3)) {
                        return true;
                    }
                }
            }

            return false;
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

    public int getSpawnHeight(LevelHeightAccessor param0) {
        return 64;
    }

    public BiomeSource getBiomeSource() {
        return this.runtimeBiomeSource;
    }

    public abstract int getGenDepth();

    public WeightedRandomList<MobSpawnSettings.SpawnerData> getMobsAt(Holder<Biome> param0, StructureFeatureManager param1, MobCategory param2, BlockPos param3) {
        Map<ConfiguredStructureFeature<?, ?>, LongSet> var0 = param1.getAllStructuresAt(param3);

        for(Entry<ConfiguredStructureFeature<?, ?>, LongSet> var1 : var0.entrySet()) {
            ConfiguredStructureFeature<?, ?> var2 = var1.getKey();
            StructureSpawnOverride var3 = var2.spawnOverrides.get(param2);
            if (var3 != null) {
                MutableBoolean var4 = new MutableBoolean(false);
                Predicate<StructureStart> var5 = var3.boundingBox() == StructureSpawnOverride.BoundingBoxType.PIECE
                    ? param2x -> param1.structureHasPieceAt(param3, param2x)
                    : param1x -> param1x.getBoundingBox().isInside(param3);
                param1.fillStartsForFeature(var2, var1.getValue(), param2x -> {
                    if (var4.isFalse() && var5.test(param2x)) {
                        var4.setTrue();
                    }

                });
                if (var4.isTrue()) {
                    return var3.spawns();
                }
            }
        }

        return param0.value().getMobSettings().getMobs(param2);
    }

    public static Stream<ConfiguredStructureFeature<?, ?>> allConfigurations(Registry<ConfiguredStructureFeature<?, ?>> param0, StructureFeature<?> param1) {
        return param0.stream().filter(param1x -> param1x.feature == param1);
    }

    public void createStructures(RegistryAccess param0, StructureFeatureManager param1, ChunkAccess param2, StructureManager param3, long param4) {
        ChunkPos var0 = param2.getPos();
        SectionPos var1 = SectionPos.bottomOf(param2);
        this.possibleStructureSets().forEach(param7 -> {
            StructurePlacement var0x = param7.value().placement();
            List<StructureSet.StructureSelectionEntry> var1x = param7.value().structures();

            for(StructureSet.StructureSelectionEntry var5x : var1x) {
                StructureStart var3 = param1.getStartForFeature(var1, var5x.structure().value(), param2);
                if (var3 != null && var3.isValid()) {
                    return;
                }
            }

            if (var0x.isFeatureChunk(this, var0.x, var0.z)) {
                if (var1x.size() == 1) {
                    this.tryGenerateStructure(var1x.get(0), param1, param0, param3, param4, param2, var0, var1);
                } else {
                    ArrayList<StructureSet.StructureSelectionEntry> var4 = new ArrayList<>(var1x.size());
                    var4.addAll(var1x);
                    WorldgenRandom var5 = new WorldgenRandom(new LegacyRandomSource(0L));
                    var5.setLargeFeatureSeed(param4, var0.x, var0.z);
                    int var6 = 0;

                    for(StructureSet.StructureSelectionEntry var7 : var4) {
                        var6 += var7.weight();
                    }

                    while(!var4.isEmpty()) {
                        int var8 = var5.nextInt(var6);
                        int var9 = 0;

                        for(StructureSet.StructureSelectionEntry var10 : var4) {
                            var8 -= var10.weight();
                            if (var8 < 0) {
                                break;
                            }

                            ++var9;
                        }

                        StructureSet.StructureSelectionEntry var11 = var4.get(var9);
                        if (this.tryGenerateStructure(var11, param1, param0, param3, param4, param2, var0, var1)) {
                            return;
                        }

                        var4.remove(var9);
                        var6 -= var11.weight();
                    }

                }
            }
        });
    }

    private boolean tryGenerateStructure(
        StructureSet.StructureSelectionEntry param0,
        StructureFeatureManager param1,
        RegistryAccess param2,
        StructureManager param3,
        long param4,
        ChunkAccess param5,
        ChunkPos param6,
        SectionPos param7
    ) {
        ConfiguredStructureFeature<?, ?> var0 = param0.structure().value();
        int var1 = fetchReferences(param1, param5, param7, var0);
        HolderSet<Biome> var2 = var0.biomes();
        Predicate<Holder<Biome>> var3 = param1x -> var2.contains(this.adjustBiome(param1x));
        StructureStart var4 = var0.generate(param2, this, this.biomeSource, param3, param4, param6, var1, param5, var3);
        if (var4.isValid()) {
            param1.setStartForFeature(param7, var0, var4, param5);
            return true;
        } else {
            return false;
        }
    }

    private static int fetchReferences(StructureFeatureManager param0, ChunkAccess param1, SectionPos param2, ConfiguredStructureFeature<?, ?> param3) {
        StructureStart var0 = param0.getStartForFeature(param2, param3, param1);
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

                for(StructureStart var10 : param0.getChunk(var7, var8).getAllStarts().values()) {
                    try {
                        if (var10.isValid() && var10.getBoundingBox().intersects(var4, var5, var4 + 15, var5 + 15)) {
                            param1.addReferenceForFeature(var6, var10.getFeature(), var9, param2);
                            DebugPackets.sendStructurePacket(param0, var10);
                        }
                    } catch (Exception var21) {
                        CrashReport var12 = CrashReport.forThrowable(var21, "Generating structure reference");
                        CrashReportCategory var13 = var12.addCategory("Structure");
                        Optional<? extends Registry<ConfiguredStructureFeature<?, ?>>> var14 = param0.registryAccess()
                            .registry(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY);
                        var13.setDetail("Id", () -> var14.<String>map(param1x -> param1x.getKey(var10.getFeature()).toString()).orElse("UNKNOWN"));
                        var13.setDetail("Name", () -> Registry.STRUCTURE_FEATURE.getKey(var10.getFeature().feature).toString());
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

    private void ensureGenerated() {
        if (!this.hasGeneratedPositions) {
            this.generatePositions();
            this.hasGeneratedPositions = true;
        }

    }

    @Nullable
    public List<ChunkPos> getRingPositionsFor(ConcentricRingsStructurePlacement param0) {
        this.ensureGenerated();
        return this.ringPositions.get(param0);
    }

    private List<StructurePlacement> getPlacementsForFeature(Holder<ConfiguredStructureFeature<?, ?>> param0) {
        this.ensureGenerated();
        return this.placementsForFeature.getOrDefault(param0.value(), List.of());
    }

    public long seed() {
        return this.seed;
    }

    public abstract void addDebugScreenInfo(List<String> var1, BlockPos var2);

    static {
        Registry.register(Registry.CHUNK_GENERATOR, "noise", NoiseBasedChunkGenerator.CODEC);
        Registry.register(Registry.CHUNK_GENERATOR, "flat", FlatLevelSource.CODEC);
        Registry.register(Registry.CHUNK_GENERATOR, "debug", DebugLevelSource.CODEC);
    }
}
