package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.levelgen.feature.configurations.MegaTreeConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class DarkOakFeature extends AbstractTreeFeature<MegaTreeConfiguration> {
    public DarkOakFeature(Function<Dynamic<?>, ? extends MegaTreeConfiguration> param0, Function<Random, ? extends MegaTreeConfiguration> param1) {
        super(param0, param1);
    }

    public boolean doPlace(
        LevelSimulatedRW param0, Random param1, BlockPos param2, Set<BlockPos> param3, Set<BlockPos> param4, BoundingBox param5, MegaTreeConfiguration param6
    ) {
        int var0 = param1.nextInt(3) + param1.nextInt(2) + param6.baseHeight;
        int var1 = param2.getX();
        int var2 = param2.getY();
        int var3 = param2.getZ();
        if (var2 >= 1 && var2 + var0 + 1 < 256) {
            BlockPos var4 = param2.below();
            if (!isGrassOrDirt(param0, var4)) {
                return false;
            } else if (!this.canPlaceTreeOfHeight(param0, param2, var0)) {
                return false;
            } else {
                this.setDirtAt(param0, var4);
                this.setDirtAt(param0, var4.east());
                this.setDirtAt(param0, var4.south());
                this.setDirtAt(param0, var4.south().east());
                Direction var5 = Direction.Plane.HORIZONTAL.getRandomDirection(param1);
                int var6 = var0 - param1.nextInt(4);
                int var7 = 2 - param1.nextInt(3);
                int var8 = var1;
                int var9 = var3;
                int var10 = var2 + var0 - 1;

                for(int var11 = 0; var11 < var0; ++var11) {
                    if (var11 >= var6 && var7 > 0) {
                        var8 += var5.getStepX();
                        var9 += var5.getStepZ();
                        --var7;
                    }

                    int var12 = var2 + var11;
                    BlockPos var13 = new BlockPos(var8, var12, var9);
                    if (isAirOrLeaves(param0, var13)) {
                        this.placeLog(param0, param1, var13, param3, param5, param6);
                        this.placeLog(param0, param1, var13.east(), param3, param5, param6);
                        this.placeLog(param0, param1, var13.south(), param3, param5, param6);
                        this.placeLog(param0, param1, var13.east().south(), param3, param5, param6);
                    }
                }

                for(int var14 = -2; var14 <= 0; ++var14) {
                    for(int var15 = -2; var15 <= 0; ++var15) {
                        int var16 = -1;
                        this.placeLeaf(param0, param1, new BlockPos(var8 + var14, var10 + var16, var9 + var15), param4, param5, param6);
                        this.placeLeaf(param0, param1, new BlockPos(1 + var8 - var14, var10 + var16, var9 + var15), param4, param5, param6);
                        this.placeLeaf(param0, param1, new BlockPos(var8 + var14, var10 + var16, 1 + var9 - var15), param4, param5, param6);
                        this.placeLeaf(param0, param1, new BlockPos(1 + var8 - var14, var10 + var16, 1 + var9 - var15), param4, param5, param6);
                        if ((var14 > -2 || var15 > -1) && (var14 != -1 || var15 != -2)) {
                            int var31 = 1;
                            this.placeLeaf(param0, param1, new BlockPos(var8 + var14, var10 + var31, var9 + var15), param4, param5, param6);
                            this.placeLeaf(param0, param1, new BlockPos(1 + var8 - var14, var10 + var31, var9 + var15), param4, param5, param6);
                            this.placeLeaf(param0, param1, new BlockPos(var8 + var14, var10 + var31, 1 + var9 - var15), param4, param5, param6);
                            this.placeLeaf(param0, param1, new BlockPos(1 + var8 - var14, var10 + var31, 1 + var9 - var15), param4, param5, param6);
                        }
                    }
                }

                if (param1.nextBoolean()) {
                    this.placeLeaf(param0, param1, new BlockPos(var8, var10 + 2, var9), param4, param5, param6);
                    this.placeLeaf(param0, param1, new BlockPos(var8 + 1, var10 + 2, var9), param4, param5, param6);
                    this.placeLeaf(param0, param1, new BlockPos(var8 + 1, var10 + 2, var9 + 1), param4, param5, param6);
                    this.placeLeaf(param0, param1, new BlockPos(var8, var10 + 2, var9 + 1), param4, param5, param6);
                }

                for(int var17 = -3; var17 <= 4; ++var17) {
                    for(int var18 = -3; var18 <= 4; ++var18) {
                        if ((var17 != -3 || var18 != -3)
                            && (var17 != -3 || var18 != 4)
                            && (var17 != 4 || var18 != -3)
                            && (var17 != 4 || var18 != 4)
                            && (Math.abs(var17) < 3 || Math.abs(var18) < 3)) {
                            this.placeLeaf(param0, param1, new BlockPos(var8 + var17, var10, var9 + var18), param4, param5, param6);
                        }
                    }
                }

                for(int var19 = -1; var19 <= 2; ++var19) {
                    for(int var20 = -1; var20 <= 2; ++var20) {
                        if ((var19 < 0 || var19 > 1 || var20 < 0 || var20 > 1) && param1.nextInt(3) <= 0) {
                            int var21 = param1.nextInt(3) + 2;

                            for(int var22 = 0; var22 < var21; ++var22) {
                                this.placeLog(param0, param1, new BlockPos(var1 + var19, var10 - var22 - 1, var3 + var20), param3, param5, param6);
                            }

                            for(int var23 = -1; var23 <= 1; ++var23) {
                                for(int var24 = -1; var24 <= 1; ++var24) {
                                    this.placeLeaf(param0, param1, new BlockPos(var8 + var19 + var23, var10, var9 + var20 + var24), param4, param5, param6);
                                }
                            }

                            for(int var25 = -2; var25 <= 2; ++var25) {
                                for(int var26 = -2; var26 <= 2; ++var26) {
                                    if (Math.abs(var25) != 2 || Math.abs(var26) != 2) {
                                        this.placeLeaf(
                                            param0, param1, new BlockPos(var8 + var19 + var25, var10 - 1, var9 + var20 + var26), param4, param5, param6
                                        );
                                    }
                                }
                            }
                        }
                    }
                }

                return true;
            }
        } else {
            return false;
        }
    }

    private boolean canPlaceTreeOfHeight(LevelSimulatedReader param0, BlockPos param1, int param2) {
        int var0 = param1.getX();
        int var1 = param1.getY();
        int var2 = param1.getZ();
        BlockPos.MutableBlockPos var3 = new BlockPos.MutableBlockPos();

        for(int var4 = 0; var4 <= param2 + 1; ++var4) {
            int var5 = 1;
            if (var4 == 0) {
                var5 = 0;
            }

            if (var4 >= param2 - 1) {
                var5 = 2;
            }

            for(int var6 = -var5; var6 <= var5; ++var6) {
                for(int var7 = -var5; var7 <= var5; ++var7) {
                    if (!isFree(param0, var3.set(var0 + var6, var1 + var4, var2 + var7))) {
                        return false;
                    }
                }
            }
        }

        return true;
    }
}
