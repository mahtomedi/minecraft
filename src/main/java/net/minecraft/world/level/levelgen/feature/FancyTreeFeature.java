package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Lists;
import com.mojang.datafixers.Dynamic;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.block.LogBlock;
import net.minecraft.world.level.levelgen.feature.configurations.SmallTreeConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class FancyTreeFeature extends AbstractTreeFeature<SmallTreeConfiguration> {
    public FancyTreeFeature(Function<Dynamic<?>, ? extends SmallTreeConfiguration> param0) {
        super(param0);
    }

    private void crossSection(
        LevelSimulatedRW param0, Random param1, BlockPos param2, float param3, Set<BlockPos> param4, BoundingBox param5, SmallTreeConfiguration param6
    ) {
        int var0 = (int)((double)param3 + 0.618);

        for(int var1 = -var0; var1 <= var0; ++var1) {
            for(int var2 = -var0; var2 <= var0; ++var2) {
                if (Math.pow((double)Math.abs(var1) + 0.5, 2.0) + Math.pow((double)Math.abs(var2) + 0.5, 2.0) <= (double)(param3 * param3)) {
                    this.placeLeaf(param0, param1, param2.offset(var1, 0, var2), param4, param5, param6);
                }
            }
        }

    }

    private float treeShape(int param0, int param1) {
        if ((float)param1 < (float)param0 * 0.3F) {
            return -1.0F;
        } else {
            float var0 = (float)param0 / 2.0F;
            float var1 = var0 - (float)param1;
            float var2 = Mth.sqrt(var0 * var0 - var1 * var1);
            if (var1 == 0.0F) {
                var2 = var0;
            } else if (Math.abs(var1) >= var0) {
                return 0.0F;
            }

            return var2 * 0.5F;
        }
    }

    private float foliageShape(int param0) {
        if (param0 < 0 || param0 >= 5) {
            return -1.0F;
        } else {
            return param0 != 0 && param0 != 4 ? 3.0F : 2.0F;
        }
    }

    private void foliageCluster(
        LevelSimulatedRW param0, Random param1, BlockPos param2, Set<BlockPos> param3, BoundingBox param4, SmallTreeConfiguration param5
    ) {
        for(int var0 = 0; var0 < 5; ++var0) {
            this.crossSection(param0, param1, param2.above(var0), this.foliageShape(var0), param3, param4, param5);
        }

    }

    private int makeLimb(
        LevelSimulatedRW param0,
        Random param1,
        BlockPos param2,
        BlockPos param3,
        boolean param4,
        Set<BlockPos> param5,
        BoundingBox param6,
        SmallTreeConfiguration param7
    ) {
        if (!param4 && Objects.equals(param2, param3)) {
            return -1;
        } else {
            BlockPos var0 = param3.offset(-param2.getX(), -param2.getY(), -param2.getZ());
            int var1 = this.getSteps(var0);
            float var2 = (float)var0.getX() / (float)var1;
            float var3 = (float)var0.getY() / (float)var1;
            float var4 = (float)var0.getZ() / (float)var1;

            for(int var5 = 0; var5 <= var1; ++var5) {
                BlockPos var6 = param2.offset((double)(0.5F + (float)var5 * var2), (double)(0.5F + (float)var5 * var3), (double)(0.5F + (float)var5 * var4));
                if (param4) {
                    this.setBlock(param0, var6, param7.trunkProvider.getState(param1, var6).setValue(LogBlock.AXIS, this.getLogAxis(param2, var6)), param6);
                    param5.add(var6);
                } else if (!isFree(param0, var6)) {
                    return var5;
                }
            }

            return -1;
        }
    }

    private int getSteps(BlockPos param0) {
        int var0 = Mth.abs(param0.getX());
        int var1 = Mth.abs(param0.getY());
        int var2 = Mth.abs(param0.getZ());
        if (var2 > var0 && var2 > var1) {
            return var2;
        } else {
            return var1 > var0 ? var1 : var0;
        }
    }

    private Direction.Axis getLogAxis(BlockPos param0, BlockPos param1) {
        Direction.Axis var0 = Direction.Axis.Y;
        int var1 = Math.abs(param1.getX() - param0.getX());
        int var2 = Math.abs(param1.getZ() - param0.getZ());
        int var3 = Math.max(var1, var2);
        if (var3 > 0) {
            if (var1 == var3) {
                var0 = Direction.Axis.X;
            } else if (var2 == var3) {
                var0 = Direction.Axis.Z;
            }
        }

        return var0;
    }

    private void makeFoliage(
        LevelSimulatedRW param0,
        Random param1,
        int param2,
        BlockPos param3,
        List<FancyTreeFeature.FoliageCoords> param4,
        Set<BlockPos> param5,
        BoundingBox param6,
        SmallTreeConfiguration param7
    ) {
        for(FancyTreeFeature.FoliageCoords var0 : param4) {
            if (this.trimBranches(param2, var0.getBranchBase() - param3.getY())) {
                this.foliageCluster(param0, param1, var0, param5, param6, param7);
            }
        }

    }

    private boolean trimBranches(int param0, int param1) {
        return (double)param1 >= (double)param0 * 0.2;
    }

    private void makeTrunk(
        LevelSimulatedRW param0, Random param1, BlockPos param2, int param3, Set<BlockPos> param4, BoundingBox param5, SmallTreeConfiguration param6
    ) {
        this.makeLimb(param0, param1, param2, param2.above(param3), true, param4, param5, param6);
    }

    private void makeBranches(
        LevelSimulatedRW param0,
        Random param1,
        int param2,
        BlockPos param3,
        List<FancyTreeFeature.FoliageCoords> param4,
        Set<BlockPos> param5,
        BoundingBox param6,
        SmallTreeConfiguration param7
    ) {
        for(FancyTreeFeature.FoliageCoords var0 : param4) {
            int var1 = var0.getBranchBase();
            BlockPos var2 = new BlockPos(param3.getX(), var1, param3.getZ());
            if (!var2.equals(var0) && this.trimBranches(param2, var1 - param3.getY())) {
                this.makeLimb(param0, param1, var2, var0, true, param5, param6, param7);
            }
        }

    }

    public boolean doPlace(
        LevelSimulatedRW param0, Random param1, BlockPos param2, Set<BlockPos> param3, Set<BlockPos> param4, BoundingBox param5, SmallTreeConfiguration param6
    ) {
        Random var0 = new Random(param1.nextLong());
        int var1 = this.checkLocation(param0, param1, param2, 5 + var0.nextInt(12), param3, param5, param6);
        if (var1 == -1) {
            return false;
        } else {
            this.setDirtAt(param0, param2.below());
            int var2 = (int)((double)var1 * 0.618);
            if (var2 >= var1) {
                var2 = var1 - 1;
            }

            double var3 = 1.0;
            int var4 = (int)(1.382 + Math.pow(1.0 * (double)var1 / 13.0, 2.0));
            if (var4 < 1) {
                var4 = 1;
            }

            int var5 = param2.getY() + var2;
            int var6 = var1 - 5;
            List<FancyTreeFeature.FoliageCoords> var7 = Lists.newArrayList();
            var7.add(new FancyTreeFeature.FoliageCoords(param2.above(var6), var5));

            for(; var6 >= 0; --var6) {
                float var8 = this.treeShape(var1, var6);
                if (!(var8 < 0.0F)) {
                    for(int var9 = 0; var9 < var4; ++var9) {
                        double var10 = 1.0;
                        double var11 = 1.0 * (double)var8 * ((double)var0.nextFloat() + 0.328);
                        double var12 = (double)(var0.nextFloat() * 2.0F) * Math.PI;
                        double var13 = var11 * Math.sin(var12) + 0.5;
                        double var14 = var11 * Math.cos(var12) + 0.5;
                        BlockPos var15 = param2.offset(var13, (double)(var6 - 1), var14);
                        BlockPos var16 = var15.above(5);
                        if (this.makeLimb(param0, param1, var15, var16, false, param3, param5, param6) == -1) {
                            int var17 = param2.getX() - var15.getX();
                            int var18 = param2.getZ() - var15.getZ();
                            double var19 = (double)var15.getY() - Math.sqrt((double)(var17 * var17 + var18 * var18)) * 0.381;
                            int var20 = var19 > (double)var5 ? var5 : (int)var19;
                            BlockPos var21 = new BlockPos(param2.getX(), var20, param2.getZ());
                            if (this.makeLimb(param0, param1, var21, var15, false, param3, param5, param6) == -1) {
                                var7.add(new FancyTreeFeature.FoliageCoords(var15, var21.getY()));
                            }
                        }
                    }
                }
            }

            this.makeFoliage(param0, param1, var1, param2, var7, param4, param5, param6);
            this.makeTrunk(param0, param1, param2, var2, param3, param5, param6);
            this.makeBranches(param0, param1, var1, param2, var7, param3, param5, param6);
            return true;
        }
    }

    private int checkLocation(
        LevelSimulatedRW param0, Random param1, BlockPos param2, int param3, Set<BlockPos> param4, BoundingBox param5, SmallTreeConfiguration param6
    ) {
        if (!isGrassOrDirtOrFarmland(param0, param2.below())) {
            return -1;
        } else {
            int var0 = this.makeLimb(param0, param1, param2, param2.above(param3 - 1), false, param4, param5, param6);
            if (var0 == -1) {
                return param3;
            } else {
                return var0 < 6 ? -1 : var0;
            }
        }
    }

    static class FoliageCoords extends BlockPos {
        private final int branchBase;

        public FoliageCoords(BlockPos param0, int param1) {
            super(param0.getX(), param0.getY(), param0.getZ());
            this.branchBase = param1;
        }

        public int getBranchBase() {
            return this.branchBase;
        }
    }
}
