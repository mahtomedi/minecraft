package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.material.Material;

public class HugeFungusFeature extends Feature<HugeFungusConfiguration> {
    public HugeFungusFeature(Codec<HugeFungusConfiguration> param0) {
        super(param0);
    }

    @Override
    public boolean place(FeaturePlaceContext<HugeFungusConfiguration> param0) {
        WorldGenLevel var0 = param0.level();
        BlockPos var1 = param0.origin();
        Random var2 = param0.random();
        ChunkGenerator var3 = param0.chunkGenerator();
        HugeFungusConfiguration var4 = param0.config();
        Block var5 = var4.validBaseState.getBlock();
        BlockPos var6 = null;
        BlockState var7 = var0.getBlockState(var1.below());
        if (var7.is(var5)) {
            var6 = var1;
        }

        if (var6 == null) {
            return false;
        } else {
            int var8 = Mth.nextInt(var2, 4, 13);
            if (var2.nextInt(12) == 0) {
                var8 *= 2;
            }

            if (!var4.planted) {
                int var9 = var3.getGenDepth();
                if (var6.getY() + var8 + 1 >= var9) {
                    return false;
                }
            }

            boolean var10 = !var4.planted && var2.nextFloat() < 0.06F;
            var0.setBlock(var1, Blocks.AIR.defaultBlockState(), 4);
            this.placeStem(var0, var2, var4, var6, var8, var10);
            this.placeHat(var0, var2, var4, var6, var8, var10);
            return true;
        }
    }

    private static boolean isReplaceable(LevelAccessor param0, BlockPos param1, boolean param2) {
        return param0.isStateAtPosition(param1, param1x -> {
            Material var0x = param1x.getMaterial();
            return param1x.getMaterial().isReplaceable() || param2 && var0x == Material.PLANT;
        });
    }

    private void placeStem(LevelAccessor param0, Random param1, HugeFungusConfiguration param2, BlockPos param3, int param4, boolean param5) {
        BlockPos.MutableBlockPos var0 = new BlockPos.MutableBlockPos();
        BlockState var1 = param2.stemState;
        int var2 = param5 ? 1 : 0;

        for(int var3 = -var2; var3 <= var2; ++var3) {
            for(int var4 = -var2; var4 <= var2; ++var4) {
                boolean var5 = param5 && Mth.abs(var3) == var2 && Mth.abs(var4) == var2;

                for(int var6 = 0; var6 < param4; ++var6) {
                    var0.setWithOffset(param3, var3, var6, var4);
                    if (isReplaceable(param0, var0, true)) {
                        if (param2.planted) {
                            if (!param0.getBlockState(var0.below()).isAir()) {
                                param0.destroyBlock(var0, true);
                            }

                            param0.setBlock(var0, var1, 3);
                        } else if (var5) {
                            if (param1.nextFloat() < 0.1F) {
                                this.setBlock(param0, var0, var1);
                            }
                        } else {
                            this.setBlock(param0, var0, var1);
                        }
                    }
                }
            }
        }

    }

    private void placeHat(LevelAccessor param0, Random param1, HugeFungusConfiguration param2, BlockPos param3, int param4, boolean param5) {
        BlockPos.MutableBlockPos var0 = new BlockPos.MutableBlockPos();
        boolean var1 = param2.hatState.is(Blocks.NETHER_WART_BLOCK);
        int var2 = Math.min(param1.nextInt(1 + param4 / 3) + 5, param4);
        int var3 = param4 - var2;

        for(int var4 = var3; var4 <= param4; ++var4) {
            int var5 = var4 < param4 - param1.nextInt(3) ? 2 : 1;
            if (var2 > 8 && var4 < var3 + 4) {
                var5 = 3;
            }

            if (param5) {
                ++var5;
            }

            for(int var6 = -var5; var6 <= var5; ++var6) {
                for(int var7 = -var5; var7 <= var5; ++var7) {
                    boolean var8 = var6 == -var5 || var6 == var5;
                    boolean var9 = var7 == -var5 || var7 == var5;
                    boolean var10 = !var8 && !var9 && var4 != param4;
                    boolean var11 = var8 && var9;
                    boolean var12 = var4 < var3 + 3;
                    var0.setWithOffset(param3, var6, var4, var7);
                    if (isReplaceable(param0, var0, false)) {
                        if (param2.planted && !param0.getBlockState(var0.below()).isAir()) {
                            param0.destroyBlock(var0, true);
                        }

                        if (var12) {
                            if (!var10) {
                                this.placeHatDropBlock(param0, param1, var0, param2.hatState, var1);
                            }
                        } else if (var10) {
                            this.placeHatBlock(param0, param1, param2, var0, 0.1F, 0.2F, var1 ? 0.1F : 0.0F);
                        } else if (var11) {
                            this.placeHatBlock(param0, param1, param2, var0, 0.01F, 0.7F, var1 ? 0.083F : 0.0F);
                        } else {
                            this.placeHatBlock(param0, param1, param2, var0, 5.0E-4F, 0.98F, var1 ? 0.07F : 0.0F);
                        }
                    }
                }
            }
        }

    }

    private void placeHatBlock(
        LevelAccessor param0, Random param1, HugeFungusConfiguration param2, BlockPos.MutableBlockPos param3, float param4, float param5, float param6
    ) {
        if (param1.nextFloat() < param4) {
            this.setBlock(param0, param3, param2.decorState);
        } else if (param1.nextFloat() < param5) {
            this.setBlock(param0, param3, param2.hatState);
            if (param1.nextFloat() < param6) {
                tryPlaceWeepingVines(param3, param0, param1);
            }
        }

    }

    private void placeHatDropBlock(LevelAccessor param0, Random param1, BlockPos param2, BlockState param3, boolean param4) {
        if (param0.getBlockState(param2.below()).is(param3.getBlock())) {
            this.setBlock(param0, param2, param3);
        } else if ((double)param1.nextFloat() < 0.15) {
            this.setBlock(param0, param2, param3);
            if (param4 && param1.nextInt(11) == 0) {
                tryPlaceWeepingVines(param2, param0, param1);
            }
        }

    }

    private static void tryPlaceWeepingVines(BlockPos param0, LevelAccessor param1, Random param2) {
        BlockPos.MutableBlockPos var0 = param0.mutable().move(Direction.DOWN);
        if (param1.isEmptyBlock(var0)) {
            int var1 = Mth.nextInt(param2, 1, 5);
            if (param2.nextInt(7) == 0) {
                var1 *= 2;
            }

            int var2 = 23;
            int var3 = 25;
            WeepingVinesFeature.placeWeepingVinesColumn(param1, param2, var0, var1, 23, 25);
        }
    }
}
