package net.minecraft.world.level.levelgen.surfacebuilders;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.IntStream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BlockColumn;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
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

        SurfaceBuilderConfiguration var8 = param2.getGenerationSettings().getSurfaceBuilderConfig();
        BlockState var9 = var8.getUnderMaterial();
        BlockState var10 = var8.getTopMaterial();
        BlockState var11 = var9;
        BlockState var12 = var10;
        int var13 = (int)(param6 / 3.0 + 3.0 + param0.nextDouble() * 0.25);
        int var14 = -1;
        int var15 = 0;
        int var16 = 2 + param0.nextInt(4);
        int var17 = param9 + 18 + param0.nextInt(10);

        for(int var18 = Math.max(param5, (int)var0 + 1); var18 >= param10; --var18) {
            if (param1.getBlock(var18).isAir() && var18 < (int)var0 && param0.nextDouble() > 0.01) {
                param1.setBlock(var18, PACKED_ICE);
            } else if (param1.getBlock(var18).getMaterial() == Material.WATER
                && var18 > (int)var1
                && var18 < param9
                && var1 != 0.0
                && param0.nextDouble() > 0.15) {
                param1.setBlock(var18, PACKED_ICE);
            }

            BlockState var19 = param1.getBlock(var18);
            if (var19.isAir()) {
                var14 = -1;
            } else if (!var19.is(param7.getBlock())) {
                if (var19.is(Blocks.PACKED_ICE) && var15 <= var16 && var18 > var17) {
                    param1.setBlock(var18, SNOW_BLOCK);
                    ++var15;
                }
            } else if (var14 == -1) {
                if (var13 <= 0) {
                    var12 = AIR;
                    var11 = param7;
                } else if (var18 >= param9 - 4 && var18 <= param9 + 1) {
                    var12 = var10;
                    var11 = var9;
                }

                if (var18 < param9 && (var12 == null || var12.isAir())) {
                    if (param2.getTemperature(var2.set(param3, var18, param4)) < 0.15F) {
                        var12 = ICE;
                    } else {
                        var12 = param8;
                    }
                }

                var14 = var13;
                if (var18 >= param9 - 1) {
                    param1.setBlock(var18, var12);
                } else if (var18 < param9 - 7 - var13) {
                    var12 = AIR;
                    var11 = param7;
                    param1.setBlock(var18, GRAVEL);
                } else {
                    param1.setBlock(var18, var11);
                }
            } else if (var14 > 0) {
                --var14;
                param1.setBlock(var18, var11);
                if (var14 == 0 && var11.is(Blocks.SAND) && var13 > 1) {
                    var14 = param0.nextInt(4) + Math.max(0, var18 - 63);
                    var11 = var11.is(Blocks.RED_SAND) ? Blocks.RED_SANDSTONE.defaultBlockState() : Blocks.SANDSTONE.defaultBlockState();
                }
            }
        }

    }

    @Override
    public void initNoise(long param0) {
        if (this.seed != param0 || this.icebergNoise == null || this.icebergRoofNoise == null) {
            WorldgenRandom var0 = new WorldgenRandom(new LegacyRandomSource(param0));
            this.icebergNoise = new PerlinSimplexNoise(var0, IntStream.rangeClosed(-3, 0));
            this.icebergRoofNoise = new PerlinSimplexNoise(var0, ImmutableList.of(0));
        }

        this.seed = param0;
    }
}
