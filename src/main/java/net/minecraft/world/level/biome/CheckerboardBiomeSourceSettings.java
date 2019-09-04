package net.minecraft.world.level.biome;

import net.minecraft.world.level.storage.LevelData;

public class CheckerboardBiomeSourceSettings implements BiomeSourceSettings {
    private Biome[] allowedBiomes = new Biome[]{Biomes.PLAINS};
    private int size = 1;

    public CheckerboardBiomeSourceSettings(LevelData param0) {
    }

    public CheckerboardBiomeSourceSettings setAllowedBiomes(Biome[] param0) {
        this.allowedBiomes = param0;
        return this;
    }

    public CheckerboardBiomeSourceSettings setSize(int param0) {
        this.size = param0;
        return this;
    }

    public Biome[] getAllowedBiomes() {
        return this.allowedBiomes;
    }

    public int getSize() {
        return this.size;
    }
}
