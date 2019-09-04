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
        double[] var9 = new double[8];

        for(int var10 = 0; var10 < 8; ++var10) {
            boolean var11 = (var10 & 4) == 0;
            boolean var12 = (var10 & 2) == 0;
            boolean var13 = (var10 & 1) == 0;
            int var14 = var11 ? var3 : var3 + 1;
            int var15 = var12 ? var4 : var4 + 1;
            int var16 = var13 ? var5 : var5 + 1;
            double var17 = var11 ? var6 : 1.0 - var6;
            double var18 = var12 ? var7 : 1.0 - var7;
            double var19 = var13 ? var8 : 1.0 - var8;
            var9[var10] = getFiddledDistance(param0, var14, var15, var16, var17, var18, var19);
        }

        int var20 = 0;
        double var21 = var9[0];

        for(int var22 = 1; var22 < 8; ++var22) {
            if (var21 > var9[var22]) {
                var20 = var22;
                var21 = var9[var22];
            }
        }

        int var23 = (var20 & 4) == 0 ? var3 : var3 + 1;
        int var24 = (var20 & 2) == 0 ? var4 : var4 + 1;
        int var25 = (var20 & 1) == 0 ? var5 : var5 + 1;
        return param4.getNoiseBiome(var23, var24, var25);
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
