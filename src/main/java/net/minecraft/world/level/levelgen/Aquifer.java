package net.minecraft.world.level.levelgen;

import java.util.Arrays;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class Aquifer {
    private final NormalNoise barrierNoise;
    private final NormalNoise waterLevelNoise;
    private final NoiseGeneratorSettings noiseGeneratorSettings;
    private final int[] aquiferCache;
    private final long[] aquiferLocationCache;
    private double lastBarrierDensity;
    private int lastWaterLevel;
    private boolean shouldScheduleWaterUpdate;
    private final NoiseSampler sampler;
    private final int minGridX;
    private final int minGridY;
    private final int minGridZ;
    private final int gridSizeX;
    private final int gridSizeZ;

    public Aquifer(int param0, int param1, NormalNoise param2, NormalNoise param3, NoiseGeneratorSettings param4, NoiseSampler param5, int param6) {
        this.barrierNoise = param2;
        this.waterLevelNoise = param3;
        this.noiseGeneratorSettings = param4;
        this.sampler = param5;
        ChunkPos var0 = new ChunkPos(param0, param1);
        this.minGridX = this.gridX(var0.getMinBlockX()) - 1;
        int var1 = this.gridX(var0.getMaxBlockX()) + 1;
        this.gridSizeX = var1 - this.minGridX + 1;
        int var2 = param4.noiseSettings().minY();
        this.minGridY = this.gridY(var2) - 1;
        int var3 = this.gridY(var2 + param6) + 1;
        int var4 = var3 - this.minGridY + 1;
        this.minGridZ = this.gridZ(var0.getMinBlockZ()) - 1;
        int var5 = this.gridZ(var0.getMaxBlockZ()) + 1;
        this.gridSizeZ = var5 - this.minGridZ + 1;
        int var6 = this.gridSizeX * var4 * this.gridSizeZ;
        this.aquiferCache = new int[var6];
        Arrays.fill(this.aquiferCache, Integer.MAX_VALUE);
        this.aquiferLocationCache = new long[var6];
        Arrays.fill(this.aquiferLocationCache, Long.MAX_VALUE);
    }

    private int getIndex(int param0, int param1, int param2) {
        int var0 = param0 - this.minGridX;
        int var1 = param1 - this.minGridY;
        int var2 = param2 - this.minGridZ;
        return (var1 * this.gridSizeZ + var2) * this.gridSizeX + var0;
    }

    protected void computeAt(int param0, int param1, int param2) {
        int var0 = Math.floorDiv(param0 - 5, 16);
        int var1 = Math.floorDiv(param1 + 1, 12);
        int var2 = Math.floorDiv(param2 - 5, 16);
        int var3 = Integer.MAX_VALUE;
        int var4 = Integer.MAX_VALUE;
        int var5 = Integer.MAX_VALUE;
        long var6 = 0L;
        long var7 = 0L;
        long var8 = 0L;

        for(int var9 = 0; var9 <= 1; ++var9) {
            for(int var10 = -1; var10 <= 1; ++var10) {
                for(int var11 = 0; var11 <= 1; ++var11) {
                    int var12 = var0 + var9;
                    int var13 = var1 + var10;
                    int var14 = var2 + var11;
                    int var15 = this.getIndex(var12, var13, var14);
                    long var16 = this.aquiferLocationCache[var15];
                    long var17;
                    if (var16 != Long.MAX_VALUE) {
                        var17 = var16;
                    } else {
                        WorldgenRandom var18 = new WorldgenRandom(Mth.getSeed(var12, var13 * 3, var14) + 1L);
                        var17 = BlockPos.asLong(var12 * 16 + var18.nextInt(10), var13 * 12 + var18.nextInt(9), var14 * 16 + var18.nextInt(10));
                        this.aquiferLocationCache[var15] = var17;
                    }

                    int var20 = BlockPos.getX(var17) - param0;
                    int var21 = BlockPos.getY(var17) - param1;
                    int var22 = BlockPos.getZ(var17) - param2;
                    int var23 = var20 * var20 + var21 * var21 + var22 * var22;
                    if (var3 >= var23) {
                        var8 = var7;
                        var7 = var6;
                        var6 = var17;
                        var5 = var4;
                        var4 = var3;
                        var3 = var23;
                    } else if (var4 >= var23) {
                        var8 = var7;
                        var7 = var17;
                        var5 = var4;
                        var4 = var23;
                    } else if (var5 >= var23) {
                        var8 = var17;
                        var5 = var23;
                    }
                }
            }
        }

        int var24 = this.getWaterLevel(var6);
        int var25 = this.getWaterLevel(var7);
        int var26 = this.getWaterLevel(var8);
        double var27 = this.similarity(var3, var4);
        double var28 = this.similarity(var3, var5);
        double var29 = this.similarity(var4, var5);
        this.lastWaterLevel = var24;
        this.shouldScheduleWaterUpdate = var27 > 0.0;
        if (var27 > -1.0) {
            double var30 = 1.0 + (this.barrierNoise.getValue((double)param0, (double)param1, (double)param2) + 0.1) / 4.0;
            double var31 = this.calculatePressure(param1, var30, var24, var25);
            double var32 = this.calculatePressure(param1, var30, var24, var26);
            double var33 = this.calculatePressure(param1, var30, var25, var26);
            double var34 = Math.max(0.0, var27);
            double var35 = Math.max(0.0, var28);
            double var36 = Math.max(0.0, var29);
            double var37 = 2.0 * var34 * Math.max(var31, Math.max(var32 * var35, var33 * var36));
            this.lastBarrierDensity = Math.max(0.0, var37);
        } else {
            this.lastBarrierDensity = 0.0;
        }

    }

    private double similarity(int param0, int param1) {
        double var0 = 25.0;
        return 1.0 - (double)Math.abs(param1 - param0) / 25.0;
    }

    private double calculatePressure(int param0, double param1, int param2, int param3) {
        return 0.5 * (double)Math.abs(param2 - param3) * param1 - Math.abs(0.5 * (double)(param2 + param3) - (double)param0 - 0.5);
    }

    private int gridX(int param0) {
        return Math.floorDiv(param0, 16);
    }

    private int gridY(int param0) {
        return Math.floorDiv(param0, 12);
    }

    private int gridZ(int param0) {
        return Math.floorDiv(param0, 16);
    }

    private int getWaterLevel(long param0) {
        int var0 = BlockPos.getX(param0);
        int var1 = BlockPos.getY(param0);
        int var2 = BlockPos.getZ(param0);
        int var3 = this.gridX(var0);
        int var4 = this.gridY(var1);
        int var5 = this.gridZ(var2);
        int var6 = this.getIndex(var3, var4, var5);
        int var7 = this.aquiferCache[var6];
        if (var7 != Integer.MAX_VALUE) {
            return var7;
        } else {
            int var8 = this.computeAquifer(var0, var1, var2);
            this.aquiferCache[var6] = var8;
            return var8;
        }
    }

    private int computeAquifer(int param0, int param1, int param2) {
        int var0 = this.noiseGeneratorSettings.seaLevel();
        if (param1 > 30) {
            return var0;
        } else {
            int var1 = 64;
            int var2 = -12;
            int var3 = 40;
            double var4 = this.waterLevelNoise
                        .getValue((double)Math.floorDiv(param0, 64), (double)Math.floorDiv(param1, 40) / 1.4, (double)Math.floorDiv(param2, 64))
                    * 30.0
                + -12.0;
            if (Math.abs(var4) > 8.0) {
                var4 *= 4.0;
            }

            int var5 = Math.floorDiv(param1, 40) * 40 + 20;
            int var6 = var5 + Mth.floor(var4);
            return Math.min(56, var6);
        }
    }

    public int getLastWaterLevel() {
        return this.lastWaterLevel;
    }

    public double getLastBarrierDensity() {
        return this.lastBarrierDensity;
    }

    public boolean shouldScheduleWaterUpdate() {
        return this.shouldScheduleWaterUpdate;
    }
}
