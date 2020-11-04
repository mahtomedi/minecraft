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
import net.minecraft.world.level.chunk.ChunkGenerator;
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

    public boolean place(WorldGenLevel param0, ChunkGenerator param1, Random param2, BlockPos param3, GeodeConfiguration param4) {
        int var0 = param4.minGenOffset;
        int var1 = param4.maxGenOffset;
        if (param0.getFluidState(param3.offset(0, var1 / 3, 0)).isSource()) {
            return false;
        } else {
            List<Pair<BlockPos, Integer>> var2 = Lists.newLinkedList();
            int var3 = param4.minDistributionPoints + param2.nextInt(param4.maxDistributionPoints - param4.minDistributionPoints);
            WorldgenRandom var4 = new WorldgenRandom(param0.getSeed());
            NormalNoise var5 = NormalNoise.create(var4, -4, 1.0);
            List<BlockPos> var6 = Lists.newLinkedList();
            double var7 = (double)var3 / (double)param4.maxOuterWallDistance;
            GeodeLayerSettings var8 = param4.geodeLayerSettings;
            GeodeBlockSettings var9 = param4.geodeBlockSettings;
            GeodeCrackSettings var10 = param4.geodeCrackSettings;
            double var11 = 1.0 / Math.sqrt(var8.filling);
            double var12 = 1.0 / Math.sqrt(var8.innerLayer + var7);
            double var13 = 1.0 / Math.sqrt(var8.middleLayer + var7);
            double var14 = 1.0 / Math.sqrt(var8.outerLayer + var7);
            double var15 = 1.0 / Math.sqrt(var10.baseCrackSize + param2.nextDouble() / 2.0 + (var3 > 3 ? var7 : 0.0));
            boolean var16 = (double)param2.nextFloat() < var10.generateCrackChance;

            for(int var17 = 0; var17 < var3; ++var17) {
                int var18 = param4.minOuterWallDistance + param2.nextInt(param4.maxOuterWallDistance - param4.minOuterWallDistance);
                int var19 = param4.minOuterWallDistance + param2.nextInt(param4.maxOuterWallDistance - param4.minOuterWallDistance);
                int var20 = param4.minOuterWallDistance + param2.nextInt(param4.maxOuterWallDistance - param4.minOuterWallDistance);
                var2.add(Pair.of(param3.offset(var18, var19, var20), param4.minPointOffset + param2.nextInt(param4.maxPointOffset - param4.minPointOffset)));
            }

            if (var16) {
                int var21 = param2.nextInt(4);
                int var22 = var3 * 2 + 1;
                if (var21 == 0) {
                    var6.add(param3.offset(var22, 7, 0));
                    var6.add(param3.offset(var22, 5, 0));
                    var6.add(param3.offset(var22, 1, 0));
                } else if (var21 == 1) {
                    var6.add(param3.offset(0, 7, var22));
                    var6.add(param3.offset(0, 5, var22));
                    var6.add(param3.offset(0, 1, var22));
                } else if (var21 == 2) {
                    var6.add(param3.offset(var22, 7, var22));
                    var6.add(param3.offset(var22, 5, var22));
                    var6.add(param3.offset(var22, 1, var22));
                } else {
                    var6.add(param3.offset(0, 7, 0));
                    var6.add(param3.offset(0, 5, 0));
                    var6.add(param3.offset(0, 1, 0));
                }
            }

            List<BlockPos> var23 = Lists.newArrayList();

            for(BlockPos var24 : BlockPos.betweenClosed(param3.offset(var0, var0, var0), param3.offset(var1, var1, var1))) {
                double var25 = var5.getValue((double)var24.getX(), (double)var24.getY(), (double)var24.getZ()) * param4.noiseMultiplier;
                double var26 = 0.0;
                double var27 = 0.0;

                for(Pair<BlockPos, Integer> var28 : var2) {
                    var26 += Mth.fastInvSqrt(var24.distSqr(var28.getFirst()) + (double)var28.getSecond().intValue()) + var25;
                }

                for(BlockPos var29 : var6) {
                    var27 += Mth.fastInvSqrt(var24.distSqr(var29) + (double)var10.crackPointOffset) + var25;
                }

                if (!(var26 < var14)) {
                    if (var16 && var27 >= var15 && var26 < var11) {
                        if (param0.getFluidState(var24).isEmpty()) {
                            param0.setBlock(var24, Blocks.AIR.defaultBlockState(), 2);
                        }
                    } else if (var26 >= var11) {
                        param0.setBlock(var24, var9.fillingProvider.getState(param2, var24), 2);
                    } else if (var26 >= var12) {
                        boolean var30 = (double)param2.nextFloat() < param4.useAlternateLayer0Chance;
                        if (var30) {
                            param0.setBlock(var24, var9.alternateInnerLayerProvider.getState(param2, var24), 2);
                        } else {
                            param0.setBlock(var24, var9.innerLayerProvider.getState(param2, var24), 2);
                        }

                        if ((!param4.placementsRequireLayer0Alternate || var30) && (double)param2.nextFloat() < param4.usePotentialPlacementsChance) {
                            var23.add(var24.immutable());
                        }
                    } else if (var26 >= var13) {
                        param0.setBlock(var24, var9.middleLayerProvider.getState(param2, var24), 2);
                    } else if (var26 >= var14) {
                        param0.setBlock(var24, var9.outerLayerProvider.getState(param2, var24), 2);
                    }
                }
            }

            List<BlockState> var31 = var9.innerPlacements;

            for(BlockPos var32 : var23) {
                BlockState var33 = var31.get(param2.nextInt(var31.size()));

                for(Direction var34 : DIRECTIONS) {
                    if (var33.hasProperty(BlockStateProperties.FACING)) {
                        var33 = var33.setValue(BlockStateProperties.FACING, var34);
                    }

                    BlockPos var35 = var32.relative(var34);
                    BlockState var36 = param0.getBlockState(var35);
                    if (var33.hasProperty(BlockStateProperties.WATERLOGGED)) {
                        var33 = var33.setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(var36.getFluidState().isSource()));
                    }

                    if (BuddingAmethystBlock.canClusterGrowAtState(var36)) {
                        param0.setBlock(var35, var33, 2);
                        break;
                    }
                }
            }

            return true;
        }
    }
}
