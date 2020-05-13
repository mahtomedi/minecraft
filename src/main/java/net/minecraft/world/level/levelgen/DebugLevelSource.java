package net.minecraft.world.level.levelgen;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class DebugLevelSource extends ChunkGenerator {
    public static final ChunkGenerator INSTANCE = new DebugLevelSource();
    private static final List<BlockState> ALL_BLOCKS = StreamSupport.stream(Registry.BLOCK.spliterator(), false)
        .flatMap(param0 -> param0.getStateDefinition().getPossibleStates().stream())
        .collect(Collectors.toList());
    private static final int GRID_WIDTH = Mth.ceil(Mth.sqrt((float)ALL_BLOCKS.size()));
    private static final int GRID_HEIGHT = Mth.ceil((float)ALL_BLOCKS.size() / (float)GRID_WIDTH);
    protected static final BlockState AIR = Blocks.AIR.defaultBlockState();
    protected static final BlockState BARRIER = Blocks.BARRIER.defaultBlockState();

    private DebugLevelSource() {
        super(new FixedBiomeSource(Biomes.PLAINS), new ChunkGeneratorSettings());
    }

    @OnlyIn(Dist.CLIENT)
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
        int var1 = param0.getCenterX();
        int var2 = param0.getCenterZ();

        for(int var3 = 0; var3 < 16; ++var3) {
            for(int var4 = 0; var4 < 16; ++var4) {
                int var5 = (var1 << 4) + var3;
                int var6 = (var2 << 4) + var4;
                param0.setBlock(var0.set(var5, 60, var6), BARRIER, 2);
                BlockState var7 = getBlockStateFor(var5, var6);
                if (var7 != null) {
                    param0.setBlock(var0.set(var5, 70, var6), var7, 2);
                }
            }
        }

    }

    @Override
    public void fillFromNoise(LevelAccessor param0, StructureFeatureManager param1, ChunkAccess param2) {
    }

    @Override
    public int getBaseHeight(int param0, int param1, Heightmap.Types param2) {
        return 0;
    }

    @Override
    public BlockGetter getBaseColumn(int param0, int param1) {
        return new NoiseColumn(new BlockState[0]);
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
