package net.minecraft.world.level.levelgen.surfacebuilders;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BlockColumn;

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
        BlockColumn param1,
        Biome param2,
        int param3,
        int param4,
        int param5,
        double param6,
        BlockState param7,
        BlockState param8,
        int param9,
        int param10,
        long param11,
        SurfaceBuilderBaseConfiguration param12
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

        BlockState var5 = WHITE_TERRACOTTA;
        SurfaceBuilderConfiguration var6 = param2.getGenerationSettings().getSurfaceBuilderConfig();
        BlockState var7 = var6.getUnderMaterial();
        BlockState var8 = var6.getTopMaterial();
        BlockState var9 = var7;
        int var10 = (int)(param6 / 3.0 + 3.0 + param0.nextDouble() * 0.25);
        boolean var11 = Math.cos(param6 / 3.0 * Math.PI) > 0.0;
        int var12 = -1;
        boolean var13 = false;

        for(int var14 = Math.max(param5, (int)var0 + 1); var14 >= param10; --var14) {
            BlockState var15 = param1.getBlock(var14);
            if (var15.is(param7.getBlock())) {
                break;
            }

            if (var15.is(Blocks.WATER)) {
                return;
            }
        }

        for(int var16 = Math.max(param5, (int)var0 + 1); var16 >= param10; --var16) {
            if (param1.getBlock(var16).isAir() && var16 < (int)var0) {
                param1.setBlock(var16, param7);
            }

            BlockState var17 = param1.getBlock(var16);
            if (var17.isAir()) {
                var12 = -1;
            } else if (var17.is(param7.getBlock())) {
                if (var12 == -1) {
                    var13 = false;
                    if (var10 <= 0) {
                        var5 = Blocks.AIR.defaultBlockState();
                        var9 = param7;
                    } else if (var16 >= param9 - 4 && var16 <= param9 + 1) {
                        var5 = WHITE_TERRACOTTA;
                        var9 = var7;
                    }

                    if (var16 < param9 && (var5 == null || var5.isAir())) {
                        var5 = param8;
                    }

                    var12 = var10 + Math.max(0, var16 - param9);
                    if (var16 >= param9 - 1) {
                        if (var16 <= param9 + 10 + var10) {
                            param1.setBlock(var16, var8);
                            var13 = true;
                        } else {
                            BlockState var18;
                            if (var16 < 64 || var16 > 159) {
                                var18 = ORANGE_TERRACOTTA;
                            } else if (var11) {
                                var18 = TERRACOTTA;
                            } else {
                                var18 = this.getBand(param3, var16, param4);
                            }

                            param1.setBlock(var16, var18);
                        }
                    } else {
                        param1.setBlock(var16, var9);
                        if (var9.is(Blocks.WHITE_TERRACOTTA)
                            || var9.is(Blocks.ORANGE_TERRACOTTA)
                            || var9.is(Blocks.MAGENTA_TERRACOTTA)
                            || var9.is(Blocks.LIGHT_BLUE_TERRACOTTA)
                            || var9.is(Blocks.YELLOW_TERRACOTTA)
                            || var9.is(Blocks.LIME_TERRACOTTA)
                            || var9.is(Blocks.PINK_TERRACOTTA)
                            || var9.is(Blocks.GRAY_TERRACOTTA)
                            || var9.is(Blocks.LIGHT_GRAY_TERRACOTTA)
                            || var9.is(Blocks.CYAN_TERRACOTTA)
                            || var9.is(Blocks.PURPLE_TERRACOTTA)
                            || var9.is(Blocks.BLUE_TERRACOTTA)
                            || var9.is(Blocks.BROWN_TERRACOTTA)
                            || var9.is(Blocks.GREEN_TERRACOTTA)
                            || var9.is(Blocks.RED_TERRACOTTA)
                            || var9.is(Blocks.BLACK_TERRACOTTA)) {
                            param1.setBlock(var16, ORANGE_TERRACOTTA);
                        }
                    }
                } else if (var12 > 0) {
                    --var12;
                    if (var13) {
                        param1.setBlock(var16, ORANGE_TERRACOTTA);
                    } else {
                        param1.setBlock(var16, this.getBand(param3, var16, param4));
                    }
                }
            }
        }

    }
}
