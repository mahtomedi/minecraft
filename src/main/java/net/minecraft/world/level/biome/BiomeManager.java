package net.minecraft.world.level.biome;

import com.google.common.hash.Hashing;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;

public class BiomeManager {
    private static final int CHUNK_CENTER_QUART = QuartPos.fromBlock(8);
    private final BiomeManager.NoiseBiomeSource noiseBiomeSource;
    private final long biomeZoomSeed;
    private final BiomeZoomer zoomer;

    public BiomeManager(BiomeManager.NoiseBiomeSource param0, long param1, BiomeZoomer param2) {
        this.noiseBiomeSource = param0;
        this.biomeZoomSeed = param1;
        this.zoomer = param2;
    }

    public static long obfuscateSeed(long param0) {
        return Hashing.sha256().hashLong(param0).asLong();
    }

    public BiomeManager withDifferentSource(BiomeSource param0) {
        return new BiomeManager(param0, this.biomeZoomSeed, this.zoomer);
    }

    public Biome getBiome(BlockPos param0) {
        return this.zoomer.getBiome(this.biomeZoomSeed, param0.getX(), param0.getY(), param0.getZ(), this.noiseBiomeSource);
    }

    public Biome getNoiseBiomeAtPosition(double param0, double param1, double param2) {
        int var0 = QuartPos.fromBlock(Mth.floor(param0));
        int var1 = QuartPos.fromBlock(Mth.floor(param1));
        int var2 = QuartPos.fromBlock(Mth.floor(param2));
        return this.getNoiseBiomeAtQuart(var0, var1, var2);
    }

    public Biome getNoiseBiomeAtPosition(BlockPos param0) {
        int var0 = QuartPos.fromBlock(param0.getX());
        int var1 = QuartPos.fromBlock(param0.getY());
        int var2 = QuartPos.fromBlock(param0.getZ());
        return this.getNoiseBiomeAtQuart(var0, var1, var2);
    }

    public Biome getNoiseBiomeAtQuart(int param0, int param1, int param2) {
        return this.noiseBiomeSource.getNoiseBiome(param0, param1, param2);
    }

    public Biome getPrimaryBiomeAtChunk(ChunkPos param0) {
        return this.noiseBiomeSource.getPrimaryBiome(param0);
    }

    public interface NoiseBiomeSource {
        Biome getNoiseBiome(int var1, int var2, int var3);

        default Biome getPrimaryBiome(ChunkPos param0) {
            return this.getNoiseBiome(
                QuartPos.fromSection(param0.x) + BiomeManager.CHUNK_CENTER_QUART, 0, QuartPos.fromSection(param0.z) + BiomeManager.CHUNK_CENTER_QUART
            );
        }
    }
}
