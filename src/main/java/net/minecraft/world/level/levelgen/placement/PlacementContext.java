package net.minecraft.world.level.levelgen.placement;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldGenerationContext;

public class PlacementContext extends WorldGenerationContext {
    private final WorldGenLevel level;
    private final ChunkGenerator generator;
    private final Optional<PlacedFeature> topFeature;

    public PlacementContext(WorldGenLevel param0, ChunkGenerator param1, Optional<PlacedFeature> param2) {
        super(param1, param0);
        this.level = param0;
        this.generator = param1;
        this.topFeature = param2;
    }

    public int getHeight(Heightmap.Types param0, int param1, int param2) {
        return this.level.getHeight(param0, param1, param2);
    }

    public CarvingMask getCarvingMask(ChunkPos param0, GenerationStep.Carving param1) {
        return ((ProtoChunk)this.level.getChunk(param0.x, param0.z)).getOrCreateCarvingMask(param1);
    }

    public BlockState getBlockState(BlockPos param0) {
        return this.level.getBlockState(param0);
    }

    public int getMinBuildHeight() {
        return this.level.getMinBuildHeight();
    }

    public WorldGenLevel getLevel() {
        return this.level;
    }

    public Optional<PlacedFeature> topFeature() {
        return this.topFeature;
    }

    public ChunkGenerator generator() {
        return this.generator;
    }
}
