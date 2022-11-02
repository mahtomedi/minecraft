package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.structure.StructureSet;

public class FlatLevelSource extends ChunkGenerator {
    public static final Codec<FlatLevelSource> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(FlatLevelGeneratorSettings.CODEC.fieldOf("settings").forGetter(FlatLevelSource::settings))
                .apply(param0, param0.stable(FlatLevelSource::new))
    );
    private final FlatLevelGeneratorSettings settings;

    public FlatLevelSource(FlatLevelGeneratorSettings param0) {
        super(new FixedBiomeSource(param0.getBiome()), Util.memoize(param0::adjustGenerationSettings));
        this.settings = param0;
    }

    @Override
    public ChunkGeneratorStructureState createState(HolderLookup<StructureSet> param0, RandomState param1, long param2) {
        Stream<Holder<StructureSet>> var0 = this.settings
            .structureOverrides()
            .map(HolderSet::stream)
            .orElseGet(
                () -> param0.listElements()
                        .map((Function<? super Holder.Reference<StructureSet>, ? extends Holder.Reference<StructureSet>>)(param0x -> param0x))
            );
        return ChunkGeneratorStructureState.createForFlat(param1, param2, this.biomeSource, var0);
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    public FlatLevelGeneratorSettings settings() {
        return this.settings;
    }

    @Override
    public void buildSurface(WorldGenRegion param0, StructureManager param1, RandomState param2, ChunkAccess param3) {
    }

    @Override
    public int getSpawnHeight(LevelHeightAccessor param0) {
        return param0.getMinBuildHeight() + Math.min(param0.getHeight(), this.settings.getLayers().size());
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Executor param0, Blender param1, RandomState param2, StructureManager param3, ChunkAccess param4) {
        List<BlockState> var0 = this.settings.getLayers();
        BlockPos.MutableBlockPos var1 = new BlockPos.MutableBlockPos();
        Heightmap var2 = param4.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        Heightmap var3 = param4.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);

        for(int var4 = 0; var4 < Math.min(param4.getHeight(), var0.size()); ++var4) {
            BlockState var5 = var0.get(var4);
            if (var5 != null) {
                int var6 = param4.getMinBuildHeight() + var4;

                for(int var7 = 0; var7 < 16; ++var7) {
                    for(int var8 = 0; var8 < 16; ++var8) {
                        param4.setBlockState(var1.set(var7, var6, var8), var5, false);
                        var2.update(var7, var6, var8, var5);
                        var3.update(var7, var6, var8, var5);
                    }
                }
            }
        }

        return CompletableFuture.completedFuture(param4);
    }

    @Override
    public int getBaseHeight(int param0, int param1, Heightmap.Types param2, LevelHeightAccessor param3, RandomState param4) {
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
    public NoiseColumn getBaseColumn(int param0, int param1, LevelHeightAccessor param2, RandomState param3) {
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
    public void addDebugScreenInfo(List<String> param0, RandomState param1, BlockPos param2) {
    }

    @Override
    public void applyCarvers(
        WorldGenRegion param0, long param1, RandomState param2, BiomeManager param3, StructureManager param4, ChunkAccess param5, GenerationStep.Carving param6
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
