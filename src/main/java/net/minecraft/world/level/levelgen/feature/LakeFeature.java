package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.material.Material;

public class LakeFeature extends Feature<LakeConfiguration> {
    private static final BlockState AIR = Blocks.CAVE_AIR.defaultBlockState();

    public LakeFeature(Function<Dynamic<?>, ? extends LakeConfiguration> param0) {
        super(param0);
    }

    public boolean place(
        LevelAccessor param0, ChunkGenerator<? extends ChunkGeneratorSettings> param1, Random param2, BlockPos param3, LakeConfiguration param4
    ) {
        while(param3.getY() > 5 && param0.isEmptyBlock(param3)) {
            param3 = param3.below();
        }

        if (param3.getY() <= 4) {
            return false;
        } else {
            param3 = param3.below(4);
            ChunkPos var0 = new ChunkPos(param3);
            if (!param0.getChunk(var0.x, var0.z, ChunkStatus.STRUCTURE_REFERENCES).getReferencesForFeature(Feature.VILLAGE.getFeatureName()).isEmpty()) {
                return false;
            } else {
                boolean[] var1 = new boolean[2048];
                int var2 = param2.nextInt(4) + 4;

                for(int var3 = 0; var3 < var2; ++var3) {
                    double var4 = param2.nextDouble() * 6.0 + 3.0;
                    double var5 = param2.nextDouble() * 4.0 + 2.0;
                    double var6 = param2.nextDouble() * 6.0 + 3.0;
                    double var7 = param2.nextDouble() * (16.0 - var4 - 2.0) + 1.0 + var4 / 2.0;
                    double var8 = param2.nextDouble() * (8.0 - var5 - 4.0) + 2.0 + var5 / 2.0;
                    double var9 = param2.nextDouble() * (16.0 - var6 - 2.0) + 1.0 + var6 / 2.0;

                    for(int var10 = 1; var10 < 15; ++var10) {
                        for(int var11 = 1; var11 < 15; ++var11) {
                            for(int var12 = 1; var12 < 7; ++var12) {
                                double var13 = ((double)var10 - var7) / (var4 / 2.0);
                                double var14 = ((double)var12 - var8) / (var5 / 2.0);
                                double var15 = ((double)var11 - var9) / (var6 / 2.0);
                                double var16 = var13 * var13 + var14 * var14 + var15 * var15;
                                if (var16 < 1.0) {
                                    var1[(var10 * 16 + var11) * 8 + var12] = true;
                                }
                            }
                        }
                    }
                }

                for(int var17 = 0; var17 < 16; ++var17) {
                    for(int var18 = 0; var18 < 16; ++var18) {
                        for(int var19 = 0; var19 < 8; ++var19) {
                            boolean var20 = !var1[(var17 * 16 + var18) * 8 + var19]
                                && (
                                    var17 < 15 && var1[((var17 + 1) * 16 + var18) * 8 + var19]
                                        || var17 > 0 && var1[((var17 - 1) * 16 + var18) * 8 + var19]
                                        || var18 < 15 && var1[(var17 * 16 + var18 + 1) * 8 + var19]
                                        || var18 > 0 && var1[(var17 * 16 + (var18 - 1)) * 8 + var19]
                                        || var19 < 7 && var1[(var17 * 16 + var18) * 8 + var19 + 1]
                                        || var19 > 0 && var1[(var17 * 16 + var18) * 8 + (var19 - 1)]
                                );
                            if (var20) {
                                Material var21 = param0.getBlockState(param3.offset(var17, var19, var18)).getMaterial();
                                if (var19 >= 4 && var21.isLiquid()) {
                                    return false;
                                }

                                if (var19 < 4 && !var21.isSolid() && param0.getBlockState(param3.offset(var17, var19, var18)) != param4.state) {
                                    return false;
                                }
                            }
                        }
                    }
                }

                for(int var22 = 0; var22 < 16; ++var22) {
                    for(int var23 = 0; var23 < 16; ++var23) {
                        for(int var24 = 0; var24 < 8; ++var24) {
                            if (var1[(var22 * 16 + var23) * 8 + var24]) {
                                param0.setBlock(param3.offset(var22, var24, var23), var24 >= 4 ? AIR : param4.state, 2);
                            }
                        }
                    }
                }

                for(int var25 = 0; var25 < 16; ++var25) {
                    for(int var26 = 0; var26 < 16; ++var26) {
                        for(int var27 = 4; var27 < 8; ++var27) {
                            if (var1[(var25 * 16 + var26) * 8 + var27]) {
                                BlockPos var28 = param3.offset(var25, var27 - 1, var26);
                                if (Block.equalsDirt(param0.getBlockState(var28).getBlock())
                                    && param0.getBrightness(LightLayer.SKY, param3.offset(var25, var27, var26)) > 0) {
                                    Biome var29 = param0.getBiome(var28);
                                    if (var29.getSurfaceBuilderConfig().getTopMaterial().getBlock() == Blocks.MYCELIUM) {
                                        param0.setBlock(var28, Blocks.MYCELIUM.defaultBlockState(), 2);
                                    } else {
                                        param0.setBlock(var28, Blocks.GRASS_BLOCK.defaultBlockState(), 2);
                                    }
                                }
                            }
                        }
                    }
                }

                if (param4.state.getMaterial() == Material.LAVA) {
                    for(int var30 = 0; var30 < 16; ++var30) {
                        for(int var31 = 0; var31 < 16; ++var31) {
                            for(int var32 = 0; var32 < 8; ++var32) {
                                boolean var33 = !var1[(var30 * 16 + var31) * 8 + var32]
                                    && (
                                        var30 < 15 && var1[((var30 + 1) * 16 + var31) * 8 + var32]
                                            || var30 > 0 && var1[((var30 - 1) * 16 + var31) * 8 + var32]
                                            || var31 < 15 && var1[(var30 * 16 + var31 + 1) * 8 + var32]
                                            || var31 > 0 && var1[(var30 * 16 + (var31 - 1)) * 8 + var32]
                                            || var32 < 7 && var1[(var30 * 16 + var31) * 8 + var32 + 1]
                                            || var32 > 0 && var1[(var30 * 16 + var31) * 8 + (var32 - 1)]
                                    );
                                if (var33
                                    && (var32 < 4 || param2.nextInt(2) != 0)
                                    && param0.getBlockState(param3.offset(var30, var32, var31)).getMaterial().isSolid()) {
                                    param0.setBlock(param3.offset(var30, var32, var31), Blocks.STONE.defaultBlockState(), 2);
                                }
                            }
                        }
                    }
                }

                if (param4.state.getMaterial() == Material.WATER) {
                    for(int var34 = 0; var34 < 16; ++var34) {
                        for(int var35 = 0; var35 < 16; ++var35) {
                            int var36 = 4;
                            BlockPos var37 = param3.offset(var34, 4, var35);
                            if (param0.getBiome(var37).shouldFreeze(param0, var37, false)) {
                                param0.setBlock(var37, Blocks.ICE.defaultBlockState(), 2);
                            }
                        }
                    }
                }

                return true;
            }
        }
    }
}
