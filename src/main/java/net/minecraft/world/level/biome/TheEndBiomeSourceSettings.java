package net.minecraft.world.level.biome;

public class TheEndBiomeSourceSettings implements BiomeSourceSettings {
    private final long seed;

    public TheEndBiomeSourceSettings(long param0) {
        this.seed = param0;
    }

    public long getSeed() {
        return this.seed;
    }
}
