package net.minecraft.world.level.biome;

import net.minecraft.world.level.LevelType;
import net.minecraft.world.level.levelgen.OverworldGeneratorSettings;
import net.minecraft.world.level.storage.LevelData;

public class OverworldBiomeSourceSettings implements BiomeSourceSettings {
    private final long seed;
    private final LevelType generatorType;
    private OverworldGeneratorSettings generatorSettings = new OverworldGeneratorSettings();

    public OverworldBiomeSourceSettings(LevelData param0) {
        this.seed = param0.getSeed();
        this.generatorType = param0.getGeneratorType();
    }

    public OverworldBiomeSourceSettings setGeneratorSettings(OverworldGeneratorSettings param0) {
        this.generatorSettings = param0;
        return this;
    }

    public long getSeed() {
        return this.seed;
    }

    public LevelType getGeneratorType() {
        return this.generatorType;
    }

    public OverworldGeneratorSettings getGeneratorSettings() {
        return this.generatorSettings;
    }
}
