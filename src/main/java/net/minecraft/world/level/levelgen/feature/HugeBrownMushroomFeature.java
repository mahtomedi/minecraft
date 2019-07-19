package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HugeMushroomBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;

public class HugeBrownMushroomFeature extends Feature<HugeMushroomFeatureConfig> {
    public HugeBrownMushroomFeature(Function<Dynamic<?>, ? extends HugeMushroomFeatureConfig> param0) {
        super(param0);
    }

    public boolean place(
        LevelAccessor param0, ChunkGenerator<? extends ChunkGeneratorSettings> param1, Random param2, BlockPos param3, HugeMushroomFeatureConfig param4
    ) {
        int var0 = param2.nextInt(3) + 4;
        if (param2.nextInt(12) == 0) {
            var0 *= 2;
        }

        int var1 = param3.getY();
        if (var1 >= 1 && var1 + var0 + 1 < 256) {
            Block var2 = param0.getBlockState(param3.below()).getBlock();
            if (!Block.equalsDirt(var2) && var2 != Blocks.GRASS_BLOCK && var2 != Blocks.MYCELIUM) {
                return false;
            } else {
                BlockPos.MutableBlockPos var3 = new BlockPos.MutableBlockPos();

                for(int var4 = 0; var4 <= 1 + var0; ++var4) {
                    int var5 = var4 <= 3 ? 0 : 3;

                    for(int var6 = -var5; var6 <= var5; ++var6) {
                        for(int var7 = -var5; var7 <= var5; ++var7) {
                            BlockState var8 = param0.getBlockState(var3.set(param3).move(var6, var4, var7));
                            if (!var8.isAir() && !var8.is(BlockTags.LEAVES)) {
                                return false;
                            }
                        }
                    }
                }

                BlockState var9 = Blocks.BROWN_MUSHROOM_BLOCK
                    .defaultBlockState()
                    .setValue(HugeMushroomBlock.UP, Boolean.valueOf(true))
                    .setValue(HugeMushroomBlock.DOWN, Boolean.valueOf(false));
                int var10 = 3;

                for(int var11 = -3; var11 <= 3; ++var11) {
                    for(int var12 = -3; var12 <= 3; ++var12) {
                        boolean var13 = var11 == -3;
                        boolean var14 = var11 == 3;
                        boolean var15 = var12 == -3;
                        boolean var16 = var12 == 3;
                        boolean var17 = var13 || var14;
                        boolean var18 = var15 || var16;
                        if (!var17 || !var18) {
                            var3.set(param3).move(var11, var0, var12);
                            if (!param0.getBlockState(var3).isSolidRender(param0, var3)) {
                                boolean var19 = var13 || var18 && var11 == -2;
                                boolean var20 = var14 || var18 && var11 == 2;
                                boolean var21 = var15 || var17 && var12 == -2;
                                boolean var22 = var16 || var17 && var12 == 2;
                                this.setBlock(
                                    param0,
                                    var3,
                                    var9.setValue(HugeMushroomBlock.WEST, Boolean.valueOf(var19))
                                        .setValue(HugeMushroomBlock.EAST, Boolean.valueOf(var20))
                                        .setValue(HugeMushroomBlock.NORTH, Boolean.valueOf(var21))
                                        .setValue(HugeMushroomBlock.SOUTH, Boolean.valueOf(var22))
                                );
                            }
                        }
                    }
                }

                BlockState var23 = Blocks.MUSHROOM_STEM
                    .defaultBlockState()
                    .setValue(HugeMushroomBlock.UP, Boolean.valueOf(false))
                    .setValue(HugeMushroomBlock.DOWN, Boolean.valueOf(false));

                for(int var24 = 0; var24 < var0; ++var24) {
                    var3.set(param3).move(Direction.UP, var24);
                    if (!param0.getBlockState(var3).isSolidRender(param0, var3)) {
                        if (param4.planted) {
                            param0.setBlock(var3, var23, 3);
                        } else {
                            this.setBlock(param0, var3, var23);
                        }
                    }
                }

                return true;
            }
        } else {
            return false;
        }
    }
}
