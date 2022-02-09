package net.minecraft.world.level.biome;

import com.google.common.hash.Hashing;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.util.LinearCongruentialGenerator;
import net.minecraft.util.Mth;

public class BiomeManager {
    public static final int CHUNK_CENTER_QUART = QuartPos.fromBlock(8);
    private static final int ZOOM_BITS = 2;
    private static final int ZOOM = 4;
    private static final int ZOOM_MASK = 3;
    private final BiomeManager.NoiseBiomeSource noiseBiomeSource;
    private final long biomeZoomSeed;

    public BiomeManager(BiomeManager.NoiseBiomeSource param0, long param1) {
        this.noiseBiomeSource = param0;
        this.biomeZoomSeed = param1;
    }

    public static long obfuscateSeed(long param0) {
        return Hashing.sha256().hashLong(param0).asLong();
    }

    public BiomeManager withDifferentSource(BiomeManager.NoiseBiomeSource param0) {
        return new BiomeManager(param0, this.biomeZoomSeed);
    }

    public Holder<Biome> getBiome(BlockPos param0) {
        int var0 = param0.getX() - 2;
        int var1 = param0.getY() - 2;
        int var2 = param0.getZ() - 2;
        int var3 = var0 >> 2;
        int var4 = var1 >> 2;
        int var5 = var2 >> 2;
        double var6 = (double)(var0 & 3) / 4.0;
        double var7 = (double)(var1 & 3) / 4.0;
        double var8 = (double)(var2 & 3) / 4.0;
        int var9 = 0;
        double var10 = Double.POSITIVE_INFINITY;

        for(int var11 = 0; var11 < 8; ++var11) {
            boolean var12 = (var11 & 4) == 0;
            boolean var13 = (var11 & 2) == 0;
            boolean var14 = (var11 & 1) == 0;
            int var15 = var12 ? var3 : var3 + 1;
            int var16 = var13 ? var4 : var4 + 1;
            int var17 = var14 ? var5 : var5 + 1;
            double var18 = var12 ? var6 : var6 - 1.0;
            double var19 = var13 ? var7 : var7 - 1.0;
            double var20 = var14 ? var8 : var8 - 1.0;
            double var21 = getFiddledDistance(this.biomeZoomSeed, var15, var16, var17, var18, var19, var20);
            if (var10 > var21) {
                var9 = var11;
                var10 = var21;
            }
        }

        int var22 = (var9 & 4) == 0 ? var3 : var3 + 1;
        int var23 = (var9 & 2) == 0 ? var4 : var4 + 1;
        int var24 = (var9 & 1) == 0 ? var5 : var5 + 1;
        return this.noiseBiomeSource.getNoiseBiome(var22, var23, var24);
    }

    public Holder<Biome> getNoiseBiomeAtPosition(double param0, double param1, double param2) {
        int var0 = QuartPos.fromBlock(Mth.floor(param0));
        int var1 = QuartPos.fromBlock(Mth.floor(param1));
        int var2 = QuartPos.fromBlock(Mth.floor(param2));
        return this.getNoiseBiomeAtQuart(var0, var1, var2);
    }

    public Holder<Biome> getNoiseBiomeAtPosition(BlockPos param0) {
        int var0 = QuartPos.fromBlock(param0.getX());
        int var1 = QuartPos.fromBlock(param0.getY());
        int var2 = QuartPos.fromBlock(param0.getZ());
        return this.getNoiseBiomeAtQuart(var0, var1, var2);
    }

    public Holder<Biome> getNoiseBiomeAtQuart(int param0, int param1, int param2) {
        return this.noiseBiomeSource.getNoiseBiome(param0, param1, param2);
    }

    private static double getFiddledDistance(long param0, int param1, int param2, int param3, double param4, double param5, double param6) {
        long var0 = LinearCongruentialGenerator.next(param0, (long)param1);
        var0 = LinearCongruentialGenerator.next(var0, (long)param2);
        var0 = LinearCongruentialGenerator.next(var0, (long)param3);
        var0 = LinearCongruentialGenerator.next(var0, (long)param1);
        var0 = LinearCongruentialGenerator.next(var0, (long)param2);
        var0 = LinearCongruentialGenerator.next(var0, (long)param3);
        double var1 = getFiddle(var0);
        var0 = LinearCongruentialGenerator.next(var0, param0);
        double var2 = getFiddle(var0);
        var0 = LinearCongruentialGenerator.next(var0, param0);
        double var3 = getFiddle(var0);
        return Mth.square(param6 + var3) + Mth.square(param5 + var2) + Mth.square(param4 + var1);
    }

    private static double getFiddle(long param0) {
        double var0 = (double)Math.floorMod(param0 >> 24, 1024) / 1024.0;
        return (var0 - 0.5) * 0.9;
    }

    public interface NoiseBiomeSource {
        Holder<Biome> getNoiseBiome(int var1, int var2, int var3);
    }
}
