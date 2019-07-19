package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class SpruceFeature extends AbstractTreeFeature<NoneFeatureConfiguration> {
    private static final BlockState TRUNK = Blocks.SPRUCE_LOG.defaultBlockState();
    private static final BlockState LEAF = Blocks.SPRUCE_LEAVES.defaultBlockState();

    public SpruceFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> param0, boolean param1) {
        super(param0, param1);
    }

    @Override
    public boolean doPlace(Set<BlockPos> param0, LevelSimulatedRW param1, Random param2, BlockPos param3, BoundingBox param4) {
        int var0 = param2.nextInt(4) + 6;
        int var1 = 1 + param2.nextInt(2);
        int var2 = var0 - var1;
        int var3 = 2 + param2.nextInt(2);
        boolean var4 = true;
        if (param3.getY() >= 1 && param3.getY() + var0 + 1 <= 256) {
            for(int var5 = param3.getY(); var5 <= param3.getY() + 1 + var0 && var4; ++var5) {
                int var6;
                if (var5 - param3.getY() < var1) {
                    var6 = 0;
                } else {
                    var6 = var3;
                }

                BlockPos.MutableBlockPos var8 = new BlockPos.MutableBlockPos();

                for(int var9 = param3.getX() - var6; var9 <= param3.getX() + var6 && var4; ++var9) {
                    for(int var10 = param3.getZ() - var6; var10 <= param3.getZ() + var6 && var4; ++var10) {
                        if (var5 >= 0 && var5 < 256) {
                            var8.set(var9, var5, var10);
                            if (!isAirOrLeaves(param1, var8)) {
                                var4 = false;
                            }
                        } else {
                            var4 = false;
                        }
                    }
                }
            }

            if (!var4) {
                return false;
            } else if (isGrassOrDirtOrFarmland(param1, param3.below()) && param3.getY() < 256 - var0 - 1) {
                this.setDirtAt(param1, param3.below());
                int var11 = param2.nextInt(2);
                int var12 = 1;
                int var13 = 0;

                for(int var14 = 0; var14 <= var2; ++var14) {
                    int var15 = param3.getY() + var0 - var14;

                    for(int var16 = param3.getX() - var11; var16 <= param3.getX() + var11; ++var16) {
                        int var17 = var16 - param3.getX();

                        for(int var18 = param3.getZ() - var11; var18 <= param3.getZ() + var11; ++var18) {
                            int var19 = var18 - param3.getZ();
                            if (Math.abs(var17) != var11 || Math.abs(var19) != var11 || var11 <= 0) {
                                BlockPos var20 = new BlockPos(var16, var15, var18);
                                if (isAirOrLeaves(param1, var20) || isReplaceablePlant(param1, var20)) {
                                    this.setBlock(param0, param1, var20, LEAF, param4);
                                }
                            }
                        }
                    }

                    if (var11 >= var12) {
                        var11 = var13;
                        var13 = 1;
                        if (++var12 > var3) {
                            var12 = var3;
                        }
                    } else {
                        ++var11;
                    }
                }

                int var21 = param2.nextInt(3);

                for(int var22 = 0; var22 < var0 - var21; ++var22) {
                    if (isAirOrLeaves(param1, param3.above(var22))) {
                        this.setBlock(param0, param1, param3.above(var22), TRUNK, param4);
                    }
                }

                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
