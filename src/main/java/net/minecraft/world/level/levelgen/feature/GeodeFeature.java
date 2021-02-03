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
        if (var3.getFluidState(var2.offset(0, var5 / 3, 0)).isSource()) {
            return false;
        } else {
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

            for(int var21 = 0; var21 < var7; ++var21) {
                int var22 = var0.minOuterWallDistance + var1.nextInt(var0.maxOuterWallDistance - var0.minOuterWallDistance);
                int var23 = var0.minOuterWallDistance + var1.nextInt(var0.maxOuterWallDistance - var0.minOuterWallDistance);
                int var24 = var0.minOuterWallDistance + var1.nextInt(var0.maxOuterWallDistance - var0.minOuterWallDistance);
                var6.add(Pair.of(var2.offset(var22, var23, var24), var0.minPointOffset + var1.nextInt(var0.maxPointOffset - var0.minPointOffset)));
            }

            if (var20) {
                int var25 = var1.nextInt(4);
                int var26 = var7 * 2 + 1;
                if (var25 == 0) {
                    var10.add(var2.offset(var26, 7, 0));
                    var10.add(var2.offset(var26, 5, 0));
                    var10.add(var2.offset(var26, 1, 0));
                } else if (var25 == 1) {
                    var10.add(var2.offset(0, 7, var26));
                    var10.add(var2.offset(0, 5, var26));
                    var10.add(var2.offset(0, 1, var26));
                } else if (var25 == 2) {
                    var10.add(var2.offset(var26, 7, var26));
                    var10.add(var2.offset(var26, 5, var26));
                    var10.add(var2.offset(var26, 1, var26));
                } else {
                    var10.add(var2.offset(0, 7, 0));
                    var10.add(var2.offset(0, 5, 0));
                    var10.add(var2.offset(0, 1, 0));
                }
            }

            List<BlockPos> var27 = Lists.newArrayList();

            for(BlockPos var28 : BlockPos.betweenClosed(var2.offset(var4, var4, var4), var2.offset(var5, var5, var5))) {
                double var29 = var9.getValue((double)var28.getX(), (double)var28.getY(), (double)var28.getZ()) * var0.noiseMultiplier;
                double var30 = 0.0;
                double var31 = 0.0;

                for(Pair<BlockPos, Integer> var32 : var6) {
                    var30 += Mth.fastInvSqrt(var28.distSqr(var32.getFirst()) + (double)var32.getSecond().intValue()) + var29;
                }

                for(BlockPos var33 : var10) {
                    var31 += Mth.fastInvSqrt(var28.distSqr(var33) + (double)var14.crackPointOffset) + var29;
                }

                if (!(var30 < var18)) {
                    if (var20 && var31 >= var19 && var30 < var15) {
                        if (var3.getFluidState(var28).isEmpty()) {
                            var3.setBlock(var28, Blocks.AIR.defaultBlockState(), 2);
                        }
                    } else if (var30 >= var15) {
                        var3.setBlock(var28, var13.fillingProvider.getState(var1, var28), 2);
                    } else if (var30 >= var16) {
                        boolean var34 = (double)var1.nextFloat() < var0.useAlternateLayer0Chance;
                        if (var34) {
                            var3.setBlock(var28, var13.alternateInnerLayerProvider.getState(var1, var28), 2);
                        } else {
                            var3.setBlock(var28, var13.innerLayerProvider.getState(var1, var28), 2);
                        }

                        if ((!var0.placementsRequireLayer0Alternate || var34) && (double)var1.nextFloat() < var0.usePotentialPlacementsChance) {
                            var27.add(var28.immutable());
                        }
                    } else if (var30 >= var17) {
                        var3.setBlock(var28, var13.middleLayerProvider.getState(var1, var28), 2);
                    } else if (var30 >= var18) {
                        var3.setBlock(var28, var13.outerLayerProvider.getState(var1, var28), 2);
                    }
                }
            }

            List<BlockState> var35 = var13.innerPlacements;

            for(BlockPos var36 : var27) {
                BlockState var37 = var35.get(var1.nextInt(var35.size()));

                for(Direction var38 : DIRECTIONS) {
                    if (var37.hasProperty(BlockStateProperties.FACING)) {
                        var37 = var37.setValue(BlockStateProperties.FACING, var38);
                    }

                    BlockPos var39 = var36.relative(var38);
                    BlockState var40 = var3.getBlockState(var39);
                    if (var37.hasProperty(BlockStateProperties.WATERLOGGED)) {
                        var37 = var37.setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(var40.getFluidState().isSource()));
                    }

                    if (BuddingAmethystBlock.canClusterGrowAtState(var40)) {
                        var3.setBlock(var39, var37, 2);
                        break;
                    }
                }
            }

            return true;
        }
    }
}
