package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;

public class FancyTrunkPlacer extends TrunkPlacer {
    public static final Codec<FancyTrunkPlacer> CODEC = RecordCodecBuilder.create(param0 -> trunkPlacerParts(param0).apply(param0, FancyTrunkPlacer::new));
    private static final double TRUNK_HEIGHT_SCALE = 0.618;
    private static final double CLUSTER_DENSITY_MAGIC = 1.382;
    private static final double BRANCH_SLOPE = 0.381;
    private static final double BRANCH_LENGTH_MAGIC = 0.328;

    public FancyTrunkPlacer(int param0, int param1, int param2) {
        super(param0, param1, param2);
    }

    @Override
    protected TrunkPlacerType<?> type() {
        return TrunkPlacerType.FANCY_TRUNK_PLACER;
    }

    @Override
    public List<FoliagePlacer.FoliageAttachment> placeTrunk(
        LevelSimulatedReader param0, BiConsumer<BlockPos, BlockState> param1, RandomSource param2, int param3, BlockPos param4, TreeConfiguration param5
    ) {
        int var0 = 5;
        int var1 = param3 + 2;
        int var2 = Mth.floor((double)var1 * 0.618);
        setDirtAt(param0, param1, param2, param4.below(), param5);
        double var3 = 1.0;
        int var4 = Math.min(1, Mth.floor(1.382 + Math.pow(1.0 * (double)var1 / 13.0, 2.0)));
        int var5 = param4.getY() + var2;
        int var6 = var1 - 5;
        List<FancyTrunkPlacer.FoliageCoords> var7 = Lists.newArrayList();
        var7.add(new FancyTrunkPlacer.FoliageCoords(param4.above(var6), var5));

        for(; var6 >= 0; --var6) {
            float var8 = treeShape(var1, var6);
            if (!(var8 < 0.0F)) {
                for(int var9 = 0; var9 < var4; ++var9) {
                    double var10 = 1.0;
                    double var11 = 1.0 * (double)var8 * ((double)param2.nextFloat() + 0.328);
                    double var12 = (double)(param2.nextFloat() * 2.0F) * Math.PI;
                    double var13 = var11 * Math.sin(var12) + 0.5;
                    double var14 = var11 * Math.cos(var12) + 0.5;
                    BlockPos var15 = param4.offset(var13, (double)(var6 - 1), var14);
                    BlockPos var16 = var15.above(5);
                    if (this.makeLimb(param0, param1, param2, var15, var16, false, param5)) {
                        int var17 = param4.getX() - var15.getX();
                        int var18 = param4.getZ() - var15.getZ();
                        double var19 = (double)var15.getY() - Math.sqrt((double)(var17 * var17 + var18 * var18)) * 0.381;
                        int var20 = var19 > (double)var5 ? var5 : (int)var19;
                        BlockPos var21 = new BlockPos(param4.getX(), var20, param4.getZ());
                        if (this.makeLimb(param0, param1, param2, var21, var15, false, param5)) {
                            var7.add(new FancyTrunkPlacer.FoliageCoords(var15, var21.getY()));
                        }
                    }
                }
            }
        }

        this.makeLimb(param0, param1, param2, param4, param4.above(var2), true, param5);
        this.makeBranches(param0, param1, param2, var1, param4, var7, param5);
        List<FoliagePlacer.FoliageAttachment> var22 = Lists.newArrayList();

        for(FancyTrunkPlacer.FoliageCoords var23 : var7) {
            if (this.trimBranches(var1, var23.getBranchBase() - param4.getY())) {
                var22.add(var23.attachment);
            }
        }

        return var22;
    }

    private boolean makeLimb(
        LevelSimulatedReader param0,
        BiConsumer<BlockPos, BlockState> param1,
        RandomSource param2,
        BlockPos param3,
        BlockPos param4,
        boolean param5,
        TreeConfiguration param6
    ) {
        if (!param5 && Objects.equals(param3, param4)) {
            return true;
        } else {
            BlockPos var0 = param4.offset(-param3.getX(), -param3.getY(), -param3.getZ());
            int var1 = this.getSteps(var0);
            float var2 = (float)var0.getX() / (float)var1;
            float var3 = (float)var0.getY() / (float)var1;
            float var4 = (float)var0.getZ() / (float)var1;

            for(int var5 = 0; var5 <= var1; ++var5) {
                BlockPos var6 = param3.offset((double)(0.5F + (float)var5 * var2), (double)(0.5F + (float)var5 * var3), (double)(0.5F + (float)var5 * var4));
                if (param5) {
                    this.placeLog(param0, param1, param2, var6, param6, param2x -> param2x.setValue(RotatedPillarBlock.AXIS, this.getLogAxis(param3, var6)));
                } else if (!this.isFree(param0, var6)) {
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
        LevelSimulatedReader param0,
        BiConsumer<BlockPos, BlockState> param1,
        RandomSource param2,
        int param3,
        BlockPos param4,
        List<FancyTrunkPlacer.FoliageCoords> param5,
        TreeConfiguration param6
    ) {
        for(FancyTrunkPlacer.FoliageCoords var0 : param5) {
            int var1 = var0.getBranchBase();
            BlockPos var2 = new BlockPos(param4.getX(), var1, param4.getZ());
            if (!var2.equals(var0.attachment.pos()) && this.trimBranches(param3, var1 - param4.getY())) {
                this.makeLimb(param0, param1, param2, var2, var0.attachment.pos(), true, param6);
            }
        }

    }

    private static float treeShape(int param0, int param1) {
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
        final FoliagePlacer.FoliageAttachment attachment;
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
