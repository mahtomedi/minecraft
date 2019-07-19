package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.LevelWriter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class SavannaTreeFeature extends AbstractTreeFeature<NoneFeatureConfiguration> {
    private static final BlockState TRUNK = Blocks.ACACIA_LOG.defaultBlockState();
    private static final BlockState LEAF = Blocks.ACACIA_LEAVES.defaultBlockState();

    public SavannaTreeFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> param0, boolean param1) {
        super(param0, param1);
    }

    @Override
    public boolean doPlace(Set<BlockPos> param0, LevelSimulatedRW param1, Random param2, BlockPos param3, BoundingBox param4) {
        int var0 = param2.nextInt(3) + param2.nextInt(3) + 5;
        boolean var1 = true;
        if (param3.getY() >= 1 && param3.getY() + var0 + 1 <= 256) {
            for(int var2 = param3.getY(); var2 <= param3.getY() + 1 + var0; ++var2) {
                int var3 = 1;
                if (var2 == param3.getY()) {
                    var3 = 0;
                }

                if (var2 >= param3.getY() + 1 + var0 - 2) {
                    var3 = 2;
                }

                BlockPos.MutableBlockPos var4 = new BlockPos.MutableBlockPos();

                for(int var5 = param3.getX() - var3; var5 <= param3.getX() + var3 && var1; ++var5) {
                    for(int var6 = param3.getZ() - var3; var6 <= param3.getZ() + var3 && var1; ++var6) {
                        if (var2 < 0 || var2 >= 256) {
                            var1 = false;
                        } else if (!isFree(param1, var4.set(var5, var2, var6))) {
                            var1 = false;
                        }
                    }
                }
            }

            if (!var1) {
                return false;
            } else if (isGrassOrDirt(param1, param3.below()) && param3.getY() < 256 - var0 - 1) {
                this.setDirtAt(param1, param3.below());
                Direction var7 = Direction.Plane.HORIZONTAL.getRandomDirection(param2);
                int var8 = var0 - param2.nextInt(4) - 1;
                int var9 = 3 - param2.nextInt(3);
                int var10 = param3.getX();
                int var11 = param3.getZ();
                int var12 = 0;

                for(int var13 = 0; var13 < var0; ++var13) {
                    int var14 = param3.getY() + var13;
                    if (var13 >= var8 && var9 > 0) {
                        var10 += var7.getStepX();
                        var11 += var7.getStepZ();
                        --var9;
                    }

                    BlockPos var15 = new BlockPos(var10, var14, var11);
                    if (isAirOrLeaves(param1, var15)) {
                        this.placeLogAt(param0, param1, var15, param4);
                        var12 = var14;
                    }
                }

                BlockPos var16 = new BlockPos(var10, var12, var11);

                for(int var17 = -3; var17 <= 3; ++var17) {
                    for(int var18 = -3; var18 <= 3; ++var18) {
                        if (Math.abs(var17) != 3 || Math.abs(var18) != 3) {
                            this.placeLeafAt(param0, param1, var16.offset(var17, 0, var18), param4);
                        }
                    }
                }

                var16 = var16.above();

                for(int var19 = -1; var19 <= 1; ++var19) {
                    for(int var20 = -1; var20 <= 1; ++var20) {
                        this.placeLeafAt(param0, param1, var16.offset(var19, 0, var20), param4);
                    }
                }

                this.placeLeafAt(param0, param1, var16.east(2), param4);
                this.placeLeafAt(param0, param1, var16.west(2), param4);
                this.placeLeafAt(param0, param1, var16.south(2), param4);
                this.placeLeafAt(param0, param1, var16.north(2), param4);
                var10 = param3.getX();
                var11 = param3.getZ();
                Direction var21 = Direction.Plane.HORIZONTAL.getRandomDirection(param2);
                if (var21 != var7) {
                    int var22 = var8 - param2.nextInt(2) - 1;
                    int var23 = 1 + param2.nextInt(3);
                    var12 = 0;

                    for(int var24 = var22; var24 < var0 && var23 > 0; --var23) {
                        if (var24 >= 1) {
                            int var25 = param3.getY() + var24;
                            var10 += var21.getStepX();
                            var11 += var21.getStepZ();
                            BlockPos var26 = new BlockPos(var10, var25, var11);
                            if (isAirOrLeaves(param1, var26)) {
                                this.placeLogAt(param0, param1, var26, param4);
                                var12 = var25;
                            }
                        }

                        ++var24;
                    }

                    if (var12 > 0) {
                        BlockPos var27 = new BlockPos(var10, var12, var11);

                        for(int var28 = -2; var28 <= 2; ++var28) {
                            for(int var29 = -2; var29 <= 2; ++var29) {
                                if (Math.abs(var28) != 2 || Math.abs(var29) != 2) {
                                    this.placeLeafAt(param0, param1, var27.offset(var28, 0, var29), param4);
                                }
                            }
                        }

                        var27 = var27.above();

                        for(int var30 = -1; var30 <= 1; ++var30) {
                            for(int var31 = -1; var31 <= 1; ++var31) {
                                this.placeLeafAt(param0, param1, var27.offset(var30, 0, var31), param4);
                            }
                        }
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

    private void placeLogAt(Set<BlockPos> param0, LevelWriter param1, BlockPos param2, BoundingBox param3) {
        this.setBlock(param0, param1, param2, TRUNK, param3);
    }

    private void placeLeafAt(Set<BlockPos> param0, LevelSimulatedRW param1, BlockPos param2, BoundingBox param3) {
        if (isAirOrLeaves(param1, param2)) {
            this.setBlock(param0, param1, param2, LEAF, param3);
        }

    }
}
