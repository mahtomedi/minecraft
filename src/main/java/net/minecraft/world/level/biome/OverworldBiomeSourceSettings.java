package net.minecraft.world.level.biome;

import net.minecraft.world.level.LevelType;
import net.minecraft.world.level.levelgen.OverworldGeneratorSettings;

public class OverworldBiomeSourceSettings implements BiomeSourceSettings {
    private final long seed;
    private LevelType generatorType = LevelType.NORMAL;
    private OverworldGeneratorSettings generatorSettings = new OverworldGeneratorSettings();

    public OverworldBiomeSourceSettings(long param0) {
        this.seed = param0;
    }

    public OverworldBiomeSourceSettings setLevelType(LevelType param0) {
        this.generatorType = param0;
        return this;
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
