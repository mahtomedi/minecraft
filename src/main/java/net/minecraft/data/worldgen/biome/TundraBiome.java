package net.minecraft.data.worldgen.biome;

import net.minecraft.world.level.biome.Biome;

public final class TundraBiome extends Biome {
    public TundraBiome(Biome.BiomeBuilder param0) {
        super(param0);
    }

    @Override
    public float getCreatureProbability() {
        return 0.07F;
    }
}
