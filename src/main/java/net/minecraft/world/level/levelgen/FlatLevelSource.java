package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;

public class FlatLevelSource extends ChunkGenerator {
    public static final Codec<FlatLevelSource> CODEC = FlatLevelGeneratorSettings.CODEC
        .fieldOf("settings")
        .xmap(FlatLevelSource::new, FlatLevelSource::settings)
        .codec();
    private final FlatLevelGeneratorSettings settings;

    public FlatLevelSource(FlatLevelGeneratorSettings param0) {
        super(new FixedBiomeSource(param0.getBiomeFromSettings()), new FixedBiomeSource(param0.getBiome()), param0.structureSettings(), 0L);
        this.settings = param0;
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
    public void buildSurfaceAndBedrock(WorldGenRegion param0, ChunkAccess param1) {
    }

    @Override
    public int getSpawnHeight(LevelHeightAccessor param0) {
        return param0.getMinBuildHeight() + Math.min(param0.getHeight(), this.settings.getLayers().size());
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Executor param0, StructureFeatureManager param1, ChunkAccess param2) {
        List<BlockState> var0 = this.settings.getLayers();
        BlockPos.MutableBlockPos var1 = new BlockPos.MutableBlockPos();
        Heightmap var2 = param2.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        Heightmap var3 = param2.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);

        for(int var4 = 0; var4 < Math.min(param2.getHeight(), var0.size()); ++var4) {
            BlockState var5 = var0.get(var4);
            if (var5 != null) {
                int var6 = param2.getMinBuildHeight() + var4;

                for(int var7 = 0; var7 < 16; ++var7) {
                    for(int var8 = 0; var8 < 16; ++var8) {
                        param2.setBlockState(var1.set(var7, var6, var8), var5, false);
                        var2.update(var7, var6, var8, var5);
                        var3.update(var7, var6, var8, var5);
                    }
                }
            }
        }

        return CompletableFuture.completedFuture(param2);
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
}
