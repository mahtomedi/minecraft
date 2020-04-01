package net.minecraft.world.level.chunk;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
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
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
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
        ((ProtoChunk)param0).setBiomes(new ChunkBiomeContainer(var0, this.biomeSource));
    }

    protected Biome getCarvingOrDecorationBiome(BiomeManager param0, BlockPos param1) {
        return param0.getBiome(param1);
    }

    public void applyCarvers(BiomeManager param0, ChunkAccess param1, GenerationStep.Carving param2) {
        WorldgenRandom var0 = new WorldgenRandom();
        int var1 = 8;
        ChunkPos var2 = param1.getPos();
        int var3 = var2.x;
        int var4 = var2.z;
        Biome var5 = this.getCarvingOrDecorationBiome(param0, var2.getWorldPosition());
        BitSet var6 = param1.getCarvingMask(param2);

        for(int var7 = var3 - 8; var7 <= var3 + 8; ++var7) {
            for(int var8 = var4 - 8; var8 <= var4 + 8; ++var8) {
                List<ConfiguredWorldCarver<?>> var9 = var5.getCarvers(param2);
                ListIterator<ConfiguredWorldCarver<?>> var10 = var9.listIterator();

                while(var10.hasNext()) {
                    int var11 = var10.nextIndex();
                    ConfiguredWorldCarver<?> var12 = var10.next();
                    var0.setLargeFeatureSeed(this.seed + (long)var11, var7, var8);
                    if (var12.isStartChunk(var0, var7, var8)) {
                        var12.carve(
                            param1, param1x -> this.getCarvingOrDecorationBiome(param0, param1x), var0, this.getSeaLevel(), var7, var8, var3, var4, var6
                        );
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
        Biome var5 = this.getCarvingOrDecorationBiome(param0.getBiomeManager(), var4.offset(8, 8, 8));
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

    public abstract void buildSurfaceAndBedrock(WorldGenRegion var1, ChunkAccess var2);

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

    public void createStructures(BiomeManager param0, ChunkAccess param1, ChunkGenerator<?> param2, StructureManager param3) {
        for(StructureFeature<?> var0 : Feature.STRUCTURES_REGISTRY.values()) {
            if (param2.getBiomeSource().canGenerateStructure(var0)) {
                StructureStart var1 = param1.getStartForFeature(var0.getFeatureName());
                int var2 = var1 != null ? var1.getReferences() : 0;
                WorldgenRandom var3 = new WorldgenRandom();
                ChunkPos var4 = param1.getPos();
                StructureStart var5 = StructureStart.INVALID_START;
                Biome var6 = param0.getBiome(new BlockPos(var4.getMinBlockX() + 9, 0, var4.getMinBlockZ() + 9));
                if (var0.isFeatureChunk(param0, param2, var3, var4.x, var4.z, var6)) {
                    StructureStart var7 = var0.getStartFactory().create(var0, var4.x, var4.z, BoundingBox.getUnknownBox(), var2, param2.getSeed());
                    var7.generatePieces(this, param3, var4.x, var4.z, var6);
                    var5 = var7.isValid() ? var7 : StructureStart.INVALID_START;
                }

                param1.setStartForFeature(var0.getFeatureName(), var5);
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

    public abstract BlockGetter getBaseColumn(int var1, int var2);

    public int getFirstFreeHeight(int param0, int param1, Heightmap.Types param2) {
        return this.getBaseHeight(param0, param1, param2);
    }

    public int getFirstOccupiedHeight(int param0, int param1, Heightmap.Types param2) {
        return this.getBaseHeight(param0, param1, param2) - 1;
    }

    public abstract ChunkGeneratorType<?, ?> getType();

    public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
        BiomeSource var0 = this.getBiomeSource();
        Dynamic<T> var1 = var0.serialize(param0);
        var1 = var1.merge(var1.createString("type"), var1.createString(Registry.BIOME_SOURCE_TYPE.getKey(var0.getType()).toString()));
        return new Dynamic<>(
            param0,
            param0.createMap(
                ImmutableMap.of(
                    param0.createString("type"),
                    param0.createString(Registry.CHUNK_GENERATOR_TYPE.getKey(this.getType()).toString()),
                    param0.createString("settings"),
                    this.getSettings().serialize(param0).getValue(),
                    param0.createString("biome_source"),
                    var1.getValue()
                )
            )
        );
    }
}
