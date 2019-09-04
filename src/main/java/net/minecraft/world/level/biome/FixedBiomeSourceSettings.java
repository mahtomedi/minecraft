package net.minecraft.world.level.biome;

import net.minecraft.world.level.storage.LevelData;

public class FixedBiomeSourceSettings implements BiomeSourceSettings {
    private Biome biome = Biomes.PLAINS;

    public FixedBiomeSourceSettings(LevelData param0) {
    }

    public FixedBiomeSourceSettings setBiome(Biome param0) {
        this.biome = param0;
        return this;
    }

    public Biome getBiome() {
        return this.biome;
    }
}
