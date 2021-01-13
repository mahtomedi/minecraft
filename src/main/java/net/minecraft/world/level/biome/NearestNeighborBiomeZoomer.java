package net.minecraft.world.level.biome;

public enum NearestNeighborBiomeZoomer implements BiomeZoomer {
    INSTANCE;

    @Override
    public Biome getBiome(long param0, int param1, int param2, int param3, BiomeManager.NoiseBiomeSource param4) {
        return param4.getNoiseBiome(param1 >> 2, param2 >> 2, param3 >> 2);
    }
}
