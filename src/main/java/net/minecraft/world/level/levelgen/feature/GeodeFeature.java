package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BuddingAmethystBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.GeodeBlockSettings;
import net.minecraft.world.level.levelgen.GeodeCrackSettings;
import net.minecraft.world.level.levelgen.GeodeLayerSettings;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.GeodeConfiguration;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.material.FluidState;

public class GeodeFeature extends Feature<GeodeConfiguration> {
    private static final Direction[] DIRECTIONS = Direction.values();

    public GeodeFeature(Codec<GeodeConfiguration> param0) {
        super(param0);
    }

    @Override
    public boolean place(FeaturePlaceContext<GeodeConfiguration> param0) {
        GeodeConfiguration var0 = param0.config();
        Random var1 = param0.random();
        BlockPos var2 = param0.origin();
        WorldGenLevel var3 = param0.level();
        int var4 = var0.minGenOffset;
        int var5 = var0.maxGenOffset;
        List<Pair<BlockPos, Integer>> var6 = Lists.newLinkedList();
        int var7 = var0.distributionPoints.sample(var1);
        WorldgenRandom var8 = new WorldgenRandom(var3.getSeed());
        NormalNoise var9 = NormalNoise.create(var8, -4, 1.0);
        List<BlockPos> var10 = Lists.newLinkedList();
        double var11 = (double)var7 / (double)var0.outerWallDistance.getMaxValue();
        GeodeLayerSettings var12 = var0.geodeLayerSettings;
        GeodeBlockSettings var13 = var0.geodeBlockSettings;
        GeodeCrackSettings var14 = var0.geodeCrackSettings;
        double var15 = 1.0 / Math.sqrt(var12.filling);
        double var16 = 1.0 / Math.sqrt(var12.innerLayer + var11);
        double var17 = 1.0 / Math.sqrt(var12.middleLayer + var11);
        double var18 = 1.0 / Math.sqrt(var12.outerLayer + var11);
        double var19 = 1.0 / Math.sqrt(var14.baseCrackSize + var1.nextDouble() / 2.0 + (var7 > 3 ? var11 : 0.0));
        boolean var20 = (double)var1.nextFloat() < var14.generateCrackChance;
        int var21 = 0;

        for(int var22 = 0; var22 < var7; ++var22) {
            int var23 = var0.outerWallDistance.sample(var1);
            int var24 = var0.outerWallDistance.sample(var1);
            int var25 = var0.outerWallDistance.sample(var1);
            BlockPos var26 = var2.offset(var23, var24, var25);
            BlockState var27 = var3.getBlockState(var26);
            if (var27.isAir() || var27.is(Blocks.WATER) || var27.is(Blocks.LAVA)) {
                if (++var21 > var0.invalidBlocksThreshold) {
                    return false;
                }
            }

            var6.add(Pair.of(var26, var0.pointOffset.sample(var1)));
        }

        if (var20) {
            int var28 = var1.nextInt(4);
            int var29 = var7 * 2 + 1;
            if (var28 == 0) {
                var10.add(var2.offset(var29, 7, 0));
                var10.add(var2.offset(var29, 5, 0));
                var10.add(var2.offset(var29, 1, 0));
            } else if (var28 == 1) {
                var10.add(var2.offset(0, 7, var29));
                var10.add(var2.offset(0, 5, var29));
                var10.add(var2.offset(0, 1, var29));
            } else if (var28 == 2) {
                var10.add(var2.offset(var29, 7, var29));
                var10.add(var2.offset(var29, 5, var29));
                var10.add(var2.offset(var29, 1, var29));
            } else {
                var10.add(var2.offset(0, 7, 0));
                var10.add(var2.offset(0, 5, 0));
                var10.add(var2.offset(0, 1, 0));
            }
        }

        List<BlockPos> var30 = Lists.newArrayList();
        Predicate<BlockState> var31 = isReplaceable(var0.geodeBlockSettings.cannotReplace);

        for(BlockPos var32 : BlockPos.betweenClosed(var2.offset(var4, var4, var4), var2.offset(var5, var5, var5))) {
            double var33 = var9.getValue((double)var32.getX(), (double)var32.getY(), (double)var32.getZ()) * var0.noiseMultiplier;
            double var34 = 0.0;
            double var35 = 0.0;

            for(Pair<BlockPos, Integer> var36 : var6) {
                var34 += Mth.fastInvSqrt(var32.distSqr(var36.getFirst()) + (double)var36.getSecond().intValue()) + var33;
            }

            for(BlockPos var37 : var10) {
                var35 += Mth.fastInvSqrt(var32.distSqr(var37) + (double)var14.crackPointOffset) + var33;
            }

            if (!(var34 < var18)) {
                if (var20 && var35 >= var19 && var34 < var15) {
                    this.safeSetBlock(var3, var32, Blocks.AIR.defaultBlockState(), var31);

                    for(Direction var38 : DIRECTIONS) {
                        BlockPos var39 = var32.relative(var38);
                        FluidState var40 = var3.getFluidState(var39);
                        if (!var40.isEmpty()) {
                            var3.getLiquidTicks().scheduleTick(var39, var40.getType(), 0);
                        }
                    }
                } else if (var34 >= var15) {
                    this.safeSetBlock(var3, var32, var13.fillingProvider.getState(var1, var32), var31);
                } else if (var34 >= var16) {
                    boolean var41 = (double)var1.nextFloat() < var0.useAlternateLayer0Chance;
                    if (var41) {
                        this.safeSetBlock(var3, var32, var13.alternateInnerLayerProvider.getState(var1, var32), var31);
                    } else {
                        this.safeSetBlock(var3, var32, var13.innerLayerProvider.getState(var1, var32), var31);
                    }

                    if ((!var0.placementsRequireLayer0Alternate || var41) && (double)var1.nextFloat() < var0.usePotentialPlacementsChance) {
                        var30.add(var32.immutable());
                    }
                } else if (var34 >= var17) {
                    this.safeSetBlock(var3, var32, var13.middleLayerProvider.getState(var1, var32), var31);
                } else if (var34 >= var18) {
                    this.safeSetBlock(var3, var32, var13.outerLayerProvider.getState(var1, var32), var31);
                }
            }
        }

        List<BlockState> var42 = var13.innerPlacements;

        for(BlockPos var43 : var30) {
            BlockState var44 = Util.getRandom(var42, var1);

            for(Direction var45 : DIRECTIONS) {
                if (var44.hasProperty(BlockStateProperties.FACING)) {
                    var44 = var44.setValue(BlockStateProperties.FACING, var45);
                }

                BlockPos var46 = var43.relative(var45);
                BlockState var47 = var3.getBlockState(var46);
                if (var44.hasProperty(BlockStateProperties.WATERLOGGED)) {
                    var44 = var44.setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(var47.getFluidState().isSource()));
                }

                if (BuddingAmethystBlock.canClusterGrowAtState(var47)) {
                    this.safeSetBlock(var3, var46, var44, var31);
                    break;
                }
            }
        }

        return true;
    }
}
