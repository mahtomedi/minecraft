package net.minecraft.world.level.levelgen;

import java.util.Arrays;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.apache.commons.lang3.mutable.MutableDouble;

public interface Aquifer {
    static Aquifer create(
        NoiseChunk param0,
        ChunkPos param1,
        NormalNoise param2,
        NormalNoise param3,
        NormalNoise param4,
        NormalNoise param5,
        PositionalRandomFactory param6,
        NoiseSampler param7,
        int param8,
        int param9,
        Aquifer.FluidPicker param10
    ) {
        return new Aquifer.NoiseBasedAquifer(param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10);
    }

    static Aquifer createDisabled(final Aquifer.FluidPicker param0) {
        return new Aquifer() {
            @Nullable
            @Override
            public BlockState computeSubstance(int param0x, int param1, int param2, double param3, double param4) {
                return param4 > 0.0 ? null : param0.computeFluid(param0, param1, param2).at(param1);
            }

            @Override
            public boolean shouldScheduleFluidUpdate() {
                return false;
            }
        };
    }

    @Nullable
    BlockState computeSubstance(int var1, int var2, int var3, double var4, double var6);

    boolean shouldScheduleFluidUpdate();

    public interface FluidPicker {
        Aquifer.FluidStatus computeFluid(int var1, int var2, int var3);
    }

    public static final class FluidStatus {
        final int fluidLevel;
        final BlockState fluidType;

        public FluidStatus(int param0, BlockState param1) {
            this.fluidLevel = param0;
            this.fluidType = param1;
        }

        public BlockState at(int param0) {
            return param0 < this.fluidLevel ? this.fluidType : Blocks.AIR.defaultBlockState();
        }
    }

    public static class NoiseBasedAquifer implements Aquifer, Aquifer.FluidPicker {
        private static final int X_RANGE = 10;
        private static final int Y_RANGE = 9;
        private static final int Z_RANGE = 10;
        private static final int X_SEPARATION = 6;
        private static final int Y_SEPARATION = 3;
        private static final int Z_SEPARATION = 6;
        private static final int X_SPACING = 16;
        private static final int Y_SPACING = 12;
        private static final int Z_SPACING = 16;
        private static final int MAX_REASONABLE_DISTANCE_TO_AQUIFER_CENTER = 11;
        private static final double FLOWING_UPDATE_SIMULARITY = similarity(Mth.square(10), Mth.square(12));
        private final NoiseChunk noiseChunk;
        private final NormalNoise barrierNoise;
        private final NormalNoise fluidLevelFloodednessNoise;
        private final NormalNoise fluidLevelSpreadNoise;
        private final NormalNoise lavaNoise;
        private final PositionalRandomFactory positionalRandomFactory;
        private final Aquifer.FluidStatus[] aquiferCache;
        private final long[] aquiferLocationCache;
        private final Aquifer.FluidPicker globalFluidPicker;
        private boolean shouldScheduleFluidUpdate;
        private final NoiseSampler sampler;
        private final int minGridX;
        private final int minGridY;
        private final int minGridZ;
        private final int gridSizeX;
        private final int gridSizeZ;
        private static final int[][] SURFACE_SAMPLING_OFFSETS_IN_CHUNKS = new int[][]{
            {-2, -1}, {-1, -1}, {0, -1}, {1, -1}, {-3, 0}, {-2, 0}, {-1, 0}, {0, 0}, {1, 0}, {-2, 1}, {-1, 1}, {0, 1}, {1, 1}
        };

        NoiseBasedAquifer(
            NoiseChunk param0,
            ChunkPos param1,
            NormalNoise param2,
            NormalNoise param3,
            NormalNoise param4,
            NormalNoise param5,
            PositionalRandomFactory param6,
            NoiseSampler param7,
            int param8,
            int param9,
            Aquifer.FluidPicker param10
        ) {
            this.noiseChunk = param0;
            this.barrierNoise = param2;
            this.fluidLevelFloodednessNoise = param3;
            this.fluidLevelSpreadNoise = param4;
            this.lavaNoise = param5;
            this.positionalRandomFactory = param6;
            this.sampler = param7;
            this.minGridX = this.gridX(param1.getMinBlockX()) - 1;
            this.globalFluidPicker = param10;
            int var0 = this.gridX(param1.getMaxBlockX()) + 1;
            this.gridSizeX = var0 - this.minGridX + 1;
            this.minGridY = this.gridY(param8) - 1;
            int var1 = this.gridY(param8 + param9) + 1;
            int var2 = var1 - this.minGridY + 1;
            this.minGridZ = this.gridZ(param1.getMinBlockZ()) - 1;
            int var3 = this.gridZ(param1.getMaxBlockZ()) + 1;
            this.gridSizeZ = var3 - this.minGridZ + 1;
            int var4 = this.gridSizeX * var2 * this.gridSizeZ;
            this.aquiferCache = new Aquifer.FluidStatus[var4];
            this.aquiferLocationCache = new long[var4];
            Arrays.fill(this.aquiferLocationCache, Long.MAX_VALUE);
        }

        private int getIndex(int param0, int param1, int param2) {
            int var0 = param0 - this.minGridX;
            int var1 = param1 - this.minGridY;
            int var2 = param2 - this.minGridZ;
            return (var1 * this.gridSizeZ + var2) * this.gridSizeX + var0;
        }

        @Nullable
        @Override
        public BlockState computeSubstance(int param0, int param1, int param2, double param3, double param4) {
            if (param3 <= -64.0) {
                return this.globalFluidPicker.computeFluid(param0, param1, param2).at(param1);
            } else {
                if (param4 <= 0.0) {
                    Aquifer.FluidStatus var0 = this.globalFluidPicker.computeFluid(param0, param1, param2);
                    double var2;
                    BlockState var1;
                    boolean var3;
                    if (var0.at(param1).is(Blocks.LAVA)) {
                        var1 = Blocks.LAVA.defaultBlockState();
                        var2 = 0.0;
                        var3 = false;
                    } else {
                        int var4 = Math.floorDiv(param0 - 5, 16);
                        int var5 = Math.floorDiv(param1 + 1, 12);
                        int var6 = Math.floorDiv(param2 - 5, 16);
                        int var7 = Integer.MAX_VALUE;
                        int var8 = Integer.MAX_VALUE;
                        int var9 = Integer.MAX_VALUE;
                        long var10 = 0L;
                        long var11 = 0L;
                        long var12 = 0L;

                        for(int var13 = 0; var13 <= 1; ++var13) {
                            for(int var14 = -1; var14 <= 1; ++var14) {
                                for(int var15 = 0; var15 <= 1; ++var15) {
                                    int var16 = var4 + var13;
                                    int var17 = var5 + var14;
                                    int var18 = var6 + var15;
                                    int var19 = this.getIndex(var16, var17, var18);
                                    long var20 = this.aquiferLocationCache[var19];
                                    long var21;
                                    if (var20 != Long.MAX_VALUE) {
                                        var21 = var20;
                                    } else {
                                        RandomSource var22 = this.positionalRandomFactory.at(var16, var17, var18);
                                        var21 = BlockPos.asLong(var16 * 16 + var22.nextInt(10), var17 * 12 + var22.nextInt(9), var18 * 16 + var22.nextInt(10));
                                        this.aquiferLocationCache[var19] = var21;
                                    }

                                    int var24 = BlockPos.getX(var21) - param0;
                                    int var25 = BlockPos.getY(var21) - param1;
                                    int var26 = BlockPos.getZ(var21) - param2;
                                    int var27 = var24 * var24 + var25 * var25 + var26 * var26;
                                    if (var7 >= var27) {
                                        var12 = var11;
                                        var11 = var10;
                                        var10 = var21;
                                        var9 = var8;
                                        var8 = var7;
                                        var7 = var27;
                                    } else if (var8 >= var27) {
                                        var12 = var11;
                                        var11 = var21;
                                        var9 = var8;
                                        var8 = var27;
                                    } else if (var9 >= var27) {
                                        var12 = var21;
                                        var9 = var27;
                                    }
                                }
                            }
                        }

                        Aquifer.FluidStatus var28 = this.getAquiferStatus(var10);
                        Aquifer.FluidStatus var29 = this.getAquiferStatus(var11);
                        Aquifer.FluidStatus var30 = this.getAquiferStatus(var12);
                        double var31 = similarity(var7, var8);
                        double var32 = similarity(var7, var9);
                        double var33 = similarity(var8, var9);
                        var3 = var31 >= FLOWING_UPDATE_SIMULARITY;
                        if (var28.at(param1).is(Blocks.WATER) && this.globalFluidPicker.computeFluid(param0, param1 - 1, param2).at(param1 - 1).is(Blocks.LAVA)
                            )
                         {
                            var2 = 1.0;
                        } else if (var31 > -1.0) {
                            MutableDouble var36 = new MutableDouble(Double.NaN);
                            double var37 = this.calculatePressure(param0, param1, param2, var36, var28, var29);
                            double var38 = this.calculatePressure(param0, param1, param2, var36, var28, var30);
                            double var39 = this.calculatePressure(param0, param1, param2, var36, var29, var30);
                            double var40 = Math.max(0.0, var31);
                            double var41 = Math.max(0.0, var32);
                            double var42 = Math.max(0.0, var33);
                            double var43 = 2.0 * var40 * Math.max(var37, Math.max(var38 * var41, var39 * var42));
                            var2 = Math.max(0.0, var43);
                        } else {
                            var2 = 0.0;
                        }

                        var1 = var28.at(param1);
                    }

                    if (param4 + var2 <= 0.0) {
                        this.shouldScheduleFluidUpdate = var3;
                        return var1;
                    }
                }

                this.shouldScheduleFluidUpdate = false;
                return null;
            }
        }

        @Override
        public boolean shouldScheduleFluidUpdate() {
            return this.shouldScheduleFluidUpdate;
        }

        private static double similarity(int param0, int param1) {
            double var0 = 25.0;
            return 1.0 - (double)Math.abs(param1 - param0) / 25.0;
        }

        private double calculatePressure(int param0, int param1, int param2, MutableDouble param3, Aquifer.FluidStatus param4, Aquifer.FluidStatus param5) {
            BlockState var0 = param4.at(param1);
            BlockState var1 = param5.at(param1);
            if ((!var0.is(Blocks.LAVA) || !var1.is(Blocks.WATER)) && (!var0.is(Blocks.WATER) || !var1.is(Blocks.LAVA))) {
                int var2 = Math.abs(param4.fluidLevel - param5.fluidLevel);
                if (var2 == 0) {
                    return 0.0;
                } else {
                    double var3 = 0.5 * (double)(param4.fluidLevel + param5.fluidLevel);
                    double var4 = (double)param1 + 0.5 - var3;
                    double var5 = (double)var2 / 2.0;
                    double var6 = 0.0;
                    double var7 = 2.5;
                    double var8 = 1.5;
                    double var9 = 3.0;
                    double var10 = 10.0;
                    double var11 = 3.0;
                    double var12 = var5 - Math.abs(var4);
                    double var14;
                    if (var4 > 0.0) {
                        double var13 = 0.0 + var12;
                        if (var13 > 0.0) {
                            var14 = var13 / 1.5;
                        } else {
                            var14 = var13 / 2.5;
                        }
                    } else {
                        double var16 = 3.0 + var12;
                        if (var16 > 0.0) {
                            var14 = var16 / 3.0;
                        } else {
                            var14 = var16 / 10.0;
                        }
                    }

                    if (!(var14 < -2.0) && !(var14 > 2.0)) {
                        double var19 = param3.getValue();
                        if (Double.isNaN(var19)) {
                            double var20 = 0.5;
                            double var21 = this.barrierNoise.getValue((double)param0, (double)param1 * 0.5, (double)param2);
                            param3.setValue(var21);
                            return var21 + var14;
                        } else {
                            return var19 + var14;
                        }
                    } else {
                        return var14;
                    }
                }
            } else {
                return 1.0;
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

        private Aquifer.FluidStatus getAquiferStatus(long param0) {
            int var0 = BlockPos.getX(param0);
            int var1 = BlockPos.getY(param0);
            int var2 = BlockPos.getZ(param0);
            int var3 = this.gridX(var0);
            int var4 = this.gridY(var1);
            int var5 = this.gridZ(var2);
            int var6 = this.getIndex(var3, var4, var5);
            Aquifer.FluidStatus var7 = this.aquiferCache[var6];
            if (var7 != null) {
                return var7;
            } else {
                Aquifer.FluidStatus var8 = this.computeFluid(var0, var1, var2);
                this.aquiferCache[var6] = var8;
                return var8;
            }
        }

        @Override
        public Aquifer.FluidStatus computeFluid(int param0, int param1, int param2) {
            Aquifer.FluidStatus var0 = this.globalFluidPicker.computeFluid(param0, param1, param2);
            int var1 = Integer.MAX_VALUE;
            int var2 = param1 + 12;
            int var3 = param1 - 12;
            boolean var4 = false;

            for(int[] var5 : SURFACE_SAMPLING_OFFSETS_IN_CHUNKS) {
                int var6 = param0 + SectionPos.sectionToBlockCoord(var5[0]);
                int var7 = param2 + SectionPos.sectionToBlockCoord(var5[1]);
                int var8 = this.sampler
                    .getPreliminarySurfaceLevel(var6, var7, this.noiseChunk.terrainInfoWide(this.sampler, QuartPos.fromBlock(var6), QuartPos.fromBlock(var7)));
                int var9 = var8 + 8;
                boolean var10 = var5[0] == 0 && var5[1] == 0;
                if (var10 && var3 > var9) {
                    return var0;
                }

                boolean var11 = var2 > var9;
                if (var11 || var10) {
                    Aquifer.FluidStatus var12 = this.globalFluidPicker.computeFluid(var6, var9, var7);
                    if (!var12.at(var9).isAir()) {
                        if (var10) {
                            var4 = true;
                        }

                        if (var11) {
                            return var12;
                        }
                    }
                }

                var1 = Math.min(var1, var8);
            }

            int var13 = var1 + 8 - param1;
            int var14 = 64;
            double var15 = var4 ? Mth.clampedMap((double)var13, 0.0, 64.0, 1.0, 0.0) : 0.0;
            double var16 = 0.67;
            double var17 = Mth.clamp(this.fluidLevelFloodednessNoise.getValue((double)param0, (double)param1 * 0.67, (double)param2), -1.0, 1.0);
            double var18 = Mth.map(var15, 1.0, 0.0, -0.3, 0.8);
            if (var17 > var18) {
                return var0;
            } else {
                double var19 = Mth.map(var15, 1.0, 0.0, -0.8, 0.4);
                if (var17 <= var19) {
                    return new Aquifer.FluidStatus(DimensionType.WAY_BELOW_MIN_Y, var0.fluidType);
                } else {
                    int var20 = 16;
                    int var21 = 40;
                    int var22 = Math.floorDiv(param0, 16);
                    int var23 = Math.floorDiv(param1, 40);
                    int var24 = Math.floorDiv(param2, 16);
                    int var25 = var23 * 40 + 20;
                    int var26 = 10;
                    double var27 = this.fluidLevelSpreadNoise.getValue((double)var22, (double)var23 / 1.4, (double)var24) * 10.0;
                    int var28 = Mth.quantize(var27, 3);
                    int var29 = var25 + var28;
                    int var30 = Math.min(var1, var29);
                    BlockState var31 = this.getFluidType(param0, param1, param2, var0, var29);
                    return new Aquifer.FluidStatus(var30, var31);
                }
            }
        }

        private BlockState getFluidType(int param0, int param1, int param2, Aquifer.FluidStatus param3, int param4) {
            if (param4 <= -10) {
                int var0 = 64;
                int var1 = 40;
                int var2 = Math.floorDiv(param0, 64);
                int var3 = Math.floorDiv(param1, 40);
                int var4 = Math.floorDiv(param2, 64);
                double var5 = this.lavaNoise.getValue((double)var2, (double)var3, (double)var4);
                if (Math.abs(var5) > 0.3) {
                    return Blocks.LAVA.defaultBlockState();
                }
            }

            return param3.fluidType;
        }
    }
}
