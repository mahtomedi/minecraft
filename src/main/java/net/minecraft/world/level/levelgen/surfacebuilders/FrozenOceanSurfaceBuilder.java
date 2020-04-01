package net.minecraft.world.level.levelgen.surfacebuilders;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
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

    public FrozenOceanSurfaceBuilder(
        Function<Dynamic<?>, ? extends SurfaceBuilderBaseConfiguration> param0, Function<Random, ? extends SurfaceBuilderBaseConfiguration> param1
    ) {
        super(param0, param1);
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
        BlockState var10 = param2.getSurfaceBuilderConfig().getUnderMaterial();
        BlockState var11 = param2.getSurfaceBuilderConfig().getTopMaterial();
        int var12 = (int)(param6 / 3.0 + 3.0 + param0.nextDouble() * 0.25);
        int var13 = -1;
        int var14 = 0;
        int var15 = 2 + param0.nextInt(4);
        int var16 = param9 + 18 + param0.nextInt(10);

        for(int var17 = Math.max(param5, (int)var0 + 1); var17 >= 0; --var17) {
            var2.set(var8, var17, var9);
            if (param1.getBlockState(var2).isAir() && var17 < (int)var0 && param0.nextDouble() > 0.01) {
                param1.setBlockState(var2, PACKED_ICE, false);
            } else if (param1.getBlockState(var2).getMaterial() == Material.WATER
                && var17 > (int)var1
                && var17 < param9
                && var1 != 0.0
                && param0.nextDouble() > 0.15) {
                param1.setBlockState(var2, PACKED_ICE, false);
            }

            BlockState var18 = param1.getBlockState(var2);
            if (var18.isAir()) {
                var13 = -1;
            } else if (var18.getBlock() != param7.getBlock()) {
                if (var18.getBlock() == Blocks.PACKED_ICE && var14 <= var15 && var17 > var16) {
                    param1.setBlockState(var2, SNOW_BLOCK, false);
                    ++var14;
                }
            } else if (var13 == -1) {
                if (var12 <= 0) {
                    var11 = AIR;
                    var10 = param7;
                } else if (var17 >= param9 - 4 && var17 <= param9 + 1) {
                    var11 = param2.getSurfaceBuilderConfig().getTopMaterial();
                    var10 = param2.getSurfaceBuilderConfig().getUnderMaterial();
                }

                if (var17 < param9 && (var11 == null || var11.isAir())) {
                    if (param2.getTemperature(var2.set(param3, var17, param4)) < 0.15F) {
                        var11 = ICE;
                    } else {
                        var11 = param8;
                    }
                }

                var13 = var12;
                if (var17 >= param9 - 1) {
                    param1.setBlockState(var2, var11, false);
                } else if (var17 < param9 - 7 - var12) {
                    var11 = AIR;
                    var10 = param7;
                    param1.setBlockState(var2, GRAVEL, false);
                } else {
                    param1.setBlockState(var2, var10, false);
                }
            } else if (var13 > 0) {
                --var13;
                param1.setBlockState(var2, var10, false);
                if (var13 == 0 && var10.getBlock() == Blocks.SAND && var12 > 1) {
                    var13 = param0.nextInt(4) + Math.max(0, var17 - 63);
                    var10 = var10.getBlock() == Blocks.RED_SAND ? Blocks.RED_SANDSTONE.defaultBlockState() : Blocks.SANDSTONE.defaultBlockState();
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
