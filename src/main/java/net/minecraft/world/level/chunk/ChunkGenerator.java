package net.minecraft.world.level.chunk;

import java.util.BitSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public abstract class ChunkGenerator<C extends ChunkGeneratorSettings> {
    protected final LevelAccessor level;
    protected final long seed;
    protected final BiomeSource biomeSource;
    protected final C settings;

    public ChunkGenerator(LevelAccessor param0, BiomeSource param1, C param2) {
        this.level = param0;
        this.seed = param0.getSeed();
        this.biomeSource = param1;
        this.settings = param2;
    }

    public void createBiomes(ChunkAccess param0) {
        ChunkPos var0 = param0.getPos();
        int var1 = var0.x;
        int var2 = var0.z;
        Biome[] var3 = this.biomeSource.getBiomeBlock(var1 * 16, var2 * 16, 16, 16);
        param0.setBiomes(var3);
    }

    protected Biome getCarvingBiome(ChunkAccess param0) {
        return param0.getBiome(BlockPos.ZERO);
    }

    protected Biome getDecorationBiome(WorldGenRegion param0, BlockPos param1) {
        return this.biomeSource.getBiome(param1);
    }

    public void applyCarvers(ChunkAccess param0, GenerationStep.Carving param1) {
        WorldgenRandom var0 = new WorldgenRandom();
        int var1 = 8;
        ChunkPos var2 = param0.getPos();
        int var3 = var2.x;
        int var4 = var2.z;
        BitSet var5 = param0.getCarvingMask(param1);

        for(int var6 = var3 - 8; var6 <= var3 + 8; ++var6) {
            for(int var7 = var4 - 8; var7 <= var4 + 8; ++var7) {
                List<ConfiguredWorldCarver<?>> var8 = this.getCarvingBiome(param0).getCarvers(param1);
                ListIterator<ConfiguredWorldCarver<?>> var9 = var8.listIterator();

                while(var9.hasNext()) {
                    int var10 = var9.nextIndex();
                    ConfiguredWorldCarver<?> var11 = var9.next();
                    var0.setLargeFeatureSeed(this.seed + (long)var10, var6, var7);
                    if (var11.isStartChunk(var0, var6, var7)) {
                        var11.carve(param0, var0, this.getSeaLevel(), var6, var7, var3, var4, var5);
                    }
                }
            }
        }

    }

    @Nullable
    public BlockPos findNearestMapFeature(Level param0, String param1, BlockPos param2, int param3, boolean param4) {
        StructureFeature<?> var0 = Feature.STRUCTURES_REGISTRY.get(param1.toLowerCase(Locale.ROOT));
        return var0 != null ? var0.getNearestGeneratedFeature(param0, this, param2, param3, param4) : null;
    }

    public void applyBiomeDecoration(WorldGenRegion param0) {
        int var0 = param0.getCenterX();
        int var1 = param0.getCenterZ();
        int var2 = var0 * 16;
        int var3 = var1 * 16;
        BlockPos var4 = new BlockPos(var2, 0, var3);
        Biome var5 = this.getDecorationBiome(param0, var4.offset(8, 8, 8));
        WorldgenRandom var6 = new WorldgenRandom();
        long var7 = var6.setDecorationSeed(param0.getSeed(), var2, var3);

        for(GenerationStep.Decoration var8 : GenerationStep.Decoration.values()) {
            try {
                var5.generate(var8, this, param0, var7, var6, var4);
            } catch (Exception var17) {
                CrashReport var10 = CrashReport.forThrowable(var17, "Biome decoration");
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

    public abstract void buildSurfaceAndBedrock(ChunkAccess var1);

    public void spawnOriginalMobs(WorldGenRegion param0) {
    }

    public C getSettings() {
        return this.settings;
    }

    public abstract int getSpawnHeight();

    public void tickCustomSpawners(ServerLevel param0, boolean param1, boolean param2) {
    }

    public boolean isBiomeValidStartForStructure(Biome param0, StructureFeature<? extends FeatureConfiguration> param1) {
        return param0.isValidStart(param1);
    }

    @Nullable
    public <C extends FeatureConfiguration> C getStructureConfiguration(Biome param0, StructureFeature<C> param1) {
        return param0.getStructureConfiguration(param1);
    }

    public BiomeSource getBiomeSource() {
        return this.biomeSource;
    }

    public long getSeed() {
        return this.seed;
    }

    public int getGenDepth() {
        return 256;
    }

    public List<Biome.SpawnerData> getMobsAt(MobCategory param0, BlockPos param1) {
        return this.level.getBiome(param1).getMobs(param0);
    }

    public void createStructures(ChunkAccess param0, ChunkGenerator<?> param1, StructureManager param2) {
        for(StructureFeature<?> var0 : Feature.STRUCTURES_REGISTRY.values()) {
            if (param1.getBiomeSource().canGenerateStructure(var0)) {
                WorldgenRandom var1 = new WorldgenRandom();
                ChunkPos var2 = param0.getPos();
                StructureStart var3 = StructureStart.INVALID_START;
                if (var0.isFeatureChunk(param1, var1, var2.x, var2.z)) {
                    Biome var4 = this.getBiomeSource().getBiome(new BlockPos(var2.getMinBlockX() + 9, 0, var2.getMinBlockZ() + 9));
                    StructureStart var5 = var0.getStartFactory().create(var0, var2.x, var2.z, var4, BoundingBox.getUnknownBox(), 0, param1.getSeed());
                    var5.generatePieces(this, param2, var2.x, var2.z, var4);
                    var3 = var5.isValid() ? var5 : StructureStart.INVALID_START;
                }

                param0.setStartForFeature(var0.getFeatureName(), var3);
            }
        }

    }

    public void createReferences(LevelAccessor param0, ChunkAccess param1) {
        int var0 = 8;
        int var1 = param1.getPos().x;
        int var2 = param1.getPos().z;
        int var3 = var1 << 4;
        int var4 = var2 << 4;

        for(int var5 = var1 - 8; var5 <= var1 + 8; ++var5) {
            for(int var6 = var2 - 8; var6 <= var2 + 8; ++var6) {
                long var7 = ChunkPos.asLong(var5, var6);

                for(Entry<String, StructureStart> var8 : param0.getChunk(var5, var6).getAllStarts().entrySet()) {
                    StructureStart var9 = var8.getValue();
                    if (var9 != StructureStart.INVALID_START && var9.getBoundingBox().intersects(var3, var4, var3 + 15, var4 + 15)) {
                        param1.addReferenceForFeature(var8.getKey(), var7);
                        DebugPackets.sendStructurePacket(param0, var9);
                    }
                }
            }
        }

    }

    public abstract void fillFromNoise(LevelAccessor var1, ChunkAccess var2);

    public int getSeaLevel() {
        return 63;
    }

    public abstract int getBaseHeight(int var1, int var2, Heightmap.Types var3);

    public int getFirstFreeHeight(int param0, int param1, Heightmap.Types param2) {
        return this.getBaseHeight(param0, param1, param2);
    }

    public int getFirstOccupiedHeight(int param0, int param1, Heightmap.Types param2) {
        return this.getBaseHeight(param0, param1, param2) - 1;
    }
}
