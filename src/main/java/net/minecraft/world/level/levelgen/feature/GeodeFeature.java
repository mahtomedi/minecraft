package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Random;
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
        int var7 = var0.minDistributionPoints + var1.nextInt(var0.maxDistributionPoints - var0.minDistributionPoints);
        WorldgenRandom var8 = new WorldgenRandom(var3.getSeed());
        NormalNoise var9 = NormalNoise.create(var8, -4, 1.0);
        List<BlockPos> var10 = Lists.newLinkedList();
        double var11 = (double)var7 / (double)var0.maxOuterWallDistance;
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
            int var23 = var0.minOuterWallDistance + var1.nextInt(var0.maxOuterWallDistance - var0.minOuterWallDistance);
            int var24 = var0.minOuterWallDistance + var1.nextInt(var0.maxOuterWallDistance - var0.minOuterWallDistance);
            int var25 = var0.minOuterWallDistance + var1.nextInt(var0.maxOuterWallDistance - var0.minOuterWallDistance);
            BlockPos var26 = var2.offset(var23, var24, var25);
            BlockState var27 = var3.getBlockState(var26);
            if (var27.isAir() || var27.is(Blocks.WATER) || var27.is(Blocks.LAVA)) {
                if (++var21 > var0.invalidBlocksThreshold) {
                    return false;
                }
            }

            var6.add(Pair.of(var26, var0.minPointOffset + var1.nextInt(var0.maxPointOffset - var0.minPointOffset)));
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

        for(BlockPos var31 : BlockPos.betweenClosed(var2.offset(var4, var4, var4), var2.offset(var5, var5, var5))) {
            double var32 = var9.getValue((double)var31.getX(), (double)var31.getY(), (double)var31.getZ()) * var0.noiseMultiplier;
            double var33 = 0.0;
            double var34 = 0.0;

            for(Pair<BlockPos, Integer> var35 : var6) {
                var33 += Mth.fastInvSqrt(var31.distSqr(var35.getFirst()) + (double)var35.getSecond().intValue()) + var32;
            }

            for(BlockPos var36 : var10) {
                var34 += Mth.fastInvSqrt(var31.distSqr(var36) + (double)var14.crackPointOffset) + var32;
            }

            if (!(var33 < var18)) {
                if (var20 && var34 >= var19 && var33 < var15) {
                    if (var3.getFluidState(var31).isEmpty()) {
                        var3.setBlock(var31, Blocks.AIR.defaultBlockState(), 2);
                    }
                } else if (var33 >= var15) {
                    var3.setBlock(var31, var13.fillingProvider.getState(var1, var31), 2);
                } else if (var33 >= var16) {
                    boolean var37 = (double)var1.nextFloat() < var0.useAlternateLayer0Chance;
                    if (var37) {
                        var3.setBlock(var31, var13.alternateInnerLayerProvider.getState(var1, var31), 2);
                    } else {
                        var3.setBlock(var31, var13.innerLayerProvider.getState(var1, var31), 2);
                    }

                    if ((!var0.placementsRequireLayer0Alternate || var37) && (double)var1.nextFloat() < var0.usePotentialPlacementsChance) {
                        var30.add(var31.immutable());
                    }
                } else if (var33 >= var17) {
                    var3.setBlock(var31, var13.middleLayerProvider.getState(var1, var31), 2);
                } else if (var33 >= var18) {
                    var3.setBlock(var31, var13.outerLayerProvider.getState(var1, var31), 2);
                }
            }
        }

        List<BlockState> var38 = var13.innerPlacements;

        for(BlockPos var39 : var30) {
            BlockState var40 = var38.get(var1.nextInt(var38.size()));

            for(Direction var41 : DIRECTIONS) {
                if (var40.hasProperty(BlockStateProperties.FACING)) {
                    var40 = var40.setValue(BlockStateProperties.FACING, var41);
                }

                BlockPos var42 = var39.relative(var41);
                BlockState var43 = var3.getBlockState(var42);
                if (var40.hasProperty(BlockStateProperties.WATERLOGGED)) {
                    var40 = var40.setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(var43.getFluidState().isSource()));
                }

                if (BuddingAmethystBlock.canClusterGrowAtState(var43)) {
                    var3.setBlock(var42, var40, 2);
                    break;
                }
            }
        }

        return true;
    }
}
