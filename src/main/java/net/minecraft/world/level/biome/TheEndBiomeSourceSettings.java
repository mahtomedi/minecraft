package net.minecraft.world.level.biome;

import net.minecraft.world.level.storage.LevelData;

public class TheEndBiomeSourceSettings implements BiomeSourceSettings {
    private final long seed;

    public TheEndBiomeSourceSettings(LevelData param0) {
        this.seed = param0.getSeed();
    }

    public long getSeed() {
        return this.seed;
    }
}
