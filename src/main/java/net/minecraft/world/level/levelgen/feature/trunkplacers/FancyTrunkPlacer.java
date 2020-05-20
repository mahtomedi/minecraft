package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class FancyTrunkPlacer extends TrunkPlacer {
    public static final Codec<FancyTrunkPlacer> CODEC = RecordCodecBuilder.create(param0 -> trunkPlacerParts(param0).apply(param0, FancyTrunkPlacer::new));

    public FancyTrunkPlacer(int param0, int param1, int param2) {
        super(param0, param1, param2);
    }

    @Override
    protected TrunkPlacerType<?> type() {
        return TrunkPlacerType.FANCY_TRUNK_PLACER;
    }

    @Override
    public List<FoliagePlacer.FoliageAttachment> placeTrunk(
        LevelSimulatedRW param0, Random param1, int param2, BlockPos param3, Set<BlockPos> param4, BoundingBox param5, TreeConfiguration param6
    ) {
        int var0 = 5;
        int var1 = param2 + 2;
        int var2 = Mth.floor((double)var1 * 0.618);
        setDirtAt(param0, param3.below());
        double var3 = 1.0;
        int var4 = Math.min(1, Mth.floor(1.382 + Math.pow(1.0 * (double)var1 / 13.0, 2.0)));
        int var5 = param3.getY() + var2;
        int var6 = var1 - 5;
        List<FancyTrunkPlacer.FoliageCoords> var7 = Lists.newArrayList();
        var7.add(new FancyTrunkPlacer.FoliageCoords(param3.above(var6), var5));

        for(; var6 >= 0; --var6) {
            float var8 = this.treeShape(var1, var6);
            if (!(var8 < 0.0F)) {
                for(int var9 = 0; var9 < var4; ++var9) {
                    double var10 = 1.0;
                    double var11 = 1.0 * (double)var8 * ((double)param1.nextFloat() + 0.328);
                    double var12 = (double)(param1.nextFloat() * 2.0F) * Math.PI;
                    double var13 = var11 * Math.sin(var12) + 0.5;
                    double var14 = var11 * Math.cos(var12) + 0.5;
                    BlockPos var15 = param3.offset(var13, (double)(var6 - 1), var14);
                    BlockPos var16 = var15.above(5);
                    if (this.makeLimb(param0, param1, var15, var16, false, param4, param5, param6)) {
                        int var17 = param3.getX() - var15.getX();
                        int var18 = param3.getZ() - var15.getZ();
                        double var19 = (double)var15.getY() - Math.sqrt((double)(var17 * var17 + var18 * var18)) * 0.381;
                        int var20 = var19 > (double)var5 ? var5 : (int)var19;
                        BlockPos var21 = new BlockPos(param3.getX(), var20, param3.getZ());
                        if (this.makeLimb(param0, param1, var21, var15, false, param4, param5, param6)) {
                            var7.add(new FancyTrunkPlacer.FoliageCoords(var15, var21.getY()));
                        }
                    }
                }
            }
        }

        this.makeLimb(param0, param1, param3, param3.above(var2), true, param4, param5, param6);
        this.makeBranches(param0, param1, var1, param3, var7, param4, param5, param6);
        List<FoliagePlacer.FoliageAttachment> var22 = Lists.newArrayList();

        for(FancyTrunkPlacer.FoliageCoords var23 : var7) {
            if (this.trimBranches(var1, var23.getBranchBase() - param3.getY())) {
                var22.add(var23.attachment);
            }
        }

        return var22;
    }

    private boolean makeLimb(
        LevelSimulatedRW param0,
        Random param1,
        BlockPos param2,
        BlockPos param3,
        boolean param4,
        Set<BlockPos> param5,
        BoundingBox param6,
        TreeConfiguration param7
    ) {
        if (!param4 && Objects.equals(param2, param3)) {
            return true;
        } else {
            BlockPos var0 = param3.offset(-param2.getX(), -param2.getY(), -param2.getZ());
            int var1 = this.getSteps(var0);
            float var2 = (float)var0.getX() / (float)var1;
            float var3 = (float)var0.getY() / (float)var1;
            float var4 = (float)var0.getZ() / (float)var1;

            for(int var5 = 0; var5 <= var1; ++var5) {
                BlockPos var6 = param2.offset((double)(0.5F + (float)var5 * var2), (double)(0.5F + (float)var5 * var3), (double)(0.5F + (float)var5 * var4));
                if (param4) {
                    setBlock(param0, var6, param7.trunkProvider.getState(param1, var6).setValue(RotatedPillarBlock.AXIS, this.getLogAxis(param2, var6)), param6);
                    param5.add(var6.immutable());
                } else if (!TreeFeature.isFree(param0, var6)) {
                    return false;
                }
            }

            return true;
        }
    }

    private int getSteps(BlockPos param0) {
        int var0 = Mth.abs(param0.getX());
        int var1 = Mth.abs(param0.getY());
        int var2 = Mth.abs(param0.getZ());
        return Math.max(var0, Math.max(var1, var2));
    }

    private Direction.Axis getLogAxis(BlockPos param0, BlockPos param1) {
        Direction.Axis var0 = Direction.Axis.Y;
        int var1 = Math.abs(param1.getX() - param0.getX());
        int var2 = Math.abs(param1.getZ() - param0.getZ());
        int var3 = Math.max(var1, var2);
        if (var3 > 0) {
            if (var1 == var3) {
                var0 = Direction.Axis.X;
            } else {
                var0 = Direction.Axis.Z;
            }
        }

        return var0;
    }

    private boolean trimBranches(int param0, int param1) {
        return (double)param1 >= (double)param0 * 0.2;
    }

    private void makeBranches(
        LevelSimulatedRW param0,
        Random param1,
        int param2,
        BlockPos param3,
        List<FancyTrunkPlacer.FoliageCoords> param4,
        Set<BlockPos> param5,
        BoundingBox param6,
        TreeConfiguration param7
    ) {
        for(FancyTrunkPlacer.FoliageCoords var0 : param4) {
            int var1 = var0.getBranchBase();
            BlockPos var2 = new BlockPos(param3.getX(), var1, param3.getZ());
            if (!var2.equals(var0.attachment.foliagePos()) && this.trimBranches(param2, var1 - param3.getY())) {
                this.makeLimb(param0, param1, var2, var0.attachment.foliagePos(), true, param5, param6, param7);
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

    static class FoliageCoords {
        private final FoliagePlacer.FoliageAttachment attachment;
        private final int branchBase;

        public FoliageCoords(BlockPos param0, int param1) {
            this.attachment = new FoliagePlacer.FoliageAttachment(param0, 0, false);
            this.branchBase = param1;
        }

        public int getBranchBase() {
            return this.branchBase;
        }
    }
}
