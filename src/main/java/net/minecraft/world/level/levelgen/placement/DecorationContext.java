package net.minecraft.world.level.levelgen.placement;

import java.util.BitSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;

public class DecorationContext implements LevelHeightAccessor {
    private final WorldGenLevel level;
    private final ChunkGenerator generator;

    public DecorationContext(WorldGenLevel param0, ChunkGenerator param1) {
        this.level = param0;
        this.generator = param1;
    }

    public int getHeight(Heightmap.Types param0, int param1, int param2) {
        return this.level.getHeight(param0, param1, param2);
    }

    public int getGenDepth() {
        return this.generator.getGenDepth();
    }

    public int getSeaLevel() {
        return this.generator.getSeaLevel();
    }

    public BitSet getCarvingMask(ChunkPos param0, GenerationStep.Carving param1) {
        return ((ProtoChunk)this.level.getChunk(param0.x, param0.z)).getOrCreateCarvingMask(param1);
    }

    public BlockState getBlockState(BlockPos param0) {
        return this.level.getBlockState(param0);
    }

    @Override
    public int getSectionsCount() {
        return this.level.getSectionsCount();
    }

    @Override
    public int getMinSection() {
        return this.level.getMinSection();
    }
}
