package net.minecraft.world.level.levelgen.surfacebuilders;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
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
        SurfaceBuilderConfiguration var8 = param2.getGenerationSettings().getSurfaceBuilderConfig();
        BlockState var9 = var8.getUnderMaterial();
        BlockState var10 = var8.getTopMaterial();
        BlockState var11 = var9;
        int var12 = (int)(param6 / 3.0 + 3.0 + param0.nextDouble() * 0.25);
        boolean var13 = Math.cos(param6 / 3.0 * Math.PI) > 0.0;
        int var14 = -1;
        boolean var15 = false;
        BlockPos.MutableBlockPos var16 = new BlockPos.MutableBlockPos();

        for(int var17 = Math.max(param5, (int)var0 + 1); var17 >= 0; --var17) {
            var16.set(var5, var17, var6);
            if (param1.getBlockState(var16).isAir() && var17 < (int)var0) {
                param1.setBlockState(var16, param7, false);
            }

            BlockState var18 = param1.getBlockState(var16);
            if (var18.isAir()) {
                var14 = -1;
            } else if (var18.is(param7.getBlock())) {
                if (var14 == -1) {
                    var15 = false;
                    if (var12 <= 0) {
                        var7 = Blocks.AIR.defaultBlockState();
                        var11 = param7;
                    } else if (var17 >= param9 - 4 && var17 <= param9 + 1) {
                        var7 = WHITE_TERRACOTTA;
                        var11 = var9;
                    }

                    if (var17 < param9 && (var7 == null || var7.isAir())) {
                        var7 = param8;
                    }

                    var14 = var12 + Math.max(0, var17 - param9);
                    if (var17 >= param9 - 1) {
                        if (var17 <= param9 + 3 + var12) {
                            param1.setBlockState(var16, var10, false);
                            var15 = true;
                        } else {
                            BlockState var19;
                            if (var17 < 64 || var17 > 127) {
                                var19 = ORANGE_TERRACOTTA;
                            } else if (var13) {
                                var19 = TERRACOTTA;
                            } else {
                                var19 = this.getBand(param3, var17, param4);
                            }

                            param1.setBlockState(var16, var19, false);
                        }
                    } else {
                        param1.setBlockState(var16, var11, false);
                        if (var11.is(Blocks.WHITE_TERRACOTTA)
                            || var11.is(Blocks.ORANGE_TERRACOTTA)
                            || var11.is(Blocks.MAGENTA_TERRACOTTA)
                            || var11.is(Blocks.LIGHT_BLUE_TERRACOTTA)
                            || var11.is(Blocks.YELLOW_TERRACOTTA)
                            || var11.is(Blocks.LIME_TERRACOTTA)
                            || var11.is(Blocks.PINK_TERRACOTTA)
                            || var11.is(Blocks.GRAY_TERRACOTTA)
                            || var11.is(Blocks.LIGHT_GRAY_TERRACOTTA)
                            || var11.is(Blocks.CYAN_TERRACOTTA)
                            || var11.is(Blocks.PURPLE_TERRACOTTA)
                            || var11.is(Blocks.BLUE_TERRACOTTA)
                            || var11.is(Blocks.BROWN_TERRACOTTA)
                            || var11.is(Blocks.GREEN_TERRACOTTA)
                            || var11.is(Blocks.RED_TERRACOTTA)
                            || var11.is(Blocks.BLACK_TERRACOTTA)) {
                            param1.setBlockState(var16, ORANGE_TERRACOTTA, false);
                        }
                    }
                } else if (var14 > 0) {
                    --var14;
                    if (var15) {
                        param1.setBlockState(var16, ORANGE_TERRACOTTA, false);
                    } else {
                        param1.setBlockState(var16, this.getBand(param3, var17, param4), false);
                    }
                }
            }
        }

    }
}
