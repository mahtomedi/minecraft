package net.minecraft.world.level.levelgen.surfacebuilders;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

public class ErodedBadlandsSurfaceBuilder extends BadlandsSurfaceBuilder {
    private static final BlockState WHITE_TERRACOTTA = Blocks.WHITE_TERRACOTTA.defaultBlockState();
    private static final BlockState ORANGE_TERRACOTTA = Blocks.ORANGE_TERRACOTTA.defaultBlockState();
    private static final BlockState TERRACOTTA = Blocks.TERRACOTTA.defaultBlockState();

    public ErodedBadlandsSurfaceBuilder(Codec<SurfaceBuilderBaseConfiguration> param0) {
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
        double var0 = 0.0;
        double var1 = Math.min(Math.abs(param6), this.pillarNoise.getValue((double)param3 * 0.25, (double)param4 * 0.25, false) * 15.0);
        if (var1 > 0.0) {
            double var2 = 0.001953125;
            double var3 = Math.abs(this.pillarRoofNoise.getValue((double)param3 * 0.001953125, (double)param4 * 0.001953125, false));
            var0 = var1 * var1 * 2.5;
            double var4 = Math.ceil(var3 * 50.0) + 14.0;
            if (var0 > var4) {
                var0 = var4;
            }

            var0 += 64.0;
        }

        int var5 = param3 & 15;
        int var6 = param4 & 15;
        BlockState var7 = WHITE_TERRACOTTA;
        BlockState var8 = param2.getSurfaceBuilderConfig().getUnderMaterial();
        int var9 = (int)(param6 / 3.0 + 3.0 + param0.nextDouble() * 0.25);
        boolean var10 = Math.cos(param6 / 3.0 * Math.PI) > 0.0;
        int var11 = -1;
        boolean var12 = false;
        BlockPos.MutableBlockPos var13 = new BlockPos.MutableBlockPos();

        for(int var14 = Math.max(param5, (int)var0 + 1); var14 >= 0; --var14) {
            var13.set(var5, var14, var6);
            if (param1.getBlockState(var13).isAir() && var14 < (int)var0) {
                param1.setBlockState(var13, param7, false);
            }

            BlockState var15 = param1.getBlockState(var13);
            if (var15.isAir()) {
                var11 = -1;
            } else if (var15.is(param7.getBlock())) {
                if (var11 == -1) {
                    var12 = false;
                    if (var9 <= 0) {
                        var7 = Blocks.AIR.defaultBlockState();
                        var8 = param7;
                    } else if (var14 >= param9 - 4 && var14 <= param9 + 1) {
                        var7 = WHITE_TERRACOTTA;
                        var8 = param2.getSurfaceBuilderConfig().getUnderMaterial();
                    }

                    if (var14 < param9 && (var7 == null || var7.isAir())) {
                        var7 = param8;
                    }

                    var11 = var9 + Math.max(0, var14 - param9);
                    if (var14 >= param9 - 1) {
                        if (var14 <= param9 + 3 + var9) {
                            param1.setBlockState(var13, param2.getSurfaceBuilderConfig().getTopMaterial(), false);
                            var12 = true;
                        } else {
                            BlockState var16;
                            if (var14 < 64 || var14 > 127) {
                                var16 = ORANGE_TERRACOTTA;
                            } else if (var10) {
                                var16 = TERRACOTTA;
                            } else {
                                var16 = this.getBand(param3, var14, param4);
                            }

                            param1.setBlockState(var13, var16, false);
                        }
                    } else {
                        param1.setBlockState(var13, var8, false);
                        Block var19 = var8.getBlock();
                        if (var19 == Blocks.WHITE_TERRACOTTA
                            || var19 == Blocks.ORANGE_TERRACOTTA
                            || var19 == Blocks.MAGENTA_TERRACOTTA
                            || var19 == Blocks.LIGHT_BLUE_TERRACOTTA
                            || var19 == Blocks.YELLOW_TERRACOTTA
                            || var19 == Blocks.LIME_TERRACOTTA
                            || var19 == Blocks.PINK_TERRACOTTA
                            || var19 == Blocks.GRAY_TERRACOTTA
                            || var19 == Blocks.LIGHT_GRAY_TERRACOTTA
                            || var19 == Blocks.CYAN_TERRACOTTA
                            || var19 == Blocks.PURPLE_TERRACOTTA
                            || var19 == Blocks.BLUE_TERRACOTTA
                            || var19 == Blocks.BROWN_TERRACOTTA
                            || var19 == Blocks.GREEN_TERRACOTTA
                            || var19 == Blocks.RED_TERRACOTTA
                            || var19 == Blocks.BLACK_TERRACOTTA) {
                            param1.setBlockState(var13, ORANGE_TERRACOTTA, false);
                        }
                    }
                } else if (var11 > 0) {
                    --var11;
                    if (var12) {
                        param1.setBlockState(var13, ORANGE_TERRACOTTA, false);
                    } else {
                        param1.setBlockState(var13, this.getBand(param3, var14, param4), false);
                    }
                }
            }
        }

    }
}
