package net.minecraft.world.level.levelgen.carver;

import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.WorldGenerationContext;

public class CarvingContext extends WorldGenerationContext {
    private final NoiseBasedChunkGenerator generator;
    private final RegistryAccess registryAccess;
    private final NoiseChunk noiseChunk;

    public CarvingContext(NoiseBasedChunkGenerator param0, RegistryAccess param1, LevelHeightAccessor param2, NoiseChunk param3) {
        super(param0, param2);
        this.generator = param0;
        this.registryAccess = param1;
        this.noiseChunk = param3;
    }

    @Deprecated
    public Optional<BlockState> topMaterial(Function<BlockPos, Holder<Biome>> param0, ChunkAccess param1, BlockPos param2, boolean param3) {
        return this.generator.topMaterial(this, param0, param1, this.noiseChunk, param2, param3);
    }

    @Deprecated
    public RegistryAccess registryAccess() {
        return this.registryAccess;
    }
}
