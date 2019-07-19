package net.minecraft.world.level.biome;

import net.minecraft.world.level.levelgen.OverworldGeneratorSettings;
import net.minecraft.world.level.storage.LevelData;

public class OverworldBiomeSourceSettings implements BiomeSourceSettings {
    private LevelData levelData;
    private OverworldGeneratorSettings generatorSettings;

    public OverworldBiomeSourceSettings setLevelData(LevelData param0) {
        this.levelData = param0;
        return this;
    }

    public OverworldBiomeSourceSettings setGeneratorSettings(OverworldGeneratorSettings param0) {
        this.generatorSettings = param0;
        return this;
    }

    public LevelData getLevelData() {
        return this.levelData;
    }

    public OverworldGeneratorSettings getGeneratorSettings() {
        return this.generatorSettings;
    }
}
