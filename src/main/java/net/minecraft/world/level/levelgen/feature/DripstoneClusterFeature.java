package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ClampedNormalFloat;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Column;
import net.minecraft.world.level.levelgen.feature.configurations.DripstoneClusterConfiguration;

public class DripstoneClusterFeature extends Feature<DripstoneClusterConfiguration> {
    public DripstoneClusterFeature(Codec<DripstoneClusterConfiguration> param0) {
        super(param0);
    }

    @Override
    public boolean place(FeaturePlaceContext<DripstoneClusterConfiguration> param0) {
        WorldGenLevel var0 = param0.level();
        BlockPos var1 = param0.origin();
        DripstoneClusterConfiguration var2 = param0.config();
        RandomSource var3 = param0.random();
        if (!DripstoneUtils.isEmptyOrWater(var0, var1)) {
            return false;
        } else {
            int var4 = var2.height.sample(var3);
            float var5 = var2.wetness.sample(var3);
            float var6 = var2.density.sample(var3);
            int var7 = var2.radius.sample(var3);
            int var8 = var2.radius.sample(var3);

            for(int var9 = -var7; var9 <= var7; ++var9) {
                for(int var10 = -var8; var10 <= var8; ++var10) {
                    double var11 = this.getChanceOfStalagmiteOrStalactite(var7, var8, var9, var10, var2);
                    BlockPos var12 = var1.offset(var9, 0, var10);
                    this.placeColumn(var0, var3, var12, var9, var10, var5, var11, var4, var6, var2);
                }
            }

            return true;
        }
    }

    private void placeColumn(
        WorldGenLevel param0,
        RandomSource param1,
        BlockPos param2,
        int param3,
        int param4,
        float param5,
        double param6,
        int param7,
        float param8,
        DripstoneClusterConfiguration param9
    ) {
        Optional<Column> var0 = Column.scan(
            param0, param2, param9.floorToCeilingSearchRange, DripstoneUtils::isEmptyOrWater, DripstoneUtils::isNeitherEmptyNorWater
        );
        if (!var0.isEmpty()) {
            OptionalInt var1 = var0.get().getCeiling();
            OptionalInt var2 = var0.get().getFloor();
            if (!var1.isEmpty() || !var2.isEmpty()) {
                boolean var3 = param1.nextFloat() < param5;
                Column var5;
                if (var3 && var2.isPresent() && this.canPlacePool(param0, param2.atY(var2.getAsInt()))) {
                    int var4 = var2.getAsInt();
                    var5 = var0.get().withFloor(OptionalInt.of(var4 - 1));
                    param0.setBlock(param2.atY(var4), Blocks.WATER.defaultBlockState(), 2);
                } else {
                    var5 = var0.get();
                }

                OptionalInt var7 = var5.getFloor();
                boolean var8 = param1.nextDouble() < param6;
                int var12;
                if (var1.isPresent() && var8 && !this.isLava(param0, param2.atY(var1.getAsInt()))) {
                    int var9 = param9.dripstoneBlockLayerThickness.sample(param1);
                    this.replaceBlocksWithDripstoneBlocks(param0, param2.atY(var1.getAsInt()), var9, Direction.UP);
                    int var10;
                    if (var7.isPresent()) {
                        var10 = Math.min(param7, var1.getAsInt() - var7.getAsInt());
                    } else {
                        var10 = param7;
                    }

                    var12 = this.getDripstoneHeight(param1, param3, param4, param8, var10, param9);
                } else {
                    var12 = 0;
                }

                boolean var14 = param1.nextDouble() < param6;
                int var16;
                if (var7.isPresent() && var14 && !this.isLava(param0, param2.atY(var7.getAsInt()))) {
                    int var15 = param9.dripstoneBlockLayerThickness.sample(param1);
                    this.replaceBlocksWithDripstoneBlocks(param0, param2.atY(var7.getAsInt()), var15, Direction.DOWN);
                    if (var1.isPresent()) {
                        var16 = Math.max(
                            0, var12 + Mth.randomBetweenInclusive(param1, -param9.maxStalagmiteStalactiteHeightDiff, param9.maxStalagmiteStalactiteHeightDiff)
                        );
                    } else {
                        var16 = this.getDripstoneHeight(param1, param3, param4, param8, param7, param9);
                    }
                } else {
                    var16 = 0;
                }

                int var26;
                int var25;
                if (var1.isPresent() && var7.isPresent() && var1.getAsInt() - var12 <= var7.getAsInt() + var16) {
                    int var19 = var7.getAsInt();
                    int var20 = var1.getAsInt();
                    int var21 = Math.max(var20 - var12, var19 + 1);
                    int var22 = Math.min(var19 + var16, var20 - 1);
                    int var23 = Mth.randomBetweenInclusive(param1, var21, var22 + 1);
                    int var24 = var23 - 1;
                    var25 = var20 - var23;
                    var26 = var24 - var19;
                } else {
                    var25 = var12;
                    var26 = var16;
                }

                boolean var29 = param1.nextBoolean() && var25 > 0 && var26 > 0 && var5.getHeight().isPresent() && var25 + var26 == var5.getHeight().getAsInt();
                if (var1.isPresent()) {
                    DripstoneUtils.growPointedDripstone(param0, param2.atY(var1.getAsInt() - 1), Direction.DOWN, var25, var29);
                }

                if (var7.isPresent()) {
                    DripstoneUtils.growPointedDripstone(param0, param2.atY(var7.getAsInt() + 1), Direction.UP, var26, var29);
                }

            }
        }
    }

    private boolean isLava(LevelReader param0, BlockPos param1) {
        return param0.getBlockState(param1).is(Blocks.LAVA);
    }

    private int getDripstoneHeight(RandomSource param0, int param1, int param2, float param3, int param4, DripstoneClusterConfiguration param5) {
        if (param0.nextFloat() > param3) {
            return 0;
        } else {
            int var0 = Math.abs(param1) + Math.abs(param2);
            float var1 = (float)Mth.clampedMap((double)var0, 0.0, (double)param5.maxDistanceFromCenterAffectingHeightBias, (double)param4 / 2.0, 0.0);
            return (int)randomBetweenBiased(param0, 0.0F, (float)param4, var1, (float)param5.heightDeviation);
        }
    }

    private boolean canPlacePool(WorldGenLevel param0, BlockPos param1) {
        BlockState var0 = param0.getBlockState(param1);
        if (!var0.is(Blocks.WATER) && !var0.is(Blocks.DRIPSTONE_BLOCK) && !var0.is(Blocks.POINTED_DRIPSTONE)) {
            if (param0.getBlockState(param1.above()).getFluidState().is(FluidTags.WATER)) {
                return false;
            } else {
                for(Direction var1 : Direction.Plane.HORIZONTAL) {
                    if (!this.canBeAdjacentToWater(param0, param1.relative(var1))) {
                        return false;
                    }
                }

                return this.canBeAdjacentToWater(param0, param1.below());
            }
        } else {
            return false;
        }
    }

    private boolean canBeAdjacentToWater(LevelAccessor param0, BlockPos param1) {
        BlockState var0 = param0.getBlockState(param1);
        return var0.is(BlockTags.BASE_STONE_OVERWORLD) || var0.getFluidState().is(FluidTags.WATER);
    }

    private void replaceBlocksWithDripstoneBlocks(WorldGenLevel param0, BlockPos param1, int param2, Direction param3) {
        BlockPos.MutableBlockPos var0 = param1.mutable();

        for(int var1 = 0; var1 < param2; ++var1) {
            if (!DripstoneUtils.placeDripstoneBlockIfPossible(param0, var0)) {
                return;
            }

            var0.move(param3);
        }

    }

    private double getChanceOfStalagmiteOrStalactite(int param0, int param1, int param2, int param3, DripstoneClusterConfiguration param4) {
        int var0 = param0 - Math.abs(param2);
        int var1 = param1 - Math.abs(param3);
        int var2 = Math.min(var0, var1);
        return (double)Mth.clampedMap(
            (float)var2, 0.0F, (float)param4.maxDistanceFromEdgeAffectingChanceOfDripstoneColumn, param4.chanceOfDripstoneColumnAtMaxDistanceFromCenter, 1.0F
        );
    }

    private static float randomBetweenBiased(RandomSource param0, float param1, float param2, float param3, float param4) {
        return ClampedNormalFloat.sample(param0, param3, param4, param1, param2);
    }
}
