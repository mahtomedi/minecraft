package net.minecraft.world.level.levelgen.surfacebuilders;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

public class WoodedBadlandsSurfaceBuilder extends BadlandsSurfaceBuilder {
    private static final BlockState WHITE_TERRACOTTA = Blocks.WHITE_TERRACOTTA.defaultBlockState();
    private static final BlockState ORANGE_TERRACOTTA = Blocks.ORANGE_TERRACOTTA.defaultBlockState();
    private static final BlockState TERRACOTTA = Blocks.TERRACOTTA.defaultBlockState();

    public WoodedBadlandsSurfaceBuilder(Codec<SurfaceBuilderBaseConfiguration> param0) {
        super(param0);
    }

    @Override
    public void apply(
        Random param0,
        ChunkAccess param1,
        Biome param2,
        int param3,
        int param4,
        int param5,
        double param6,
        BlockState param7,
        BlockState param8,
        int param9,
        long param10,
        SurfaceBuilderBaseConfiguration param11
    ) {
        int var0 = param3 & 15;
        int var1 = param4 & 15;
        BlockState var2 = WHITE_TERRACOTTA;
        SurfaceBuilderConfiguration var3 = param2.getGenerationSettings().getSurfaceBuilderConfig();
        BlockState var4 = var3.getUnderMaterial();
        BlockState var5 = var3.getTopMaterial();
        BlockState var6 = var4;
        int var7 = (int)(param6 / 3.0 + 3.0 + param0.nextDouble() * 0.25);
        boolean var8 = Math.cos(param6 / 3.0 * Math.PI) > 0.0;
        int var9 = -1;
        boolean var10 = false;
        int var11 = 0;
        BlockPos.MutableBlockPos var12 = new BlockPos.MutableBlockPos();

        for(int var13 = param5; var13 >= 0; --var13) {
            if (var11 < 15) {
                var12.set(var0, var13, var1);
                BlockState var14 = param1.getBlockState(var12);
                if (var14.isAir()) {
                    var9 = -1;
                } else if (var14.is(param7.getBlock())) {
                    if (var9 == -1) {
                        var10 = false;
                        if (var7 <= 0) {
                            var2 = Blocks.AIR.defaultBlockState();
                            var6 = param7;
                        } else if (var13 >= param9 - 4 && var13 <= param9 + 1) {
                            var2 = WHITE_TERRACOTTA;
                            var6 = var4;
                        }

                        if (var13 < param9 && (var2 == null || var2.isAir())) {
                            var2 = param8;
                        }

                        var9 = var7 + Math.max(0, var13 - param9);
                        if (var13 >= param9 - 1) {
                            if (var13 > 86 + var7 * 2) {
                                if (var8) {
                                    param1.setBlockState(var12, Blocks.COARSE_DIRT.defaultBlockState(), false);
                                } else {
                                    param1.setBlockState(var12, Blocks.GRASS_BLOCK.defaultBlockState(), false);
                                }
                            } else if (var13 > param9 + 3 + var7) {
                                BlockState var15;
                                if (var13 < 64 || var13 > 127) {
                                    var15 = ORANGE_TERRACOTTA;
                                } else if (var8) {
                                    var15 = TERRACOTTA;
                                } else {
                                    var15 = this.getBand(param3, var13, param4);
                                }

                                param1.setBlockState(var12, var15, false);
                            } else {
                                param1.setBlockState(var12, var5, false);
                                var10 = true;
                            }
                        } else {
                            param1.setBlockState(var12, var6, false);
                            if (var6 == WHITE_TERRACOTTA) {
                                param1.setBlockState(var12, ORANGE_TERRACOTTA, false);
                            }
                        }
                    } else if (var9 > 0) {
                        --var9;
                        if (var10) {
                            param1.setBlockState(var12, ORANGE_TERRACOTTA, false);
                        } else {
                            param1.setBlockState(var12, this.getBand(param3, var13, param4), false);
                        }
                    }

                    ++var11;
                }
            }
        }

    }
}
