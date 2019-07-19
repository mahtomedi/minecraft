package net.minecraft.world.level.biome;

public class TheEndBiomeSourceSettings implements BiomeSourceSettings {
    private long seed;

    public TheEndBiomeSourceSettings setSeed(long param0) {
        this.seed = param0;
        return this;
    }

    public long getSeed() {
        return this.seed;
    }
}
