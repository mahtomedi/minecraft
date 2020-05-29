package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
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

    public boolean place(
        WorldGenLevel param0, StructureFeatureManager param1, ChunkGenerator param2, Random param3, BlockPos param4, HugeFungusConfiguration param5
    ) {
        Block var0 = param5.validBaseState.getBlock();
        BlockPos var1 = null;
        if (param5.planted) {
            Block var2 = param0.getBlockState(param4.below()).getBlock();
            if (var2 == var0) {
                var1 = param4;
            }
        } else {
            var1 = findOnNyliumPosition(param0, param4, var0);
        }

        if (var1 == null) {
            return false;
        } else {
            int var3 = Mth.nextInt(param3, 4, 13);
            if (param3.nextInt(12) == 0) {
                var3 *= 2;
            }

            if (!param5.planted) {
                int var4 = param0.getHeight();
                if (var1.getY() + var3 + 1 >= var4) {
                    return false;
                }
            }

            boolean var5 = !param5.planted && param3.nextFloat() < 0.06F;
            param0.setBlock(param4, Blocks.AIR.defaultBlockState(), 4);
            this.placeStem(param0, param3, param5, var1, var3, var5);
            this.placeHat(param0, param3, param5, var1, var3, var5);
            return true;
        }
    }

    private static boolean isReplaceable(LevelAccessor param0, BlockPos param1, boolean param2) {
        return param0.isStateAtPosition(
            param1,
            param1x -> {
                Material var0x = param1x.getMaterial();
                return param1x.isAir()
                    || param1x.is(Blocks.WATER)
                    || param1x.is(Blocks.LAVA)
                    || var0x == Material.REPLACEABLE_PLANT
                    || param2 && var0x == Material.PLANT;
            }
        );
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

    @Nullable
    private static BlockPos.MutableBlockPos findOnNyliumPosition(LevelAccessor param0, BlockPos param1, Block param2) {
        BlockPos.MutableBlockPos var0 = param1.mutable();

        for(int var1 = param1.getY(); var1 >= 1; --var1) {
            var0.setY(var1);
            Block var2 = param0.getBlockState(var0.below()).getBlock();
            if (var2 == param2) {
                return var0;
            }
        }

        return null;
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
