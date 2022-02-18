package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.structure.StructureSet;

public class FlatLevelSource extends ChunkGenerator {
    public static final Codec<FlatLevelSource> CODEC = RecordCodecBuilder.create(
        param0 -> commonCodec(param0)
                .and(FlatLevelGeneratorSettings.CODEC.fieldOf("settings").forGetter(FlatLevelSource::settings))
                .apply(param0, param0.stable(FlatLevelSource::new))
    );
    private final FlatLevelGeneratorSettings settings;

    public FlatLevelSource(Registry<StructureSet> param0, FlatLevelGeneratorSettings param1) {
        super(param0, param1.structureOverrides(), new FixedBiomeSource(param1.getBiomeFromSettings()), new FixedBiomeSource(param1.getBiome()), 0L);
        this.settings = param1;
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public ChunkGenerator withSeed(long param0) {
        return this;
    }

    public FlatLevelGeneratorSettings settings() {
        return this.settings;
    }

    @Override
    public void buildSurface(WorldGenRegion param0, StructureFeatureManager param1, ChunkAccess param2) {
    }

    @Override
    public int getSpawnHeight(LevelHeightAccessor param0) {
        return param0.getMinBuildHeight() + Math.min(param0.getHeight(), this.settings.getLayers().size());
    }

    @Override
    protected Holder<Biome> adjustBiome(Holder<Biome> param0) {
        return this.settings.getBiome();
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Executor param0, Blender param1, StructureFeatureManager param2, ChunkAccess param3) {
        List<BlockState> var0 = this.settings.getLayers();
        BlockPos.MutableBlockPos var1 = new BlockPos.MutableBlockPos();
        Heightmap var2 = param3.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        Heightmap var3 = param3.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);

        for(int var4 = 0; var4 < Math.min(param3.getHeight(), var0.size()); ++var4) {
            BlockState var5 = var0.get(var4);
            if (var5 != null) {
                int var6 = param3.getMinBuildHeight() + var4;

                for(int var7 = 0; var7 < 16; ++var7) {
                    for(int var8 = 0; var8 < 16; ++var8) {
                        param3.setBlockState(var1.set(var7, var6, var8), var5, false);
                        var2.update(var7, var6, var8, var5);
                        var3.update(var7, var6, var8, var5);
                    }
                }
            }
        }

        return CompletableFuture.completedFuture(param3);
    }

    @Override
    public int getBaseHeight(int param0, int param1, Heightmap.Types param2, LevelHeightAccessor param3) {
        List<BlockState> var0 = this.settings.getLayers();

        for(int var1 = Math.min(var0.size(), param3.getMaxBuildHeight()) - 1; var1 >= 0; --var1) {
            BlockState var2 = var0.get(var1);
            if (var2 != null && param2.isOpaque().test(var2)) {
                return param3.getMinBuildHeight() + var1 + 1;
            }
        }

        return param3.getMinBuildHeight();
    }

    @Override
    public NoiseColumn getBaseColumn(int param0, int param1, LevelHeightAccessor param2) {
        return new NoiseColumn(
            param2.getMinBuildHeight(),
            this.settings
                .getLayers()
                .stream()
                .limit((long)param2.getHeight())
                .map(param0x -> param0x == null ? Blocks.AIR.defaultBlockState() : param0x)
                .toArray(param0x -> new BlockState[param0x])
        );
    }

    @Override
    public void addDebugScreenInfo(List<String> param0, BlockPos param1) {
    }

    @Override
    public Climate.Sampler climateSampler() {
        return Climate.empty();
    }

    @Override
    public void applyCarvers(
        WorldGenRegion param0, long param1, BiomeManager param2, StructureFeatureManager param3, ChunkAccess param4, GenerationStep.Carving param5
    ) {
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion param0) {
    }

    @Override
    public int getMinY() {
        return 0;
    }

    @Override
    public int getGenDepth() {
        return 384;
    }

    @Override
    public int getSeaLevel() {
        return -63;
    }
}
