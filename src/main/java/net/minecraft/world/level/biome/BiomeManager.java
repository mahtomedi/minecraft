package net.minecraft.world.level.biome;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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

    @OnlyIn(Dist.CLIENT)
    public Biome getNoiseBiomeAtPosition(double param0, double param1, double param2) {
        int var0 = Mth.floor(param0) >> 2;
        int var1 = Mth.floor(param1) >> 2;
        int var2 = Mth.floor(param2) >> 2;
        return this.getNoiseBiomeAtQuart(var0, var1, var2);
    }

    @OnlyIn(Dist.CLIENT)
    public Biome getNoiseBiomeAtPosition(BlockPos param0) {
        int var0 = param0.getX() >> 2;
        int var1 = param0.getY() >> 2;
        int var2 = param0.getZ() >> 2;
        return this.getNoiseBiomeAtQuart(var0, var1, var2);
    }

    @OnlyIn(Dist.CLIENT)
    public Biome getNoiseBiomeAtQuart(int param0, int param1, int param2) {
        return this.noiseBiomeSource.getNoiseBiome(param0, param1, param2);
    }

    public interface NoiseBiomeSource {
        Biome getNoiseBiome(int var1, int var2, int var3);
    }
}
