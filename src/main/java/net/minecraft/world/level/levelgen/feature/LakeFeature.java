package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import net.minecraft.world.level.material.Material;

public class LakeFeature extends Feature<BlockStateConfiguration> {
    private static final BlockState AIR = Blocks.CAVE_AIR.defaultBlockState();

    public LakeFeature(Codec<BlockStateConfiguration> param0) {
        super(param0);
    }

    @Override
    public boolean place(FeaturePlaceContext<BlockStateConfiguration> param0) {
        BlockPos var0 = param0.origin();
        WorldGenLevel var1 = param0.level();
        Random var2 = param0.random();
        BlockStateConfiguration var3 = param0.config();

        while(var0.getY() > var1.getMinBuildHeight() + 5 && var1.isEmptyBlock(var0)) {
            var0 = var0.below();
        }

        if (var0.getY() <= var1.getMinBuildHeight() + 4) {
            return false;
        } else {
            var0 = var0.below(4);
            if (!var1.startsForFeature(SectionPos.of(var0), StructureFeature.VILLAGE).isEmpty()) {
                return false;
            } else {
                boolean[] var4 = new boolean[2048];
                int var5 = var2.nextInt(4) + 4;

                for(int var6 = 0; var6 < var5; ++var6) {
                    double var7 = var2.nextDouble() * 6.0 + 3.0;
                    double var8 = var2.nextDouble() * 4.0 + 2.0;
                    double var9 = var2.nextDouble() * 6.0 + 3.0;
                    double var10 = var2.nextDouble() * (16.0 - var7 - 2.0) + 1.0 + var7 / 2.0;
                    double var11 = var2.nextDouble() * (8.0 - var8 - 4.0) + 2.0 + var8 / 2.0;
                    double var12 = var2.nextDouble() * (16.0 - var9 - 2.0) + 1.0 + var9 / 2.0;

                    for(int var13 = 1; var13 < 15; ++var13) {
                        for(int var14 = 1; var14 < 15; ++var14) {
                            for(int var15 = 1; var15 < 7; ++var15) {
                                double var16 = ((double)var13 - var10) / (var7 / 2.0);
                                double var17 = ((double)var15 - var11) / (var8 / 2.0);
                                double var18 = ((double)var14 - var12) / (var9 / 2.0);
                                double var19 = var16 * var16 + var17 * var17 + var18 * var18;
                                if (var19 < 1.0) {
                                    var4[(var13 * 16 + var14) * 8 + var15] = true;
                                }
                            }
                        }
                    }
                }

                for(int var20 = 0; var20 < 16; ++var20) {
                    for(int var21 = 0; var21 < 16; ++var21) {
                        for(int var22 = 0; var22 < 8; ++var22) {
                            boolean var23 = !var4[(var20 * 16 + var21) * 8 + var22]
                                && (
                                    var20 < 15 && var4[((var20 + 1) * 16 + var21) * 8 + var22]
                                        || var20 > 0 && var4[((var20 - 1) * 16 + var21) * 8 + var22]
                                        || var21 < 15 && var4[(var20 * 16 + var21 + 1) * 8 + var22]
                                        || var21 > 0 && var4[(var20 * 16 + (var21 - 1)) * 8 + var22]
                                        || var22 < 7 && var4[(var20 * 16 + var21) * 8 + var22 + 1]
                                        || var22 > 0 && var4[(var20 * 16 + var21) * 8 + (var22 - 1)]
                                );
                            if (var23) {
                                Material var24 = var1.getBlockState(var0.offset(var20, var22, var21)).getMaterial();
                                if (var22 >= 4 && var24.isLiquid()) {
                                    return false;
                                }

                                if (var22 < 4 && !var24.isSolid() && var1.getBlockState(var0.offset(var20, var22, var21)) != var3.state) {
                                    return false;
                                }
                            }
                        }
                    }
                }

                for(int var25 = 0; var25 < 16; ++var25) {
                    for(int var26 = 0; var26 < 16; ++var26) {
                        for(int var27 = 0; var27 < 8; ++var27) {
                            if (var4[(var25 * 16 + var26) * 8 + var27]) {
                                BlockPos var28 = var0.offset(var25, var27, var26);
                                boolean var29 = var27 >= 4;
                                var1.setBlock(var28, var29 ? AIR : var3.state, 2);
                                if (var29) {
                                    var1.getBlockTicks().scheduleTick(var28, AIR.getBlock(), 0);
                                    this.markAboveForPostProcessing(var1, var28);
                                }
                            }
                        }
                    }
                }

                if (var3.state.getMaterial() == Material.WATER) {
                    for(int var30 = 0; var30 < 16; ++var30) {
                        for(int var31 = 0; var31 < 16; ++var31) {
                            int var32 = 4;
                            BlockPos var33 = var0.offset(var30, 4, var31);
                            if (var1.getBiome(var33).shouldFreeze(var1, var33, false)) {
                                var1.setBlock(var33, Blocks.ICE.defaultBlockState(), 2);
                            }
                        }
                    }
                }

                return true;
            }
        }
    }
}
