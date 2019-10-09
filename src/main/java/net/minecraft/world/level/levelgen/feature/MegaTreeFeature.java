package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.levelgen.feature.configurations.MegaTreeConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public abstract class MegaTreeFeature<T extends TreeConfiguration> extends AbstractTreeFeature<T> {
    public MegaTreeFeature(Function<Dynamic<?>, ? extends T> param0) {
        super(param0);
    }

    protected int calcTreeHeigth(Random param0, MegaTreeConfiguration param1) {
        int var0 = param0.nextInt(3) + param1.baseHeight;
        if (param1.heightInterval > 1) {
            var0 += param0.nextInt(param1.heightInterval);
        }

        return var0;
    }

    private boolean checkIsFree(LevelSimulatedReader param0, BlockPos param1, int param2) {
        boolean var0 = true;
        if (param1.getY() >= 1 && param1.getY() + param2 + 1 <= 256) {
            for(int var1 = 0; var1 <= 1 + param2; ++var1) {
                int var2 = 2;
                if (var1 == 0) {
                    var2 = 1;
                } else if (var1 >= 1 + param2 - 2) {
                    var2 = 2;
                }

                for(int var3 = -var2; var3 <= var2 && var0; ++var3) {
                    for(int var4 = -var2; var4 <= var2 && var0; ++var4) {
                        if (param1.getY() + var1 < 0 || param1.getY() + var1 >= 256 || !isFree(param0, param1.offset(var3, var1, var4))) {
                            var0 = false;
                        }
                    }
                }
            }

            return var0;
        } else {
            return false;
        }
    }

    private boolean makeDirtFloor(LevelSimulatedRW param0, BlockPos param1) {
        BlockPos var0 = param1.below();
        if (isGrassOrDirt(param0, var0) && param1.getY() >= 2) {
            this.setDirtAt(param0, var0);
            this.setDirtAt(param0, var0.east());
            this.setDirtAt(param0, var0.south());
            this.setDirtAt(param0, var0.south().east());
            return true;
        } else {
            return false;
        }
    }

    protected boolean prepareTree(LevelSimulatedRW param0, BlockPos param1, int param2) {
        return this.checkIsFree(param0, param1, param2) && this.makeDirtFloor(param0, param1);
    }

    protected void placeDoubleTrunkLeaves(
        LevelSimulatedRW param0, Random param1, BlockPos param2, int param3, Set<BlockPos> param4, BoundingBox param5, TreeConfiguration param6
    ) {
        int var0 = param3 * param3;

        for(int var1 = -param3; var1 <= param3 + 1; ++var1) {
            for(int var2 = -param3; var2 <= param3 + 1; ++var2) {
                int var3 = Math.min(Math.abs(var1), Math.abs(var1 - 1));
                int var4 = Math.min(Math.abs(var2), Math.abs(var2 - 1));
                if (var3 + var4 < 7 && var3 * var3 + var4 * var4 <= var0) {
                    this.placeLeaf(param0, param1, param2.offset(var1, 0, var2), param4, param5, param6);
                }
            }
        }

    }

    protected void placeSingleTrunkLeaves(
        LevelSimulatedRW param0, Random param1, BlockPos param2, int param3, Set<BlockPos> param4, BoundingBox param5, TreeConfiguration param6
    ) {
        int var0 = param3 * param3;

        for(int var1 = -param3; var1 <= param3; ++var1) {
            for(int var2 = -param3; var2 <= param3; ++var2) {
                if (var1 * var1 + var2 * var2 <= var0) {
                    this.placeLeaf(param0, param1, param2.offset(var1, 0, var2), param4, param5, param6);
                }
            }
        }

    }

    protected void placeTrunk(
        LevelSimulatedRW param0, Random param1, BlockPos param2, int param3, Set<BlockPos> param4, BoundingBox param5, MegaTreeConfiguration param6
    ) {
        BlockPos.MutableBlockPos var0 = new BlockPos.MutableBlockPos();

        for(int var1 = 0; var1 < param3; ++var1) {
            var0.set(param2).move(0, var1, 0);
            if (isFree(param0, var0)) {
                this.placeLog(param0, param1, var0, param4, param5, param6);
            }

            if (var1 < param3 - 1) {
                var0.set(param2).move(1, var1, 0);
                if (isFree(param0, var0)) {
                    this.placeLog(param0, param1, var0, param4, param5, param6);
                }

                var0.set(param2).move(1, var1, 1);
                if (isFree(param0, var0)) {
                    this.placeLog(param0, param1, var0, param4, param5, param6);
                }

                var0.set(param2).move(0, var1, 1);
                if (isFree(param0, var0)) {
                    this.placeLog(param0, param1, var0, param4, param5, param6);
                }
            }
        }

    }
}
