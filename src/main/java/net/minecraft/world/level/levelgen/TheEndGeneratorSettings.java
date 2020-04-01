package net.minecraft.world.level.levelgen;

import java.util.Random;
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

    public TheEndGeneratorSettings() {
    }

    public TheEndGeneratorSettings(Random param0) {
        this.defaultBlock = this.randomGroundBlock(param0);
        this.defaultFluid = this.randomLiquidBlock(param0);
    }
}
