package net.minecraft.world.level.biome;

import net.minecraft.util.LinearCongruentialGenerator;

public enum FuzzyOffsetBiomeZoomer implements BiomeZoomer {
    INSTANCE;

    @Override
    public Biome getBiome(long param0, int param1, int param2, int param3, BiomeManager.NoiseBiomeSource param4) {
        int var0 = param1 - 2;
        int var1 = param2 - 2;
        int var2 = param3 - 2;
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
            double var21 = getFiddledDistance(param0, var15, var16, var17, var18, var19, var20);
            if (var10 > var21) {
                var9 = var11;
                var10 = var21;
            }
        }

        int var22 = (var9 & 4) == 0 ? var3 : var3 + 1;
        int var23 = (var9 & 2) == 0 ? var4 : var4 + 1;
        int var24 = (var9 & 1) == 0 ? var5 : var5 + 1;
        return param4.getNoiseBiome(var22, var23, var24);
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
        return sqr(param6 + var3) + sqr(param5 + var2) + sqr(param4 + var1);
    }

    private static double getFiddle(long param0) {
        double var0 = (double)((int)Math.floorMod(param0 >> 24, 1024L)) / 1024.0;
        return (var0 - 0.5) * 0.9;
    }

    private static double sqr(double param0) {
        return param0 * param0;
    }
}
