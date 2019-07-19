package net.minecraft.world.level.levelgen;

import net.minecraft.core.BlockPos;

public class TheEndGeneratorSettings extends ChunkGeneratorSettings {
    private BlockPos spawnPosition;

    public TheEndGeneratorSettings setSpawnPosition(BlockPos param0) {
        this.spawnPosition = param0;
        return this;
    }

    public BlockPos getSpawnPosition() {
        return this.spawnPosition;
    }
}
