package net.minecraft.world.level.levelgen.carver;

import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldGenerationContext;

public class CarvingContext implements WorldGenerationContext {
    private final ChunkGenerator generator;

    public CarvingContext(ChunkGenerator param0) {
        this.generator = param0;
    }

    @Override
    public int getMinGenY() {
        return this.generator.getMinY();
    }

    @Override
    public int getGenDepth() {
        return this.generator.getGenDepth();
    }
}
