package net.minecraft.world.level.levelgen.surfacebuilders;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BlockColumn;

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
        BlockState var0 = WHITE_TERRACOTTA;
        SurfaceBuilderConfiguration var1 = param2.getGenerationSettings().getSurfaceBuilderConfig();
        BlockState var2 = var1.getUnderMaterial();
        BlockState var3 = var1.getTopMaterial();
        BlockState var4 = var2;
        int var5 = (int)(param6 / 3.0 + 3.0 + param0.nextDouble() * 0.25);
        boolean var6 = Math.cos(param6 / 3.0 * Math.PI) > 0.0;
        int var7 = -1;
        boolean var8 = false;
        int var9 = 0;

        for(int var10 = param5; var10 >= param10; --var10) {
            if (var9 < 15) {
                BlockState var11 = param1.getBlock(var10);
                if (var11.isAir()) {
                    var7 = -1;
                } else if (var11.is(param7.getBlock())) {
                    if (var7 == -1) {
                        var8 = false;
                        if (var5 <= 0) {
                            var0 = Blocks.AIR.defaultBlockState();
                            var4 = param7;
                        } else if (var10 >= param9 - 4 && var10 <= param9 + 1) {
                            var0 = WHITE_TERRACOTTA;
                            var4 = var2;
                        }

                        if (var10 < param9 && (var0 == null || var0.isAir())) {
                            var0 = param8;
                        }

                        var7 = var5 + Math.max(0, var10 - param9);
                        if (var10 >= param9 - 1) {
                            if (var10 > 96 + var5 * 2) {
                                if (var6) {
                                    param1.setBlock(var10, Blocks.COARSE_DIRT.defaultBlockState());
                                } else {
                                    param1.setBlock(var10, Blocks.GRASS_BLOCK.defaultBlockState());
                                }
                            } else if (var10 > param9 + 10 + var5) {
                                BlockState var12;
                                if (var10 < 64 || var10 > 159) {
                                    var12 = ORANGE_TERRACOTTA;
                                } else if (var6) {
                                    var12 = TERRACOTTA;
                                } else {
                                    var12 = this.getBand(param3, var10, param4);
                                }

                                param1.setBlock(var10, var12);
                            } else {
                                param1.setBlock(var10, var3);
                                var8 = true;
                            }
                        } else {
                            param1.setBlock(var10, var4);
                            if (var4 == WHITE_TERRACOTTA) {
                                param1.setBlock(var10, ORANGE_TERRACOTTA);
                            }
                        }
                    } else if (var7 > 0) {
                        --var7;
                        if (var8) {
                            param1.setBlock(var10, ORANGE_TERRACOTTA);
                        } else {
                            param1.setBlock(var10, this.getBand(param3, var10, param4));
                        }
                    }

                    ++var9;
                }
            }
        }

    }
}
