package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;

public class DebugLevelSource extends ChunkGenerator {
    public static final Codec<DebugLevelSource> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    RegistryOps.retrieveRegistry(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY).forGetter(param0x -> param0x.configuredStructures),
                    RegistryOps.retrieveRegistry(Registry.BIOME_REGISTRY).forGetter(param0x -> param0x.biomes)
                )
                .apply(param0, param0.stable(DebugLevelSource::new))
    );
    private static final int BLOCK_MARGIN = 2;
    private static final List<BlockState> ALL_BLOCKS = StreamSupport.stream(Registry.BLOCK.spliterator(), false)
        .flatMap(param0 -> param0.getStateDefinition().getPossibleStates().stream())
        .collect(Collectors.toList());
    private static final int GRID_WIDTH = Mth.ceil(Mth.sqrt((float)ALL_BLOCKS.size()));
    private static final int GRID_HEIGHT = Mth.ceil((float)ALL_BLOCKS.size() / (float)GRID_WIDTH);
    protected static final BlockState AIR = Blocks.AIR.defaultBlockState();
    protected static final BlockState BARRIER = Blocks.BARRIER.defaultBlockState();
    public static final int HEIGHT = 70;
    public static final int BARRIER_HEIGHT = 60;
    private final Registry<Biome> biomes;

    public DebugLevelSource(Registry<ConfiguredStructureFeature<?, ?>> param0, Registry<Biome> param1) {
        super(param0, new FixedBiomeSource(param1.getOrCreateHolder(Biomes.PLAINS)), new StructureSettings(false));
        this.biomes = param1;
    }

    public Registry<Biome> biomes() {
        return this.biomes;
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public ChunkGenerator withSeed(long param0) {
        return this;
    }

    @Override
    public void buildSurface(WorldGenRegion param0, StructureFeatureManager param1, ChunkAccess param2) {
    }

    @Override
    public void applyBiomeDecoration(WorldGenLevel param0, ChunkAccess param1, StructureFeatureManager param2) {
        BlockPos.MutableBlockPos var0 = new BlockPos.MutableBlockPos();
        ChunkPos var1 = param1.getPos();
        int var2 = var1.x;
        int var3 = var1.z;

        for(int var4 = 0; var4 < 16; ++var4) {
            for(int var5 = 0; var5 < 16; ++var5) {
                int var6 = SectionPos.sectionToBlockCoord(var2, var4);
                int var7 = SectionPos.sectionToBlockCoord(var3, var5);
                param0.setBlock(var0.set(var6, 60, var7), BARRIER, 2);
                BlockState var8 = getBlockStateFor(var6, var7);
                param0.setBlock(var0.set(var6, 70, var7), var8, 2);
            }
        }

    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Executor param0, Blender param1, StructureFeatureManager param2, ChunkAccess param3) {
        return CompletableFuture.completedFuture(param3);
    }

    @Override
    public int getBaseHeight(int param0, int param1, Heightmap.Types param2, LevelHeightAccessor param3) {
        return 0;
    }

    @Override
    public NoiseColumn getBaseColumn(int param0, int param1, LevelHeightAccessor param2) {
        return new NoiseColumn(0, new BlockState[0]);
    }

    @Override
    public void addDebugScreenInfo(List<String> param0, BlockPos param1) {
    }

    public static BlockState getBlockStateFor(int param0, int param1) {
        BlockState var0 = AIR;
        if (param0 > 0 && param1 > 0 && param0 % 2 != 0 && param1 % 2 != 0) {
            param0 /= 2;
            param1 /= 2;
            if (param0 <= GRID_WIDTH && param1 <= GRID_HEIGHT) {
                int var1 = Mth.abs(param0 * GRID_WIDTH + param1);
                if (var1 < ALL_BLOCKS.size()) {
                    var0 = ALL_BLOCKS.get(var1);
                }
            }
        }

        return var0;
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
        return 63;
    }
}
