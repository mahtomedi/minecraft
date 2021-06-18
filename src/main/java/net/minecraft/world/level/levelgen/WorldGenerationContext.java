package net.minecraft.world.level.levelgen;

import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;

public class WorldGenerationContext {
    private final int minY;
    private final int height;

    public WorldGenerationContext(ChunkGenerator param0, LevelHeightAccessor param1) {
        this.minY = Math.max(param1.getMinBuildHeight(), param0.getMinY());
        this.height = Math.min(param1.getHeight(), param0.getGenDepth());
    }

    public int getMinGenY() {
        return this.minY;
    }

    public int getGenDepth() {
        return this.height;
    }
}
