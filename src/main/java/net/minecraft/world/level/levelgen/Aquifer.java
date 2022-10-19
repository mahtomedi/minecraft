package net.minecraft.world.level.levelgen;

import java.util.Arrays;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.OverworldBiomeBuilder;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import org.apache.commons.lang3.mutable.MutableDouble;

public interface Aquifer {
    static Aquifer create(
        NoiseChunk param0, ChunkPos param1, NoiseRouter param2, PositionalRandomFactory param3, int param4, int param5, Aquifer.FluidPicker param6
    ) {
        return new Aquifer.NoiseBasedAquifer(param0, param1, param2, param3, param4, param5, param6);
    }

    static Aquifer createDisabled(final Aquifer.FluidPicker param0) {
        return new Aquifer() {
            @Nullable
            @Override
            public BlockState computeSubstance(DensityFunction.FunctionContext param0x, double param1) {
                return param1 > 0.0 ? null : param0.computeFluid(param0.blockX(), param0.blockY(), param0.blockZ()).at(param0.blockY());
            }

            @Override
            public boolean shouldScheduleFluidUpdate() {
                return false;
            }
        };
    }

    @Nullable
    BlockState computeSubstance(DensityFunction.FunctionContext var1, double var2);

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
        private static final int MAX_REASONABLE_DISTANCE_TO_AQUIFER_CENTER = 11;
        private static final double FLOWING_UPDATE_SIMULARITY = similarity(Mth.square(10), Mth.square(12));
        private final NoiseChunk noiseChunk;
        private final DensityFunction barrierNoise;
        private final DensityFunction fluidLevelFloodednessNoise;
        private final DensityFunction fluidLevelSpreadNoise;
        private final DensityFunction lavaNoise;
        private final PositionalRandomFactory positionalRandomFactory;
        private final Aquifer.FluidStatus[] aquiferCache;
        private final long[] aquiferLocationCache;
        private final Aquifer.FluidPicker globalFluidPicker;
        private final DensityFunction erosion;
        private final DensityFunction depth;
        private boolean shouldScheduleFluidUpdate;
        private final int minGridX;
        private final int minGridY;
        private final int minGridZ;
        private final int gridSizeX;
        private final int gridSizeZ;
        private static final int[][] SURFACE_SAMPLING_OFFSETS_IN_CHUNKS = new int[][]{
            {0, 0}, {-2, -1}, {-1, -1}, {0, -1}, {1, -1}, {-3, 0}, {-2, 0}, {-1, 0}, {1, 0}, {-2, 1}, {-1, 1}, {0, 1}, {1, 1}
        };

        NoiseBasedAquifer(
            NoiseChunk param0, ChunkPos param1, NoiseRouter param2, PositionalRandomFactory param3, int param4, int param5, Aquifer.FluidPicker param6
        ) {
            this.noiseChunk = param0;
            this.barrierNoise = param2.barrierNoise();
            this.fluidLevelFloodednessNoise = param2.fluidLevelFloodednessNoise();
            this.fluidLevelSpreadNoise = param2.fluidLevelSpreadNoise();
            this.lavaNoise = param2.lavaNoise();
            this.erosion = param2.erosion();
            this.depth = param2.depth();
            this.positionalRandomFactory = param3;
            this.minGridX = this.gridX(param1.getMinBlockX()) - 1;
            this.globalFluidPicker = param6;
            int var0 = this.gridX(param1.getMaxBlockX()) + 1;
            this.gridSizeX = var0 - this.minGridX + 1;
            this.minGridY = this.gridY(param4) - 1;
            int var1 = this.gridY(param4 + param5) + 1;
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
        public BlockState computeSubstance(DensityFunction.FunctionContext param0, double param1) {
            int var0 = param0.blockX();
            int var1 = param0.blockY();
            int var2 = param0.blockZ();
            if (param1 > 0.0) {
                this.shouldScheduleFluidUpdate = false;
                return null;
            } else {
                Aquifer.FluidStatus var3 = this.globalFluidPicker.computeFluid(var0, var1, var2);
                if (var3.at(var1).is(Blocks.LAVA)) {
                    this.shouldScheduleFluidUpdate = false;
                    return Blocks.LAVA.defaultBlockState();
                } else {
                    int var4 = Math.floorDiv(var0 - 5, 16);
                    int var5 = Math.floorDiv(var1 + 1, 12);
                    int var6 = Math.floorDiv(var2 - 5, 16);
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

                                int var24 = BlockPos.getX(var21) - var0;
                                int var25 = BlockPos.getY(var21) - var1;
                                int var26 = BlockPos.getZ(var21) - var2;
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
                    double var29 = similarity(var7, var8);
                    BlockState var30 = var28.at(var1);
                    if (var29 <= 0.0) {
                        this.shouldScheduleFluidUpdate = var29 >= FLOWING_UPDATE_SIMULARITY;
                        return var30;
                    } else if (var30.is(Blocks.WATER) && this.globalFluidPicker.computeFluid(var0, var1 - 1, var2).at(var1 - 1).is(Blocks.LAVA)) {
                        this.shouldScheduleFluidUpdate = true;
                        return var30;
                    } else {
                        MutableDouble var32 = new MutableDouble(Double.NaN);
                        Aquifer.FluidStatus var33 = this.getAquiferStatus(var11);
                        double var34 = var29 * this.calculatePressure(param0, var32, var28, var33);
                        if (param1 + var34 > 0.0) {
                            this.shouldScheduleFluidUpdate = false;
                            return null;
                        } else {
                            Aquifer.FluidStatus var35 = this.getAquiferStatus(var12);
                            double var36 = similarity(var7, var9);
                            if (var36 > 0.0) {
                                double var37 = var29 * var36 * this.calculatePressure(param0, var32, var28, var35);
                                if (param1 + var37 > 0.0) {
                                    this.shouldScheduleFluidUpdate = false;
                                    return null;
                                }
                            }

                            double var38 = similarity(var8, var9);
                            if (var38 > 0.0) {
                                double var39 = var29 * var38 * this.calculatePressure(param0, var32, var33, var35);
                                if (param1 + var39 > 0.0) {
                                    this.shouldScheduleFluidUpdate = false;
                                    return null;
                                }
                            }

                            this.shouldScheduleFluidUpdate = true;
                            return var30;
                        }
                    }
                }
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

        private double calculatePressure(DensityFunction.FunctionContext param0, MutableDouble param1, Aquifer.FluidStatus param2, Aquifer.FluidStatus param3) {
            int var0 = param0.blockY();
            BlockState var1 = param2.at(var0);
            BlockState var2 = param3.at(var0);
            if ((!var1.is(Blocks.LAVA) || !var2.is(Blocks.WATER)) && (!var1.is(Blocks.WATER) || !var2.is(Blocks.LAVA))) {
                int var3 = Math.abs(param2.fluidLevel - param3.fluidLevel);
                if (var3 == 0) {
                    return 0.0;
                } else {
                    double var4 = 0.5 * (double)(param2.fluidLevel + param3.fluidLevel);
                    double var5 = (double)var0 + 0.5 - var4;
                    double var6 = (double)var3 / 2.0;
                    double var7 = 0.0;
                    double var8 = 2.5;
                    double var9 = 1.5;
                    double var10 = 3.0;
                    double var11 = 10.0;
                    double var12 = 3.0;
                    double var13 = var6 - Math.abs(var5);
                    double var15;
                    if (var5 > 0.0) {
                        double var14 = 0.0 + var13;
                        if (var14 > 0.0) {
                            var15 = var14 / 1.5;
                        } else {
                            var15 = var14 / 2.5;
                        }
                    } else {
                        double var17 = 3.0 + var13;
                        if (var17 > 0.0) {
                            var15 = var17 / 3.0;
                        } else {
                            var15 = var17 / 10.0;
                        }
                    }

                    double var20 = 2.0;
                    double var24;
                    if (!(var15 < -2.0) && !(var15 > 2.0)) {
                        double var22 = param1.getValue();
                        if (Double.isNaN(var22)) {
                            double var23 = this.barrierNoise.compute(param0);
                            param1.setValue(var23);
                            var24 = var23;
                        } else {
                            var24 = var22;
                        }
                    } else {
                        var24 = 0.0;
                    }

                    return 2.0 * (var24 + var15);
                }
            } else {
                return 2.0;
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

        private Aquifer.FluidStatus computeFluid(int param0, int param1, int param2) {
            Aquifer.FluidStatus var0 = this.globalFluidPicker.computeFluid(param0, param1, param2);
            int var1 = Integer.MAX_VALUE;
            int var2 = param1 + 12;
            int var3 = param1 - 12;
            boolean var4 = false;

            for(int[] var5 : SURFACE_SAMPLING_OFFSETS_IN_CHUNKS) {
                int var6 = param0 + SectionPos.sectionToBlockCoord(var5[0]);
                int var7 = param2 + SectionPos.sectionToBlockCoord(var5[1]);
                int var8 = this.noiseChunk.preliminarySurfaceLevel(var6, var7);
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

            int var13 = this.computeSurfaceLevel(param0, param1, param2, var0, var1, var4);
            return new Aquifer.FluidStatus(var13, this.computeFluidType(param0, param1, param2, var0, var13));
        }

        private int computeSurfaceLevel(int param0, int param1, int param2, Aquifer.FluidStatus param3, int param4, boolean param5) {
            DensityFunction.SinglePointContext var0 = new DensityFunction.SinglePointContext(param0, param1, param2);
            double var1;
            double var2;
            if (OverworldBiomeBuilder.isDeepDarkRegion(this.erosion, this.depth, var0)) {
                var1 = -1.0;
                var2 = -1.0;
            } else {
                int var3 = param4 + 8 - param1;
                int var4 = 64;
                double var5 = param5 ? Mth.clampedMap((double)var3, 0.0, 64.0, 1.0, 0.0) : 0.0;
                double var6 = Mth.clamp(this.fluidLevelFloodednessNoise.compute(var0), -1.0, 1.0);
                double var7 = Mth.map(var5, 1.0, 0.0, -0.3, 0.8);
                double var8 = Mth.map(var5, 1.0, 0.0, -0.8, 0.4);
                var1 = var6 - var8;
                var2 = var6 - var7;
            }

            int var11;
            if (var2 > 0.0) {
                var11 = param3.fluidLevel;
            } else if (var1 > 0.0) {
                var11 = this.computeRandomizedFluidSurfaceLevel(param0, param1, param2, param4);
            } else {
                var11 = DimensionType.WAY_BELOW_MIN_Y;
            }

            return var11;
        }

        private int computeRandomizedFluidSurfaceLevel(int param0, int param1, int param2, int param3) {
            int var0 = 16;
            int var1 = 40;
            int var2 = Math.floorDiv(param0, 16);
            int var3 = Math.floorDiv(param1, 40);
            int var4 = Math.floorDiv(param2, 16);
            int var5 = var3 * 40 + 20;
            int var6 = 10;
            double var7 = this.fluidLevelSpreadNoise.compute(new DensityFunction.SinglePointContext(var2, var3, var4)) * 10.0;
            int var8 = Mth.quantize(var7, 3);
            int var9 = var5 + var8;
            return Math.min(param3, var9);
        }

        private BlockState computeFluidType(int param0, int param1, int param2, Aquifer.FluidStatus param3, int param4) {
            BlockState var0 = param3.fluidType;
            if (param4 <= -10 && param4 != DimensionType.WAY_BELOW_MIN_Y && param3.fluidType != Blocks.LAVA.defaultBlockState()) {
                int var1 = 64;
                int var2 = 40;
                int var3 = Math.floorDiv(param0, 64);
                int var4 = Math.floorDiv(param1, 40);
                int var5 = Math.floorDiv(param2, 64);
                double var6 = this.lavaNoise.compute(new DensityFunction.SinglePointContext(var3, var4, var5));
                if (Math.abs(var6) > 0.3) {
                    var0 = Blocks.LAVA.defaultBlockState();
                }
            }

            return var0;
        }
    }
}
