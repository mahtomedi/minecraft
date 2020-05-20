package net.minecraft.world.level.chunk;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.util.BitSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.Map.Entry;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeDefaultFeatures;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.levelgen.DebugLevelSource;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.StructureSettings;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.StrongholdConfiguration;
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
                    if (var2.isValidStart(StructureFeature.STRONGHOLD)) {
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
                    BlockPos var14 = this.biomeSource.findBiomeHorizontal((var12 << 4) + 8, 0, (var13 << 4) + 8, 112, var1, var6);
                    if (var14 != null) {
                        var12 = var14.getX() >> 4;
                        var13 = var14.getZ() >> 4;
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

    public void createBiomes(ChunkAccess param0) {
        ChunkPos var0 = param0.getPos();
        ((ProtoChunk)param0).setBiomes(new ChunkBiomeContainer(var0, this.runtimeBiomeSource));
    }

    public void applyCarvers(long param0, BiomeManager param1, ChunkAccess param2, GenerationStep.Carving param3) {
        BiomeManager var0 = param1.withDifferentSource(this.biomeSource);
        WorldgenRandom var1 = new WorldgenRandom();
        int var2 = 8;
        ChunkPos var3 = param2.getPos();
        int var4 = var3.x;
        int var5 = var3.z;
        Biome var6 = this.biomeSource.getNoiseBiome(var3.x << 2, 0, var3.z << 2);
        BitSet var7 = ((ProtoChunk)param2).getOrCreateCarvingMask(param3);

        for(int var8 = var4 - 8; var8 <= var4 + 8; ++var8) {
            for(int var9 = var5 - 8; var9 <= var5 + 8; ++var9) {
                List<ConfiguredWorldCarver<?>> var10 = var6.getCarvers(param3);
                ListIterator<ConfiguredWorldCarver<?>> var11 = var10.listIterator();

                while(var11.hasNext()) {
                    int var12 = var11.nextIndex();
                    ConfiguredWorldCarver<?> var13 = var11.next();
                    var1.setLargeFeatureSeed(param0 + (long)var12, var8, var9);
                    if (var13.isStartChunk(var1, var8, var9)) {
                        var13.carve(param2, var0::getBiome, var1, this.getSeaLevel(), var8, var9, var4, var5, var7);
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
                var2.set((var3.x << 4) + 8, 32, (var3.z << 4) + 8);
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
            return param1.getNearestGeneratedFeature(
                param0, param0.structureFeatureManager(), param2, param3, param4, param0.getSeed(), this.settings.getConfig(param1)
            );
        }
    }

    public void applyBiomeDecoration(WorldGenRegion param0, StructureFeatureManager param1) {
        int var0 = param0.getCenterX();
        int var1 = param0.getCenterZ();
        int var2 = var0 * 16;
        int var3 = var1 * 16;
        BlockPos var4 = new BlockPos(var2, 0, var3);
        Biome var5 = this.biomeSource.getNoiseBiome((var0 << 2) + 2, 2, (var1 << 2) + 2);
        WorldgenRandom var6 = new WorldgenRandom();
        long var7 = var6.setDecorationSeed(param0.getSeed(), var2, var3);

        for(GenerationStep.Decoration var8 : GenerationStep.Decoration.values()) {
            try {
                var5.generate(var8, param1, this, param0, var7, var6, var4);
            } catch (Exception var18) {
                CrashReport var10 = CrashReport.forThrowable(var18, "Biome decoration");
                var10.addCategory("Generation")
                    .setDetail("CenterX", var0)
                    .setDetail("CenterZ", var1)
                    .setDetail("Step", var8)
                    .setDetail("Seed", var7)
                    .setDetail("Biome", Registry.BIOME.getKey(var5));
                throw new ReportedException(var10);
            }
        }

    }

    public abstract void buildSurfaceAndBedrock(WorldGenRegion var1, ChunkAccess var2);

    public void spawnOriginalMobs(WorldGenRegion param0) {
    }

    public StructureSettings getSettings() {
        return this.settings;
    }

    public int getSpawnHeight() {
        return 64;
    }

    public BiomeSource getBiomeSource() {
        return this.runtimeBiomeSource;
    }

    public int getGenDepth() {
        return 256;
    }

    public List<Biome.SpawnerData> getMobsAt(Biome param0, StructureFeatureManager param1, MobCategory param2, BlockPos param3) {
        return param0.getMobs(param2);
    }

    public void createStructures(StructureFeatureManager param0, ChunkAccess param1, StructureManager param2, long param3) {
        ChunkPos var0 = param1.getPos();
        Biome var1 = this.biomeSource.getNoiseBiome((var0.x << 2) + 2, 0, (var0.z << 2) + 2);
        this.createStructure(BiomeDefaultFeatures.STRONGHOLD, param0, param1, param2, param3, var0, var1);

        for(ConfiguredStructureFeature<?, ?> var2 : var1.structures()) {
            this.createStructure(var2, param0, param1, param2, param3, var0, var1);
        }

    }

    private void createStructure(
        ConfiguredStructureFeature<?, ?> param0,
        StructureFeatureManager param1,
        ChunkAccess param2,
        StructureManager param3,
        long param4,
        ChunkPos param5,
        Biome param6
    ) {
        StructureStart<?> var0 = param1.getStartForFeature(SectionPos.of(param2.getPos(), 0), param0.feature, param2);
        int var1 = var0 != null ? var0.getReferences() : 0;
        StructureStart<?> var2 = param0.generate(this, this.biomeSource, param3, param4, param5, param6, var1, this.settings.getConfig(param0.feature));
        param1.setStartForFeature(SectionPos.of(param2.getPos(), 0), param0.feature, var2, param2);
    }

    public void createReferences(LevelAccessor param0, StructureFeatureManager param1, ChunkAccess param2) {
        int var0 = 8;
        int var1 = param2.getPos().x;
        int var2 = param2.getPos().z;
        int var3 = var1 << 4;
        int var4 = var2 << 4;
        SectionPos var5 = SectionPos.of(param2.getPos(), 0);

        for(int var6 = var1 - 8; var6 <= var1 + 8; ++var6) {
            for(int var7 = var2 - 8; var7 <= var2 + 8; ++var7) {
                long var8 = ChunkPos.asLong(var6, var7);

                for(Entry<String, StructureStart<?>> var9 : param0.getChunk(var6, var7).getAllStarts().entrySet()) {
                    StructureStart<?> var10 = var9.getValue();
                    if (var10 != StructureStart.INVALID_START && var10.getBoundingBox().intersects(var3, var4, var3 + 15, var4 + 15)) {
                        param1.addReferenceForFeature(var5, var10.getFeature(), var8, param2);
                        DebugPackets.sendStructurePacket(param0, var10);
                    }
                }
            }
        }

    }

    public abstract void fillFromNoise(LevelAccessor var1, StructureFeatureManager var2, ChunkAccess var3);

    public int getSeaLevel() {
        return 63;
    }

    public abstract int getBaseHeight(int var1, int var2, Heightmap.Types var3);

    public abstract BlockGetter getBaseColumn(int var1, int var2);

    public int getFirstFreeHeight(int param0, int param1, Heightmap.Types param2) {
        return this.getBaseHeight(param0, param1, param2);
    }

    public int getFirstOccupiedHeight(int param0, int param1, Heightmap.Types param2) {
        return this.getBaseHeight(param0, param1, param2) - 1;
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
