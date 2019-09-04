package net.minecraft.world.level.biome;

import net.minecraft.core.BlockPos;

public class BiomeManager {
    private final BiomeManager.NoiseBiomeSource noiseBiomeSource;
    private final long biomeZoomSeed;
    private final BiomeZoomer zoomer;

    public BiomeManager(BiomeManager.NoiseBiomeSource param0, long param1, BiomeZoomer param2) {
        this.noiseBiomeSource = param0;
        this.biomeZoomSeed = param1;
        this.zoomer = param2;
    }

    public BiomeManager withDifferentSource(BiomeSource param0) {
        return new BiomeManager(param0, this.biomeZoomSeed, this.zoomer);
    }

    public Biome getBiome(BlockPos param0) {
        return this.zoomer.getBiome(this.biomeZoomSeed, param0.getX(), param0.getY(), param0.getZ(), this.noiseBiomeSource);
    }

    public interface NoiseBiomeSource {
        Biome getNoiseBiome(int var1, int var2, int var3);
    }
}
