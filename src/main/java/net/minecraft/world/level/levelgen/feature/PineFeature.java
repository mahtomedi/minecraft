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

public class PineFeature extends AbstractTreeFeature<NoneFeatureConfiguration> {
    private static final BlockState TRUNK = Blocks.SPRUCE_LOG.defaultBlockState();
    private static final BlockState LEAF = Blocks.SPRUCE_LEAVES.defaultBlockState();

    public PineFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> param0) {
        super(param0, false);
    }

    @Override
    public boolean doPlace(Set<BlockPos> param0, LevelSimulatedRW param1, Random param2, BlockPos param3, BoundingBox param4) {
        int var0 = param2.nextInt(5) + 7;
        int var1 = var0 - param2.nextInt(2) - 3;
        int var2 = var0 - var1;
        int var3 = 1 + param2.nextInt(var2 + 1);
        if (param3.getY() >= 1 && param3.getY() + var0 + 1 <= 256) {
            boolean var4 = true;

            for(int var5 = param3.getY(); var5 <= param3.getY() + 1 + var0 && var4; ++var5) {
                int var6 = 1;
                if (var5 - param3.getY() < var1) {
                    var6 = 0;
                } else {
                    var6 = var3;
                }

                BlockPos.MutableBlockPos var7 = new BlockPos.MutableBlockPos();

                for(int var8 = param3.getX() - var6; var8 <= param3.getX() + var6 && var4; ++var8) {
                    for(int var9 = param3.getZ() - var6; var9 <= param3.getZ() + var6 && var4; ++var9) {
                        if (var5 < 0 || var5 >= 256) {
                            var4 = false;
                        } else if (!isFree(param1, var7.set(var8, var5, var9))) {
                            var4 = false;
                        }
                    }
                }
            }

            if (!var4) {
                return false;
            } else if (isGrassOrDirt(param1, param3.below()) && param3.getY() < 256 - var0 - 1) {
                this.setDirtAt(param1, param3.below());
                int var10 = 0;

                for(int var11 = param3.getY() + var0; var11 >= param3.getY() + var1; --var11) {
                    for(int var12 = param3.getX() - var10; var12 <= param3.getX() + var10; ++var12) {
                        int var13 = var12 - param3.getX();

                        for(int var14 = param3.getZ() - var10; var14 <= param3.getZ() + var10; ++var14) {
                            int var15 = var14 - param3.getZ();
                            if (Math.abs(var13) != var10 || Math.abs(var15) != var10 || var10 <= 0) {
                                BlockPos var16 = new BlockPos(var12, var11, var14);
                                if (isAirOrLeaves(param1, var16)) {
                                    this.setBlock(param0, param1, var16, LEAF, param4);
                                }
                            }
                        }
                    }

                    if (var10 >= 1 && var11 == param3.getY() + var1 + 1) {
                        --var10;
                    } else if (var10 < var3) {
                        ++var10;
                    }
                }

                for(int var17 = 0; var17 < var0 - 1; ++var17) {
                    if (isAirOrLeaves(param1, param3.above(var17))) {
                        this.setBlock(param0, param1, param3.above(var17), TRUNK, param4);
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
