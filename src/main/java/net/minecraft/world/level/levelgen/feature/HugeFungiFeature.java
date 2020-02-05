package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;

public class HugeFungiFeature extends Feature<HugeFungiConfiguration> {
    public HugeFungiFeature(Function<Dynamic<?>, ? extends HugeFungiConfiguration> param0) {
        super(param0);
    }

    public boolean place(
        LevelAccessor param0, ChunkGenerator<? extends ChunkGeneratorSettings> param1, Random param2, BlockPos param3, HugeFungiConfiguration param4
    ) {
        BlockPos.MutableBlockPos var0 = findOnNyliumPosition(param0, param3);
        if (var0 == null) {
            return false;
        } else {
            int var1 = Mth.nextInt(param2, 4, 13);
            if (param2.nextInt(12) == 0) {
                var1 *= 2;
            }

            if (var0.getY() + var1 + 1 >= 256) {
                return false;
            } else {
                boolean var2 = !param4.planted && param2.nextFloat() < 0.06F;
                this.placeHat(param0, param2, param4, var0, var1, var2);
                this.placeStem(param0, param2, param4, var0, var1, var2);
                return true;
            }
        }
    }

    private void placeStem(LevelAccessor param0, Random param1, HugeFungiConfiguration param2, BlockPos.MutableBlockPos param3, int param4, boolean param5) {
        BlockPos.MutableBlockPos var0 = new BlockPos.MutableBlockPos();
        BlockState var1 = param2.stemState;
        int var2 = param5 ? 1 : 0;

        for(int var3 = -var2; var3 <= var2; ++var3) {
            for(int var4 = -var2; var4 <= var2; ++var4) {
                boolean var5 = param5 && Mth.abs(var3) == var2 && Mth.abs(var4) == var2;

                for(int var6 = 0; var6 < param4; ++var6) {
                    var0.set(param3).move(var3, var6, var4);
                    if (!param0.getBlockState(var0).isSolidRender(param0, var0)) {
                        if (param2.planted) {
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

    private void placeHat(LevelAccessor param0, Random param1, HugeFungiConfiguration param2, BlockPos.MutableBlockPos param3, int param4, boolean param5) {
        BlockPos.MutableBlockPos var0 = new BlockPos.MutableBlockPos();
        boolean var1 = param2.hatState.getBlock() == Blocks.NETHER_WART_BLOCK;
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
                    var0.set(param3).move(var6, var4, var7);
                    if (!param0.getBlockState(var0).isSolidRender(param0, var0)) {
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
        LevelAccessor param0, Random param1, HugeFungiConfiguration param2, BlockPos.MutableBlockPos param3, float param4, float param5, float param6
    ) {
        if (param1.nextFloat() < param4) {
            this.setBlock(param0, param3, param2.decorState);
        } else if (param1.nextFloat() < param5) {
            this.setBlock(param0, param3, param2.hatState);
            if (param1.nextFloat() < param6) {
                this.tryPlaceWeepingVines(param3, param0, param1);
            }
        }

    }

    private void placeHatDropBlock(LevelAccessor param0, Random param1, BlockPos param2, BlockState param3, boolean param4) {
        if (param0.getBlockState(param2.below()).getBlock() == param3.getBlock()) {
            this.setBlock(param0, param2, param3);
        } else if ((double)param1.nextFloat() < 0.15) {
            this.setBlock(param0, param2, param3);
            if (param4 && param1.nextInt(11) == 0) {
                this.tryPlaceWeepingVines(param2, param0, param1);
            }
        }

    }

    @Nullable
    private static BlockPos.MutableBlockPos findOnNyliumPosition(LevelAccessor param0, BlockPos param1) {
        BlockPos.MutableBlockPos var0 = new BlockPos.MutableBlockPos(param1);

        for(int var1 = param1.getY(); var1 >= 1; --var1) {
            var0.setY(var1);
            Block var2 = param0.getBlockState(var0.below()).getBlock();
            if (var2.is(BlockTags.NYLIUM)) {
                return var0;
            }
        }

        return null;
    }

    private void tryPlaceWeepingVines(BlockPos param0, LevelAccessor param1, Random param2) {
        BlockPos.MutableBlockPos var0 = new BlockPos.MutableBlockPos(param0).move(Direction.DOWN);
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
