package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.BaseStoneSource;
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
            if (var1.startsForFeature(SectionPos.of(var0), StructureFeature.VILLAGE).findAny().isPresent()) {
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
                                    BlockPos var30 = var28.above();
                                    this.tryScheduleBlockTick(var1, var30, var1.getBlockState(var30));
                                }
                            }
                        }
                    }
                }

                for(int var31 = 0; var31 < 16; ++var31) {
                    for(int var32 = 0; var32 < 16; ++var32) {
                        for(int var33 = 4; var33 < 8; ++var33) {
                            if (var4[(var31 * 16 + var32) * 8 + var33]) {
                                BlockPos var34 = var0.offset(var31, var33 - 1, var32);
                                if (isDirt(var1.getBlockState(var34)) && var1.getBrightness(LightLayer.SKY, var0.offset(var31, var33, var32)) > 0) {
                                    Biome var35 = var1.getBiome(var34);
                                    if (var35.getGenerationSettings().getSurfaceBuilderConfig().getTopMaterial().is(Blocks.MYCELIUM)) {
                                        var1.setBlock(var34, Blocks.MYCELIUM.defaultBlockState(), 2);
                                    } else {
                                        var1.setBlock(var34, Blocks.GRASS_BLOCK.defaultBlockState(), 2);
                                    }
                                }
                            }
                        }
                    }
                }

                if (var3.state.getMaterial() == Material.LAVA) {
                    BaseStoneSource var36 = param0.chunkGenerator().getBaseStoneSource();

                    for(int var37 = 0; var37 < 16; ++var37) {
                        for(int var38 = 0; var38 < 16; ++var38) {
                            for(int var39 = 0; var39 < 8; ++var39) {
                                boolean var40 = !var4[(var37 * 16 + var38) * 8 + var39]
                                    && (
                                        var37 < 15 && var4[((var37 + 1) * 16 + var38) * 8 + var39]
                                            || var37 > 0 && var4[((var37 - 1) * 16 + var38) * 8 + var39]
                                            || var38 < 15 && var4[(var37 * 16 + var38 + 1) * 8 + var39]
                                            || var38 > 0 && var4[(var37 * 16 + (var38 - 1)) * 8 + var39]
                                            || var39 < 7 && var4[(var37 * 16 + var38) * 8 + var39 + 1]
                                            || var39 > 0 && var4[(var37 * 16 + var38) * 8 + (var39 - 1)]
                                    );
                                if (var40
                                    && (var39 < 4 || var2.nextInt(2) != 0)
                                    && var1.getBlockState(var0.offset(var37, var39, var38)).getMaterial().isSolid()) {
                                    BlockPos var41 = var0.offset(var37, var39, var38);
                                    var1.setBlock(var41, var36.getBaseBlock(var41), 2);
                                    BlockPos var42 = var41.above();
                                    this.tryScheduleBlockTick(var1, var42, var1.getBlockState(var42));
                                }
                            }
                        }
                    }
                }

                if (var3.state.getMaterial() == Material.WATER) {
                    for(int var43 = 0; var43 < 16; ++var43) {
                        for(int var44 = 0; var44 < 16; ++var44) {
                            int var45 = 4;
                            BlockPos var46 = var0.offset(var43, 4, var44);
                            if (var1.getBiome(var46).shouldFreeze(var1, var46, false)) {
                                var1.setBlock(var46, Blocks.ICE.defaultBlockState(), 2);
                            }
                        }
                    }
                }

                return true;
            }
        }
    }

    private void tryScheduleBlockTick(WorldGenLevel param0, BlockPos param1, BlockState param2) {
        if (!param2.isAir()) {
            param0.getBlockTicks().scheduleTick(param1, param2.getBlock(), 0);
        }

    }
}
