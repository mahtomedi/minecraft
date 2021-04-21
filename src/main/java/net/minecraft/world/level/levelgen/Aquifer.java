package net.minecraft.world.level.levelgen;

import java.util.Arrays;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public interface Aquifer {
    int ALWAYS_LAVA_AT_OR_BELOW_Y_INDEX = 9;
    int ALWAYS_USE_SEA_LEVEL_WHEN_ABOVE = 30;

    static Aquifer create(
        ChunkPos param0, NormalNoise param1, NormalNoise param2, NormalNoise param3, NoiseGeneratorSettings param4, NoiseSampler param5, int param6, int param7
    ) {
        return new Aquifer.NoiseBasedAquifer(param0, param1, param2, param3, param4, param5, param6, param7);
    }

    static Aquifer createDisabled(final int param0, final BlockState param1) {
        return new Aquifer() {
            @Override
            public BlockState computeState(BaseStoneSource param0x, int param1x, int param2, int param3, double param4) {
                if (param4 > 0.0) {
                    return param0.getBaseBlock(param1, param2, param3);
                } else {
                    return param2 >= param0 ? Blocks.AIR.defaultBlockState() : param1;
                }
            }

            @Override
            public boolean shouldScheduleFluidUpdate() {
                return false;
            }
        };
    }

    BlockState computeState(BaseStoneSource var1, int var2, int var3, int var4, double var5);

    boolean shouldScheduleFluidUpdate();

    public static class NoiseBasedAquifer implements Aquifer {
        private static final int X_RANGE = 10;
        private static final int Y_RANGE = 9;
        private static final int Z_RANGE = 10;
        private static final int X_SEPARATION = 6;
        private static final int Y_SEPARATION = 3;
        private static final int Z_SEPARATION = 6;
        private static final int X_SPACING = 16;
        private static final int Y_SPACING = 12;
        private static final int Z_SPACING = 16;
        private final NormalNoise barrierNoise;
        private final NormalNoise waterLevelNoise;
        private final NormalNoise lavaNoise;
        private final NoiseGeneratorSettings noiseGeneratorSettings;
        private final Aquifer.NoiseBasedAquifer.AquiferStatus[] aquiferCache;
        private final long[] aquiferLocationCache;
        private boolean shouldScheduleFluidUpdate;
        private final NoiseSampler sampler;
        private final int minGridX;
        private final int minGridY;
        private final int minGridZ;
        private final int gridSizeX;
        private final int gridSizeZ;

        NoiseBasedAquifer(
            ChunkPos param0,
            NormalNoise param1,
            NormalNoise param2,
            NormalNoise param3,
            NoiseGeneratorSettings param4,
            NoiseSampler param5,
            int param6,
            int param7
        ) {
            this.barrierNoise = param1;
            this.waterLevelNoise = param2;
            this.lavaNoise = param3;
            this.noiseGeneratorSettings = param4;
            this.sampler = param5;
            this.minGridX = this.gridX(param0.getMinBlockX()) - 1;
            int var0 = this.gridX(param0.getMaxBlockX()) + 1;
            this.gridSizeX = var0 - this.minGridX + 1;
            this.minGridY = this.gridY(param6) - 1;
            int var1 = this.gridY(param6 + param7) + 1;
            int var2 = var1 - this.minGridY + 1;
            this.minGridZ = this.gridZ(param0.getMinBlockZ()) - 1;
            int var3 = this.gridZ(param0.getMaxBlockZ()) + 1;
            this.gridSizeZ = var3 - this.minGridZ + 1;
            int var4 = this.gridSizeX * var2 * this.gridSizeZ;
            this.aquiferCache = new Aquifer.NoiseBasedAquifer.AquiferStatus[var4];
            this.aquiferLocationCache = new long[var4];
            Arrays.fill(this.aquiferLocationCache, Long.MAX_VALUE);
        }

        private int getIndex(int param0, int param1, int param2) {
            int var0 = param0 - this.minGridX;
            int var1 = param1 - this.minGridY;
            int var2 = param2 - this.minGridZ;
            return (var1 * this.gridSizeZ + var2) * this.gridSizeX + var0;
        }

        @Override
        public BlockState computeState(BaseStoneSource param0, int param1, int param2, int param3, double param4) {
            if (param4 <= 0.0) {
                double var1;
                BlockState var0;
                boolean var2;
                if (this.isLavaLevel(param2)) {
                    var0 = Blocks.LAVA.defaultBlockState();
                    var1 = 0.0;
                    var2 = false;
                } else {
                    int var3 = Math.floorDiv(param1 - 5, 16);
                    int var4 = Math.floorDiv(param2 + 1, 12);
                    int var5 = Math.floorDiv(param3 - 5, 16);
                    int var6 = Integer.MAX_VALUE;
                    int var7 = Integer.MAX_VALUE;
                    int var8 = Integer.MAX_VALUE;
                    long var9 = 0L;
                    long var10 = 0L;
                    long var11 = 0L;

                    for(int var12 = 0; var12 <= 1; ++var12) {
                        for(int var13 = -1; var13 <= 1; ++var13) {
                            for(int var14 = 0; var14 <= 1; ++var14) {
                                int var15 = var3 + var12;
                                int var16 = var4 + var13;
                                int var17 = var5 + var14;
                                int var18 = this.getIndex(var15, var16, var17);
                                long var19 = this.aquiferLocationCache[var18];
                                long var20;
                                if (var19 != Long.MAX_VALUE) {
                                    var20 = var19;
                                } else {
                                    WorldgenRandom var21 = new WorldgenRandom(Mth.getSeed(var15, var16 * 3, var17) + 1L);
                                    var20 = BlockPos.asLong(var15 * 16 + var21.nextInt(10), var16 * 12 + var21.nextInt(9), var17 * 16 + var21.nextInt(10));
                                    this.aquiferLocationCache[var18] = var20;
                                }

                                int var23 = BlockPos.getX(var20) - param1;
                                int var24 = BlockPos.getY(var20) - param2;
                                int var25 = BlockPos.getZ(var20) - param3;
                                int var26 = var23 * var23 + var24 * var24 + var25 * var25;
                                if (var6 >= var26) {
                                    var11 = var10;
                                    var10 = var9;
                                    var9 = var20;
                                    var8 = var7;
                                    var7 = var6;
                                    var6 = var26;
                                } else if (var7 >= var26) {
                                    var11 = var10;
                                    var10 = var20;
                                    var8 = var7;
                                    var7 = var26;
                                } else if (var8 >= var26) {
                                    var11 = var20;
                                    var8 = var26;
                                }
                            }
                        }
                    }

                    Aquifer.NoiseBasedAquifer.AquiferStatus var27 = this.getAquiferStatus(var9);
                    Aquifer.NoiseBasedAquifer.AquiferStatus var28 = this.getAquiferStatus(var10);
                    Aquifer.NoiseBasedAquifer.AquiferStatus var29 = this.getAquiferStatus(var11);
                    double var30 = this.similarity(var6, var7);
                    double var31 = this.similarity(var6, var8);
                    double var32 = this.similarity(var7, var8);
                    var2 = var30 > 0.0;
                    if (var27.fluidLevel >= param2 && var27.fluidType.is(Blocks.WATER) && this.isLavaLevel(param2 - 1)) {
                        var1 = 1.0;
                    } else if (var30 > -1.0) {
                        double var35 = 1.0 + (this.barrierNoise.getValue((double)param1, (double)param2, (double)param3) + 0.05) / 4.0;
                        double var36 = this.calculatePressure(param2, var35, var27, var28);
                        double var37 = this.calculatePressure(param2, var35, var27, var29);
                        double var38 = this.calculatePressure(param2, var35, var28, var29);
                        double var39 = Math.max(0.0, var30);
                        double var40 = Math.max(0.0, var31);
                        double var41 = Math.max(0.0, var32);
                        double var42 = 2.0 * var39 * Math.max(var36, Math.max(var37 * var40, var38 * var41));
                        var1 = Math.max(0.0, var42);
                    } else {
                        var1 = 0.0;
                    }

                    var0 = param2 >= var27.fluidLevel ? Blocks.AIR.defaultBlockState() : var27.fluidType;
                }

                if (param4 + var1 <= 0.0) {
                    this.shouldScheduleFluidUpdate = var2;
                    return var0;
                }
            }

            this.shouldScheduleFluidUpdate = false;
            return param0.getBaseBlock(param1, param2, param3);
        }

        @Override
        public boolean shouldScheduleFluidUpdate() {
            return this.shouldScheduleFluidUpdate;
        }

        private boolean isLavaLevel(int param0) {
            return param0 - this.noiseGeneratorSettings.noiseSettings().minY() <= 9;
        }

        private double similarity(int param0, int param1) {
            double var0 = 25.0;
            return 1.0 - (double)Math.abs(param1 - param0) / 25.0;
        }

        private double calculatePressure(
            int param0, double param1, Aquifer.NoiseBasedAquifer.AquiferStatus param2, Aquifer.NoiseBasedAquifer.AquiferStatus param3
        ) {
            if (param0 <= param2.fluidLevel && param0 <= param3.fluidLevel && param2.fluidType != param3.fluidType) {
                return 1.0;
            } else {
                int var0 = Math.abs(param2.fluidLevel - param3.fluidLevel);
                double var1 = 0.5 * (double)(param2.fluidLevel + param3.fluidLevel);
                double var2 = Math.abs(var1 - (double)param0 - 0.5);
                return 0.5 * (double)var0 * param1 - var2;
            }
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

        private Aquifer.NoiseBasedAquifer.AquiferStatus getAquiferStatus(long param0) {
            int var0 = BlockPos.getX(param0);
            int var1 = BlockPos.getY(param0);
            int var2 = BlockPos.getZ(param0);
            int var3 = this.gridX(var0);
            int var4 = this.gridY(var1);
            int var5 = this.gridZ(var2);
            int var6 = this.getIndex(var3, var4, var5);
            Aquifer.NoiseBasedAquifer.AquiferStatus var7 = this.aquiferCache[var6];
            if (var7 != null) {
                return var7;
            } else {
                Aquifer.NoiseBasedAquifer.AquiferStatus var8 = this.computeAquifer(var0, var1, var2);
                this.aquiferCache[var6] = var8;
                return var8;
            }
        }

        private Aquifer.NoiseBasedAquifer.AquiferStatus computeAquifer(int param0, int param1, int param2) {
            int var0 = this.noiseGeneratorSettings.seaLevel();
            if (param1 > 30) {
                return new Aquifer.NoiseBasedAquifer.AquiferStatus(var0, Blocks.WATER.defaultBlockState());
            } else {
                int var1 = 64;
                int var2 = -10;
                int var3 = 40;
                double var4 = this.waterLevelNoise
                            .getValue((double)Math.floorDiv(param0, 64), (double)Math.floorDiv(param1, 40) / 1.4, (double)Math.floorDiv(param2, 64))
                        * 30.0
                    + -10.0;
                boolean var5 = false;
                if (Math.abs(var4) > 8.0) {
                    var4 *= 4.0;
                }

                int var6 = Math.floorDiv(param1, 40) * 40 + 20;
                int var7 = var6 + Mth.floor(var4);
                if (var6 == -20) {
                    double var8 = this.lavaNoise
                        .getValue((double)Math.floorDiv(param0, 64), (double)Math.floorDiv(param1, 40) / 1.4, (double)Math.floorDiv(param2, 64));
                    var5 = Math.abs(var8) > 0.22F;
                }

                return new Aquifer.NoiseBasedAquifer.AquiferStatus(
                    Math.min(56, var7), var5 ? Blocks.LAVA.defaultBlockState() : Blocks.WATER.defaultBlockState()
                );
            }
        }

        static final class AquiferStatus {
            private final int fluidLevel;
            private final BlockState fluidType;

            public AquiferStatus(int param0, BlockState param1) {
                this.fluidLevel = param0;
                this.fluidType = param1;
            }
        }
    }
}
