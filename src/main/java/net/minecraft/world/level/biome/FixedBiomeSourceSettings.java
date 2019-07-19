package net.minecraft.world.level.biome;

public class FixedBiomeSourceSettings implements BiomeSourceSettings {
    private Biome biome = Biomes.PLAINS;

    public FixedBiomeSourceSettings setBiome(Biome param0) {
        this.biome = param0;
        return this;
    }

    public Biome getBiome() {
        return this.biome;
    }
}
