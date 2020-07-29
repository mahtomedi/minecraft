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
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import net.minecraft.world.level.material.Material;

public class LakeFeature extends Feature<BlockStateConfiguration> {
    private static final BlockState AIR = Blocks.CAVE_AIR.defaultBlockState();

    public LakeFeature(Codec<BlockStateConfiguration> param0) {
        super(param0);
    }

    public boolean place(WorldGenLevel param0, ChunkGenerator param1, Random param2, BlockPos param3, BlockStateConfiguration param4) {
        while(param3.getY() > 5 && param0.isEmptyBlock(param3)) {
            param3 = param3.below();
        }

        if (param3.getY() <= 4) {
            return false;
        } else {
            param3 = param3.below(4);
            if (param0.startsForFeature(SectionPos.of(param3), StructureFeature.VILLAGE).findAny().isPresent()) {
                return false;
            } else {
                boolean[] var0 = new boolean[2048];
                int var1 = param2.nextInt(4) + 4;

                for(int var2 = 0; var2 < var1; ++var2) {
                    double var3 = param2.nextDouble() * 6.0 + 3.0;
                    double var4 = param2.nextDouble() * 4.0 + 2.0;
                    double var5 = param2.nextDouble() * 6.0 + 3.0;
                    double var6 = param2.nextDouble() * (16.0 - var3 - 2.0) + 1.0 + var3 / 2.0;
                    double var7 = param2.nextDouble() * (8.0 - var4 - 4.0) + 2.0 + var4 / 2.0;
                    double var8 = param2.nextDouble() * (16.0 - var5 - 2.0) + 1.0 + var5 / 2.0;

                    for(int var9 = 1; var9 < 15; ++var9) {
                        for(int var10 = 1; var10 < 15; ++var10) {
                            for(int var11 = 1; var11 < 7; ++var11) {
                                double var12 = ((double)var9 - var6) / (var3 / 2.0);
                                double var13 = ((double)var11 - var7) / (var4 / 2.0);
                                double var14 = ((double)var10 - var8) / (var5 / 2.0);
                                double var15 = var12 * var12 + var13 * var13 + var14 * var14;
                                if (var15 < 1.0) {
                                    var0[(var9 * 16 + var10) * 8 + var11] = true;
                                }
                            }
                        }
                    }
                }

                for(int var16 = 0; var16 < 16; ++var16) {
                    for(int var17 = 0; var17 < 16; ++var17) {
                        for(int var18 = 0; var18 < 8; ++var18) {
                            boolean var19 = !var0[(var16 * 16 + var17) * 8 + var18]
                                && (
                                    var16 < 15 && var0[((var16 + 1) * 16 + var17) * 8 + var18]
                                        || var16 > 0 && var0[((var16 - 1) * 16 + var17) * 8 + var18]
                                        || var17 < 15 && var0[(var16 * 16 + var17 + 1) * 8 + var18]
                                        || var17 > 0 && var0[(var16 * 16 + (var17 - 1)) * 8 + var18]
                                        || var18 < 7 && var0[(var16 * 16 + var17) * 8 + var18 + 1]
                                        || var18 > 0 && var0[(var16 * 16 + var17) * 8 + (var18 - 1)]
                                );
                            if (var19) {
                                Material var20 = param0.getBlockState(param3.offset(var16, var18, var17)).getMaterial();
                                if (var18 >= 4 && var20.isLiquid()) {
                                    return false;
                                }

                                if (var18 < 4 && !var20.isSolid() && param0.getBlockState(param3.offset(var16, var18, var17)) != param4.state) {
                                    return false;
                                }
                            }
                        }
                    }
                }

                for(int var21 = 0; var21 < 16; ++var21) {
                    for(int var22 = 0; var22 < 16; ++var22) {
                        for(int var23 = 0; var23 < 8; ++var23) {
                            if (var0[(var21 * 16 + var22) * 8 + var23]) {
                                param0.setBlock(param3.offset(var21, var23, var22), var23 >= 4 ? AIR : param4.state, 2);
                            }
                        }
                    }
                }

                for(int var24 = 0; var24 < 16; ++var24) {
                    for(int var25 = 0; var25 < 16; ++var25) {
                        for(int var26 = 4; var26 < 8; ++var26) {
                            if (var0[(var24 * 16 + var25) * 8 + var26]) {
                                BlockPos var27 = param3.offset(var24, var26 - 1, var25);
                                if (isDirt(param0.getBlockState(var27).getBlock())
                                    && param0.getBrightness(LightLayer.SKY, param3.offset(var24, var26, var25)) > 0) {
                                    Biome var28 = param0.getBiome(var27);
                                    if (var28.getGenerationSettings().getSurfaceBuilderConfig().getTopMaterial().is(Blocks.MYCELIUM)) {
                                        param0.setBlock(var27, Blocks.MYCELIUM.defaultBlockState(), 2);
                                    } else {
                                        param0.setBlock(var27, Blocks.GRASS_BLOCK.defaultBlockState(), 2);
                                    }
                                }
                            }
                        }
                    }
                }

                if (param4.state.getMaterial() == Material.LAVA) {
                    for(int var29 = 0; var29 < 16; ++var29) {
                        for(int var30 = 0; var30 < 16; ++var30) {
                            for(int var31 = 0; var31 < 8; ++var31) {
                                boolean var32 = !var0[(var29 * 16 + var30) * 8 + var31]
                                    && (
                                        var29 < 15 && var0[((var29 + 1) * 16 + var30) * 8 + var31]
                                            || var29 > 0 && var0[((var29 - 1) * 16 + var30) * 8 + var31]
                                            || var30 < 15 && var0[(var29 * 16 + var30 + 1) * 8 + var31]
                                            || var30 > 0 && var0[(var29 * 16 + (var30 - 1)) * 8 + var31]
                                            || var31 < 7 && var0[(var29 * 16 + var30) * 8 + var31 + 1]
                                            || var31 > 0 && var0[(var29 * 16 + var30) * 8 + (var31 - 1)]
                                    );
                                if (var32
                                    && (var31 < 4 || param2.nextInt(2) != 0)
                                    && param0.getBlockState(param3.offset(var29, var31, var30)).getMaterial().isSolid()) {
                                    param0.setBlock(param3.offset(var29, var31, var30), Blocks.STONE.defaultBlockState(), 2);
                                }
                            }
                        }
                    }
                }

                if (param4.state.getMaterial() == Material.WATER) {
                    for(int var33 = 0; var33 < 16; ++var33) {
                        for(int var34 = 0; var34 < 16; ++var34) {
                            int var35 = 4;
                            BlockPos var36 = param3.offset(var33, 4, var34);
                            if (param0.getBiome(var36).shouldFreeze(param0, var36, false)) {
                                param0.setBlock(var36, Blocks.ICE.defaultBlockState(), 2);
                            }
                        }
                    }
                }

                return true;
            }
        }
    }
}
