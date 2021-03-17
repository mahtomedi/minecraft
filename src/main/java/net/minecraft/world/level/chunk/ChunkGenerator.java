package net.minecraft.world.level.chunk;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.util.BitSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.data.worldgen.StructureFeatures;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.DebugLevelSource;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.StructureSettings;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.StrongholdConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class ChunkGenerator {
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
                    if (var2.getGenerationSettings().isValidStart(StructureFeature.STRONGHOLD)) {
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
                        .findBiomeHorizontal(SectionPos.sectionToBlockCoord(var12, 8), 0, SectionPos.sectionToBlockCoord(var13, 8), 112, var1::contains, var6);
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

    protected abstract Codec<? extends ChunkGenerator> codec();

    @OnlyIn(Dist.CLIENT)
    public abstract ChunkGenerator withSeed(long var1);

    public void createBiomes(Registry<Biome> param0, ChunkAccess param1) {
        ChunkPos var0 = param1.getPos();
        ((ProtoChunk)param1).setBiomes(new ChunkBiomeContainer(param0, param1, var0, this.runtimeBiomeSource));
    }

    public void applyCarvers(long param0, BiomeManager param1, ChunkAccess param2, GenerationStep.Carving param3) {
        BiomeManager var0 = param1.withDifferentSource(this.biomeSource);
        WorldgenRandom var1 = new WorldgenRandom();
        int var2 = 8;
        ChunkPos var3 = param2.getPos();
        BiomeGenerationSettings var4 = this.biomeSource
            .getNoiseBiome(QuartPos.fromBlock(var3.getMinBlockX()), 0, QuartPos.fromBlock(var3.getMinBlockZ()))
            .getGenerationSettings();
        CarvingContext var5 = new CarvingContext(this);
        BitSet var6 = ((ProtoChunk)param2).getOrCreateCarvingMask(param3);

        for(int var7 = -8; var7 <= 8; ++var7) {
            for(int var8 = -8; var8 <= 8; ++var8) {
                ChunkPos var9 = new ChunkPos(var3.x + var7, var3.z + var8);
                List<Supplier<ConfiguredWorldCarver<?>>> var10 = var4.getCarvers(param3);
                ListIterator<Supplier<ConfiguredWorldCarver<?>>> var11 = var10.listIterator();

                while(var11.hasNext()) {
                    int var12 = var11.nextIndex();
                    ConfiguredWorldCarver<?> var13 = var11.next().get();
                    var1.setLargeFeatureSeed(param0 + (long)var12, var9.x, var9.z);
                    if (var13.isStartChunk(var1)) {
                        var13.carve(var5, param2, var0::getBiome, var1, this.getSeaLevel(), var9, var6);
                    }
                }
            }
        }

    }

    @Nullable
    public BlockPos findNearestMapFeature(ServerLevel param0, StructureFeature<?> param1, BlockPos param2, int param3, boolean param4) {
        if (!this.biomeSource.canGenerateStructure(param1)) {
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

    public void applyBiomeDecoration(WorldGenRegion param0, StructureFeatureManager param1) {
        ChunkPos var0 = param0.getCenter();
        int var1 = var0.getMinBlockX();
        int var2 = var0.getMinBlockZ();
        BlockPos var3 = new BlockPos(var1, param0.getMinBuildHeight(), var2);
        Biome var4 = this.biomeSource.getPrimaryBiome(var0);
        WorldgenRandom var5 = new WorldgenRandom();
        long var6 = var5.setDecorationSeed(param0.getSeed(), var1, var2);

        try {
            var4.generate(param1, this, param0, var6, var5, var3);
        } catch (Exception var13) {
            CrashReport var8 = CrashReport.forThrowable(var13, "Biome decoration");
            var8.addCategory("Generation").setDetail("CenterX", var0.x).setDetail("CenterZ", var0.z).setDetail("Seed", var6).setDetail("Biome", var4);
            throw new ReportedException(var8);
        }
    }

    public abstract void buildSurfaceAndBedrock(WorldGenRegion var1, ChunkAccess var2);

    public void spawnOriginalMobs(WorldGenRegion param0) {
    }

    public StructureSettings getSettings() {
        return this.settings;
    }

    public int getSpawnHeight(LevelHeightAccessor param0) {
        return 64;
    }

    public BiomeSource getBiomeSource() {
        return this.runtimeBiomeSource;
    }

    public int getGenDepth() {
        return 384;
    }

    public List<MobSpawnSettings.SpawnerData> getMobsAt(Biome param0, StructureFeatureManager param1, MobCategory param2, BlockPos param3) {
        return param0.getMobSettings().getMobs(param2);
    }

    public void createStructures(RegistryAccess param0, StructureFeatureManager param1, ChunkAccess param2, StructureManager param3, long param4) {
        Biome var0 = this.biomeSource.getPrimaryBiome(param2.getPos());
        this.createStructure(StructureFeatures.STRONGHOLD, param0, param1, param2, param3, param4, var0);

        for(Supplier<ConfiguredStructureFeature<?, ?>> var1 : var0.getGenerationSettings().structures()) {
            this.createStructure(var1.get(), param0, param1, param2, param3, param4, var0);
        }

    }

    private void createStructure(
        ConfiguredStructureFeature<?, ?> param0,
        RegistryAccess param1,
        StructureFeatureManager param2,
        ChunkAccess param3,
        StructureManager param4,
        long param5,
        Biome param6
    ) {
        ChunkPos var0 = param3.getPos();
        SectionPos var1 = SectionPos.bottomOf(param3);
        StructureStart<?> var2 = param2.getStartForFeature(var1, param0.feature, param3);
        int var3 = var2 != null ? var2.getReferences() : 0;
        StructureFeatureConfiguration var4 = this.settings.getConfig(param0.feature);
        if (var4 != null) {
            StructureStart<?> var5 = param0.generate(param1, this, this.biomeSource, param4, param5, var0, param6, var3, var4, param3);
            param2.setStartForFeature(var1, param0.feature, var5, param3);
        }

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
                        if (var10 != StructureStart.INVALID_START && var10.getBoundingBox().intersects(var4, var5, var4 + 15, var5 + 15)) {
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

    public int getSeaLevel() {
        return 63;
    }

    public int getMinY() {
        return 0;
    }

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
