package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;

public class IcebergFeature extends Feature<BlockStateConfiguration> {
    public IcebergFeature(Codec<BlockStateConfiguration> param0) {
        super(param0);
    }

    @Override
    public boolean place(FeaturePlaceContext<BlockStateConfiguration> param0) {
        BlockPos var0 = param0.origin();
        WorldGenLevel var1 = param0.level();
        var0 = new BlockPos(var0.getX(), param0.chunkGenerator().getSeaLevel(), var0.getZ());
        RandomSource var2 = param0.random();
        boolean var3 = var2.nextDouble() > 0.7;
        BlockState var4 = param0.config().state;
        double var5 = var2.nextDouble() * 2.0 * Math.PI;
        int var6 = 11 - var2.nextInt(5);
        int var7 = 3 + var2.nextInt(3);
        boolean var8 = var2.nextDouble() > 0.7;
        int var9 = 11;
        int var10 = var8 ? var2.nextInt(6) + 6 : var2.nextInt(15) + 3;
        if (!var8 && var2.nextDouble() > 0.9) {
            var10 += var2.nextInt(19) + 7;
        }

        int var11 = Math.min(var10 + var2.nextInt(11), 18);
        int var12 = Math.min(var10 + var2.nextInt(7) - var2.nextInt(5), 11);
        int var13 = var8 ? var6 : 11;

        for(int var14 = -var13; var14 < var13; ++var14) {
            for(int var15 = -var13; var15 < var13; ++var15) {
                for(int var16 = 0; var16 < var10; ++var16) {
                    int var17 = var8 ? this.heightDependentRadiusEllipse(var16, var10, var12) : this.heightDependentRadiusRound(var2, var16, var10, var12);
                    if (var8 || var14 < var17) {
                        this.generateIcebergBlock(var1, var2, var0, var10, var14, var16, var15, var17, var13, var8, var7, var5, var3, var4);
                    }
                }
            }
        }

        this.smooth(var1, var0, var12, var10, var8, var6);

        for(int var18 = -var13; var18 < var13; ++var18) {
            for(int var19 = -var13; var19 < var13; ++var19) {
                for(int var20 = -1; var20 > -var11; --var20) {
                    int var21 = var8 ? Mth.ceil((float)var13 * (1.0F - (float)Math.pow((double)var20, 2.0) / ((float)var11 * 8.0F))) : var13;
                    int var22 = this.heightDependentRadiusSteep(var2, -var20, var11, var12);
                    if (var18 < var22) {
                        this.generateIcebergBlock(var1, var2, var0, var11, var18, var20, var19, var22, var21, var8, var7, var5, var3, var4);
                    }
                }
            }
        }

        boolean var23 = var8 ? var2.nextDouble() > 0.1 : var2.nextDouble() > 0.7;
        if (var23) {
            this.generateCutOut(var2, var1, var12, var10, var0, var8, var6, var5, var7);
        }

        return true;
    }

    private void generateCutOut(
        RandomSource param0, LevelAccessor param1, int param2, int param3, BlockPos param4, boolean param5, int param6, double param7, int param8
    ) {
        int var0 = param0.nextBoolean() ? -1 : 1;
        int var1 = param0.nextBoolean() ? -1 : 1;
        int var2 = param0.nextInt(Math.max(param2 / 2 - 2, 1));
        if (param0.nextBoolean()) {
            var2 = param2 / 2 + 1 - param0.nextInt(Math.max(param2 - param2 / 2 - 1, 1));
        }

        int var3 = param0.nextInt(Math.max(param2 / 2 - 2, 1));
        if (param0.nextBoolean()) {
            var3 = param2 / 2 + 1 - param0.nextInt(Math.max(param2 - param2 / 2 - 1, 1));
        }

        if (param5) {
            var2 = var3 = param0.nextInt(Math.max(param6 - 5, 1));
        }

        BlockPos var4 = new BlockPos(var0 * var2, 0, var1 * var3);
        double var5 = param5 ? param7 + (Math.PI / 2) : param0.nextDouble() * 2.0 * Math.PI;

        for(int var6 = 0; var6 < param3 - 3; ++var6) {
            int var7 = this.heightDependentRadiusRound(param0, var6, param3, param2);
            this.carve(var7, var6, param4, param1, false, var5, var4, param6, param8);
        }

        for(int var8 = -1; var8 > -param3 + param0.nextInt(5); --var8) {
            int var9 = this.heightDependentRadiusSteep(param0, -var8, param3, param2);
            this.carve(var9, var8, param4, param1, true, var5, var4, param6, param8);
        }

    }

    private void carve(int param0, int param1, BlockPos param2, LevelAccessor param3, boolean param4, double param5, BlockPos param6, int param7, int param8) {
        int var0 = param0 + 1 + param7 / 3;
        int var1 = Math.min(param0 - 3, 3) + param8 / 2 - 1;

        for(int var2 = -var0; var2 < var0; ++var2) {
            for(int var3 = -var0; var3 < var0; ++var3) {
                double var4 = this.signedDistanceEllipse(var2, var3, param6, var0, var1, param5);
                if (var4 < 0.0) {
                    BlockPos var5 = param2.offset(var2, param1, var3);
                    BlockState var6 = param3.getBlockState(var5);
                    if (isIcebergState(var6) || var6.is(Blocks.SNOW_BLOCK)) {
                        if (param4) {
                            this.setBlock(param3, var5, Blocks.WATER.defaultBlockState());
                        } else {
                            this.setBlock(param3, var5, Blocks.AIR.defaultBlockState());
                            this.removeFloatingSnowLayer(param3, var5);
                        }
                    }
                }
            }
        }

    }

    private void removeFloatingSnowLayer(LevelAccessor param0, BlockPos param1) {
        if (param0.getBlockState(param1.above()).is(Blocks.SNOW)) {
            this.setBlock(param0, param1.above(), Blocks.AIR.defaultBlockState());
        }

    }

    private void generateIcebergBlock(
        LevelAccessor param0,
        RandomSource param1,
        BlockPos param2,
        int param3,
        int param4,
        int param5,
        int param6,
        int param7,
        int param8,
        boolean param9,
        int param10,
        double param11,
        boolean param12,
        BlockState param13
    ) {
        double var0 = param9
            ? this.signedDistanceEllipse(param4, param6, BlockPos.ZERO, param8, this.getEllipseC(param5, param3, param10), param11)
            : this.signedDistanceCircle(param4, param6, BlockPos.ZERO, param7, param1);
        if (var0 < 0.0) {
            BlockPos var1 = param2.offset(param4, param5, param6);
            double var2 = param9 ? -0.5 : (double)(-6 - param1.nextInt(3));
            if (var0 > var2 && param1.nextDouble() > 0.9) {
                return;
            }

            this.setIcebergBlock(var1, param0, param1, param3 - param5, param3, param9, param12, param13);
        }

    }

    private void setIcebergBlock(
        BlockPos param0, LevelAccessor param1, RandomSource param2, int param3, int param4, boolean param5, boolean param6, BlockState param7
    ) {
        BlockState var0 = param1.getBlockState(param0);
        if (var0.isAir() || var0.is(Blocks.SNOW_BLOCK) || var0.is(Blocks.ICE) || var0.is(Blocks.WATER)) {
            boolean var1 = !param5 || param2.nextDouble() > 0.05;
            int var2 = param5 ? 3 : 2;
            if (param6 && !var0.is(Blocks.WATER) && (double)param3 <= (double)param2.nextInt(Math.max(1, param4 / var2)) + (double)param4 * 0.6 && var1) {
                this.setBlock(param1, param0, Blocks.SNOW_BLOCK.defaultBlockState());
            } else {
                this.setBlock(param1, param0, param7);
            }
        }

    }

    private int getEllipseC(int param0, int param1, int param2) {
        int var0 = param2;
        if (param0 > 0 && param1 - param0 <= 3) {
            var0 = param2 - (4 - (param1 - param0));
        }

        return var0;
    }

    private double signedDistanceCircle(int param0, int param1, BlockPos param2, int param3, RandomSource param4) {
        float var0 = 10.0F * Mth.clamp(param4.nextFloat(), 0.2F, 0.8F) / (float)param3;
        return (double)var0 + Math.pow((double)(param0 - param2.getX()), 2.0) + Math.pow((double)(param1 - param2.getZ()), 2.0) - Math.pow((double)param3, 2.0);
    }

    private double signedDistanceEllipse(int param0, int param1, BlockPos param2, int param3, int param4, double param5) {
        return Math.pow(((double)(param0 - param2.getX()) * Math.cos(param5) - (double)(param1 - param2.getZ()) * Math.sin(param5)) / (double)param3, 2.0)
            + Math.pow(((double)(param0 - param2.getX()) * Math.sin(param5) + (double)(param1 - param2.getZ()) * Math.cos(param5)) / (double)param4, 2.0)
            - 1.0;
    }

    private int heightDependentRadiusRound(RandomSource param0, int param1, int param2, int param3) {
        float var0 = 3.5F - param0.nextFloat();
        float var1 = (1.0F - (float)Math.pow((double)param1, 2.0) / ((float)param2 * var0)) * (float)param3;
        if (param2 > 15 + param0.nextInt(5)) {
            int var2 = param1 < 3 + param0.nextInt(6) ? param1 / 2 : param1;
            var1 = (1.0F - (float)var2 / ((float)param2 * var0 * 0.4F)) * (float)param3;
        }

        return Mth.ceil(var1 / 2.0F);
    }

    private int heightDependentRadiusEllipse(int param0, int param1, int param2) {
        float var0 = 1.0F;
        float var1 = (1.0F - (float)Math.pow((double)param0, 2.0) / ((float)param1 * 1.0F)) * (float)param2;
        return Mth.ceil(var1 / 2.0F);
    }

    private int heightDependentRadiusSteep(RandomSource param0, int param1, int param2, int param3) {
        float var0 = 1.0F + param0.nextFloat() / 2.0F;
        float var1 = (1.0F - (float)param1 / ((float)param2 * var0)) * (float)param3;
        return Mth.ceil(var1 / 2.0F);
    }

    private static boolean isIcebergState(BlockState param0) {
        return param0.is(Blocks.PACKED_ICE) || param0.is(Blocks.SNOW_BLOCK) || param0.is(Blocks.BLUE_ICE);
    }

    private boolean belowIsAir(BlockGetter param0, BlockPos param1) {
        return param0.getBlockState(param1.below()).isAir();
    }

    private void smooth(LevelAccessor param0, BlockPos param1, int param2, int param3, boolean param4, int param5) {
        int var0 = param4 ? param5 : param2 / 2;

        for(int var1 = -var0; var1 <= var0; ++var1) {
            for(int var2 = -var0; var2 <= var0; ++var2) {
                for(int var3 = 0; var3 <= param3; ++var3) {
                    BlockPos var4 = param1.offset(var1, var3, var2);
                    BlockState var5 = param0.getBlockState(var4);
                    if (isIcebergState(var5) || var5.is(Blocks.SNOW)) {
                        if (this.belowIsAir(param0, var4)) {
                            this.setBlock(param0, var4, Blocks.AIR.defaultBlockState());
                            this.setBlock(param0, var4.above(), Blocks.AIR.defaultBlockState());
                        } else if (isIcebergState(var5)) {
                            BlockState[] var6 = new BlockState[]{
                                param0.getBlockState(var4.west()),
                                param0.getBlockState(var4.east()),
                                param0.getBlockState(var4.north()),
                                param0.getBlockState(var4.south())
                            };
                            int var7 = 0;

                            for(BlockState var8 : var6) {
                                if (!isIcebergState(var8)) {
                                    ++var7;
                                }
                            }

                            if (var7 >= 3) {
                                this.setBlock(param0, var4, Blocks.AIR.defaultBlockState());
                            }
                        }
                    }
                }
            }
        }

    }
}
