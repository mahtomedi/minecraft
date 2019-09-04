package net.minecraft.world.level.biome;

public enum FuzzyOffsetConstantColumnBiomeZoomer implements BiomeZoomer {
    INSTANCE;

    @Override
    public Biome getBiome(long param0, int param1, int param2, int param3, BiomeManager.NoiseBiomeSource param4) {
        return FuzzyOffsetBiomeZoomer.INSTANCE.getBiome(param0, param1, 0, param3, param4);
    }
}
