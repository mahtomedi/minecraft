package net.minecraft.world.level.chunk;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
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
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.StructureSettings;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.StrongholdConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public abstract class ChunkGenerator implements BiomeManager.NoiseBiomeSource {
    public static final Codec<ChunkGenerator> CODEC = Registry.CHUNK_GENERATOR.dispatchStable(ChunkGenerator::codec, Function.identity());
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

    public abstract ChunkGenerator withSeed(long var1);

    public CompletableFuture<ChunkAccess> createBiomes(Executor param0, Registry<Biome> param1, StructureFeatureManager param2, ChunkAccess param3) {
        return CompletableFuture.supplyAsync(Util.wrapThreadWithTaskName("init_biomes", () -> {
            param3.fillBiomesFromNoise(this.runtimeBiomeSource, this.climateSampler());
            return param3;
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
        if (!this.canGenerateStructure(param0, param1)) {
            return null;
        } else if (param1 == StructureFeature.STRONGHOLD) {
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
            return var5 == null
                ? null
                : param1.getNearestGeneratedFeature(param0, param0.structureFeatureManager(), param2, param3, param4, param0.getSeed(), var5);
        }
    }

    private boolean canGenerateStructure(ServerLevel param0, StructureFeature<?> param1) {
        ImmutableMultimap<ConfiguredStructureFeature<?, ?>, ResourceKey<Biome>> var0 = this.settings.structures(param1);
        if (var0.isEmpty()) {
            return false;
        } else {
            Registry<Biome> var1 = param0.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY);
            Set<ResourceKey<Biome>> var2 = this.runtimeBiomeSource
                .possibleBiomes()
                .stream()
                .flatMap(param1x -> var1.getResourceKey(param1x).stream())
                .collect(Collectors.toSet());
            return var0.values().stream().anyMatch(var2::contains);
        }
    }

    public void applyBiomeDecoration(WorldGenLevel param0, ChunkPos param1, StructureFeatureManager param2) {
        int var0 = param1.x;
        int var1 = param1.z;
        int var2 = param1.getMinBlockX();
        int var3 = param1.getMinBlockZ();
        if (!SharedConstants.debugVoidTerrain(var2, var3)) {
            Registry<Biome> var4 = param0.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY);
            BlockPos var5 = new BlockPos(var2, param0.getMinBuildHeight(), var3);
            int var6 = SectionPos.blockToSectionCoord(var5.getX());
            int var7 = SectionPos.blockToSectionCoord(var5.getZ());
            int var8 = SectionPos.sectionToBlockCoord(var6);
            int var9 = SectionPos.sectionToBlockCoord(var7);
            int var10 = param0.getMinBuildHeight() + 1;
            int var11 = param0.getMaxBuildHeight() - 1;
            Map<Integer, List<StructureFeature<?>>> var12 = Registry.STRUCTURE_FEATURE
                .stream()
                .collect(Collectors.groupingBy(param0x -> param0x.step().ordinal()));
            ImmutableList<ImmutableList<ConfiguredFeature<?, ?>>> var13 = this.biomeSource.featuresPerStep();
            WorldgenRandom var14 = new WorldgenRandom();
            long var15 = var14.setDecorationSeed(param0.getSeed(), var2, var3);

            try {
                Registry<ConfiguredFeature<?, ?>> var16 = param0.registryAccess().registryOrThrow(Registry.CONFIGURED_FEATURE_REGISTRY);
                Registry<StructureFeature<?>> var17 = param0.registryAccess().registryOrThrow(Registry.STRUCTURE_FEATURE_REGISTRY);
                int var18 = Math.max(GenerationStep.Decoration.values().length, var13.size());

                for(int var19 = 0; var19 < var18; ++var19) {
                    int var20 = 0;
                    if (param2.shouldGenerateFeatures()) {
                        for(StructureFeature<?> var22 : var12.getOrDefault(var19, Collections.emptyList())) {
                            var14.setFeatureSeed(var15, var20, var19);
                            Supplier<String> var23 = () -> var17.getResourceKey(var22).map(Object::toString).orElseGet(var22::toString);

                            try {
                                ImmutableMultimap<ConfiguredStructureFeature<?, ?>, ResourceKey<Biome>> var24 = this.settings.structures(var22);
                                param0.setCurrentlyGenerating(var23);
                                Predicate<Biome> var25 = param2x -> this.validBiome(var4, var24::containsValue, param2x);
                                param2.startsForFeature(SectionPos.of(var5), var22)
                                    .forEach(
                                        param10 -> param10.placeInChunk(
                                                param0,
                                                param2,
                                                this,
                                                var14,
                                                var25,
                                                new BoundingBox(var8, var10, var9, var8 + 15, var11, var9 + 15),
                                                new ChunkPos(var6, var7)
                                            )
                                    );
                            } catch (Exception var321) {
                                CrashReport var27 = CrashReport.forThrowable(var321, "Feature placement");
                                var27.addCategory("Feature").setDetail("Description", var23::get);
                                throw new ReportedException(var27);
                            }

                            ++var20;
                        }
                    }

                    if (var13.size() > var19) {
                        for(ConfiguredFeature<?, ?> var28 : var13.get(var19)) {
                            Supplier<String> var29 = () -> var16.getResourceKey(var28).map(Object::toString).orElseGet(var28::toString);
                            var14.setFeatureSeed(var15, var20, var19);

                            try {
                                param0.setCurrentlyGenerating(var29);
                                var28.placeWithBiomeCheck(Optional.of(var28), param0, this, var14, var5);
                            } catch (Exception var331) {
                                CrashReport var31 = CrashReport.forThrowable(var331, "Feature placement");
                                var31.addCategory("Feature").setDetail("Description", var29::get);
                                throw new ReportedException(var31);
                            }

                            ++var20;
                        }
                    }
                }

                param0.setCurrentlyGenerating(null);
            } catch (Exception var34) {
                CrashReport var33 = CrashReport.forThrowable(var34, "Biome decoration");
                var33.addCategory("Generation").setDetail("CenterX", var0).setDetail("CenterZ", var1).setDetail("Seed", var15);
                throw new ReportedException(var33);
            }
        }
    }

    public abstract void buildSurfaceAndBedrock(WorldGenRegion var1, StructureFeatureManager var2, ChunkAccess var3);

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
            StructureStart<?> var3 = StructureFeatures.STRONGHOLD
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
            param1.setStartForFeature(var1, StructureFeature.STRONGHOLD, var3, param2);
        }

        Registry<Biome> var4 = param0.registryOrThrow(Registry.BIOME_REGISTRY);

        label33:
        for(StructureFeature<?> var5 : Registry.STRUCTURE_FEATURE) {
            StructureFeatureConfiguration var6 = this.settings.getConfig(var5);
            if (var6 != null) {
                int var7 = fetchReferences(param1, param2, var1, var5);

                for(Entry<ConfiguredStructureFeature<?, ?>, Collection<ResourceKey<Biome>>> var8 : this.settings.structures(var5).asMap().entrySet()) {
                    StructureStart<?> var9 = var8.getKey()
                        .generate(
                            param0,
                            this,
                            this.biomeSource,
                            param3,
                            param4,
                            var0,
                            var7,
                            var6,
                            param2,
                            param2x -> this.validBiome(var4, var8.getValue()::contains, param2x)
                        );
                    if (var9.isValid()) {
                        param1.setStartForFeature(var1, var5, var9, param2);
                        continue label33;
                    }
                }

                param1.setStartForFeature(var1, var5, StructureStart.INVALID_START, param2);
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

    public abstract CompletableFuture<ChunkAccess> fillFromNoise(Executor var1, StructureFeatureManager var2, ChunkAccess var3);

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
