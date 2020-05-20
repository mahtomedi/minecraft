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
        BlockState var3 = param2.getSurfaceBuilderConfig().getUnderMaterial();
        int var4 = (int)(param6 / 3.0 + 3.0 + param0.nextDouble() * 0.25);
        boolean var5 = Math.cos(param6 / 3.0 * Math.PI) > 0.0;
        int var6 = -1;
        boolean var7 = false;
        int var8 = 0;
        BlockPos.MutableBlockPos var9 = new BlockPos.MutableBlockPos();

        for(int var10 = param5; var10 >= 0; --var10) {
            if (var8 < 15) {
                var9.set(var0, var10, var1);
                BlockState var11 = param1.getBlockState(var9);
                if (var11.isAir()) {
                    var6 = -1;
                } else if (var11.is(param7.getBlock())) {
                    if (var6 == -1) {
                        var7 = false;
                        if (var4 <= 0) {
                            var2 = Blocks.AIR.defaultBlockState();
                            var3 = param7;
                        } else if (var10 >= param9 - 4 && var10 <= param9 + 1) {
                            var2 = WHITE_TERRACOTTA;
                            var3 = param2.getSurfaceBuilderConfig().getUnderMaterial();
                        }

                        if (var10 < param9 && (var2 == null || var2.isAir())) {
                            var2 = param8;
                        }

                        var6 = var4 + Math.max(0, var10 - param9);
                        if (var10 >= param9 - 1) {
                            if (var10 > 86 + var4 * 2) {
                                if (var5) {
                                    param1.setBlockState(var9, Blocks.COARSE_DIRT.defaultBlockState(), false);
                                } else {
                                    param1.setBlockState(var9, Blocks.GRASS_BLOCK.defaultBlockState(), false);
                                }
                            } else if (var10 > param9 + 3 + var4) {
                                BlockState var12;
                                if (var10 < 64 || var10 > 127) {
                                    var12 = ORANGE_TERRACOTTA;
                                } else if (var5) {
                                    var12 = TERRACOTTA;
                                } else {
                                    var12 = this.getBand(param3, var10, param4);
                                }

                                param1.setBlockState(var9, var12, false);
                            } else {
                                param1.setBlockState(var9, param2.getSurfaceBuilderConfig().getTopMaterial(), false);
                                var7 = true;
                            }
                        } else {
                            param1.setBlockState(var9, var3, false);
                            if (var3 == WHITE_TERRACOTTA) {
                                param1.setBlockState(var9, ORANGE_TERRACOTTA, false);
                            }
                        }
                    } else if (var6 > 0) {
                        --var6;
                        if (var7) {
                            param1.setBlockState(var9, ORANGE_TERRACOTTA, false);
                        } else {
                            param1.setBlockState(var9, this.getBand(param3, var10, param4), false);
                        }
                    }

                    ++var8;
                }
            }
        }

    }
}
