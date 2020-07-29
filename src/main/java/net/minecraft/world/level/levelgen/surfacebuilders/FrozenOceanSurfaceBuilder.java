package net.minecraft.world.level.levelgen.surfacebuilders;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.IntStream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;
import net.minecraft.world.level.material.Material;

public class FrozenOceanSurfaceBuilder extends SurfaceBuilder<SurfaceBuilderBaseConfiguration> {
    protected static final BlockState PACKED_ICE = Blocks.PACKED_ICE.defaultBlockState();
    protected static final BlockState SNOW_BLOCK = Blocks.SNOW_BLOCK.defaultBlockState();
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private static final BlockState GRAVEL = Blocks.GRAVEL.defaultBlockState();
    private static final BlockState ICE = Blocks.ICE.defaultBlockState();
    private PerlinSimplexNoise icebergNoise;
    private PerlinSimplexNoise icebergRoofNoise;
    private long seed;

    public FrozenOceanSurfaceBuilder(Codec<SurfaceBuilderBaseConfiguration> param0) {
        super(param0);
    }

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
        double var1 = 0.0;
        BlockPos.MutableBlockPos var2 = new BlockPos.MutableBlockPos();
        float var3 = param2.getTemperature(var2.set(param3, 63, param4));
        double var4 = Math.min(Math.abs(param6), this.icebergNoise.getValue((double)param3 * 0.1, (double)param4 * 0.1, false) * 15.0);
        if (var4 > 1.8) {
            double var5 = 0.09765625;
            double var6 = Math.abs(this.icebergRoofNoise.getValue((double)param3 * 0.09765625, (double)param4 * 0.09765625, false));
            var0 = var4 * var4 * 1.2;
            double var7 = Math.ceil(var6 * 40.0) + 14.0;
            if (var0 > var7) {
                var0 = var7;
            }

            if (var3 > 0.1F) {
                var0 -= 2.0;
            }

            if (var0 > 2.0) {
                var1 = (double)param9 - var0 - 7.0;
                var0 += (double)param9;
            } else {
                var0 = 0.0;
            }
        }

        int var8 = param3 & 15;
        int var9 = param4 & 15;
        SurfaceBuilderConfiguration var10 = param2.getGenerationSettings().getSurfaceBuilderConfig();
        BlockState var11 = var10.getUnderMaterial();
        BlockState var12 = var10.getTopMaterial();
        BlockState var13 = var11;
        BlockState var14 = var12;
        int var15 = (int)(param6 / 3.0 + 3.0 + param0.nextDouble() * 0.25);
        int var16 = -1;
        int var17 = 0;
        int var18 = 2 + param0.nextInt(4);
        int var19 = param9 + 18 + param0.nextInt(10);

        for(int var20 = Math.max(param5, (int)var0 + 1); var20 >= 0; --var20) {
            var2.set(var8, var20, var9);
            if (param1.getBlockState(var2).isAir() && var20 < (int)var0 && param0.nextDouble() > 0.01) {
                param1.setBlockState(var2, PACKED_ICE, false);
            } else if (param1.getBlockState(var2).getMaterial() == Material.WATER
                && var20 > (int)var1
                && var20 < param9
                && var1 != 0.0
                && param0.nextDouble() > 0.15) {
                param1.setBlockState(var2, PACKED_ICE, false);
            }

            BlockState var21 = param1.getBlockState(var2);
            if (var21.isAir()) {
                var16 = -1;
            } else if (!var21.is(param7.getBlock())) {
                if (var21.is(Blocks.PACKED_ICE) && var17 <= var18 && var20 > var19) {
                    param1.setBlockState(var2, SNOW_BLOCK, false);
                    ++var17;
                }
            } else if (var16 == -1) {
                if (var15 <= 0) {
                    var14 = AIR;
                    var13 = param7;
                } else if (var20 >= param9 - 4 && var20 <= param9 + 1) {
                    var14 = var12;
                    var13 = var11;
                }

                if (var20 < param9 && (var14 == null || var14.isAir())) {
                    if (param2.getTemperature(var2.set(param3, var20, param4)) < 0.15F) {
                        var14 = ICE;
                    } else {
                        var14 = param8;
                    }
                }

                var16 = var15;
                if (var20 >= param9 - 1) {
                    param1.setBlockState(var2, var14, false);
                } else if (var20 < param9 - 7 - var15) {
                    var14 = AIR;
                    var13 = param7;
                    param1.setBlockState(var2, GRAVEL, false);
                } else {
                    param1.setBlockState(var2, var13, false);
                }
            } else if (var16 > 0) {
                --var16;
                param1.setBlockState(var2, var13, false);
                if (var16 == 0 && var13.is(Blocks.SAND) && var15 > 1) {
                    var16 = param0.nextInt(4) + Math.max(0, var20 - 63);
                    var13 = var13.is(Blocks.RED_SAND) ? Blocks.RED_SANDSTONE.defaultBlockState() : Blocks.SANDSTONE.defaultBlockState();
                }
            }
        }

    }

    @Override
    public void initNoise(long param0) {
        if (this.seed != param0 || this.icebergNoise == null || this.icebergRoofNoise == null) {
            WorldgenRandom var0 = new WorldgenRandom(param0);
            this.icebergNoise = new PerlinSimplexNoise(var0, IntStream.rangeClosed(-3, 0));
            this.icebergRoofNoise = new PerlinSimplexNoise(var0, ImmutableList.of(0));
        }

        this.seed = param0;
    }
}
