package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.RegistryLookupCodec;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;

public class DebugLevelSource extends ChunkGenerator {
    public static final Codec<DebugLevelSource> CODEC = RegistryLookupCodec.create(Registry.BIOME_REGISTRY)
        .xmap(DebugLevelSource::new, DebugLevelSource::biomes)
        .stable()
        .codec();
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

    public DebugLevelSource(Registry<Biome> param0) {
        super(new FixedBiomeSource(param0.getOrThrow(Biomes.PLAINS)), new StructureSettings(false));
        this.biomes = param0;
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
    public void buildSurfaceAndBedrock(WorldGenRegion param0, ChunkAccess param1) {
    }

    @Override
    public void applyCarvers(long param0, BiomeManager param1, ChunkAccess param2, GenerationStep.Carving param3) {
    }

    @Override
    public void applyBiomeDecoration(WorldGenRegion param0, StructureFeatureManager param1) {
        BlockPos.MutableBlockPos var0 = new BlockPos.MutableBlockPos();
        ChunkPos var1 = param0.getCenter();

        for(int var2 = 0; var2 < 16; ++var2) {
            for(int var3 = 0; var3 < 16; ++var3) {
                int var4 = SectionPos.sectionToBlockCoord(var1.x, var2);
                int var5 = SectionPos.sectionToBlockCoord(var1.z, var3);
                param0.setBlock(var0.set(var4, 60, var5), BARRIER, 2);
                BlockState var6 = getBlockStateFor(var4, var5);
                if (var6 != null) {
                    param0.setBlock(var0.set(var4, 70, var5), var6, 2);
                }
            }
        }

    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Executor param0, StructureFeatureManager param1, ChunkAccess param2) {
        return CompletableFuture.completedFuture(param2);
    }

    @Override
    public int getBaseHeight(int param0, int param1, Heightmap.Types param2, LevelHeightAccessor param3) {
        return 0;
    }

    @Override
    public NoiseColumn getBaseColumn(int param0, int param1, LevelHeightAccessor param2) {
        return new NoiseColumn(0, new BlockState[0]);
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
}
