package net.minecraft.world.level.biome;

import net.minecraft.core.QuartPos;

public enum NearestNeighborBiomeZoomer implements BiomeZoomer {
    INSTANCE;

    @Override
    public Biome getBiome(long param0, int param1, int param2, int param3, BiomeManager.NoiseBiomeSource param4) {
        return param4.getNoiseBiome(QuartPos.fromBlock(param1), QuartPos.fromBlock(param2), QuartPos.fromBlock(param3));
    }
}
