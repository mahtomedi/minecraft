package net.minecraft.world.level.levelgen.surfacebuilders;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;

public class NetherForestSurfaceBuilder extends SurfaceBuilder<SurfaceBuilderBaseConfiguration> {
    private static final BlockState AIR = Blocks.CAVE_AIR.defaultBlockState();
    protected long seed;
    private PerlinNoise decorationNoise;

    public NetherForestSurfaceBuilder(
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
        int var0 = param9 + 1;
        int var1 = param3 & 15;
        int var2 = param4 & 15;
        double var3 = this.decorationNoise.getValue((double)param3 * 0.1, (double)param9, (double)param4 * 0.1);
        boolean var4 = var3 > 0.15 + param0.nextDouble() * 0.35;
        double var5 = this.decorationNoise.getValue((double)param3 * 0.1, 109.0, (double)param4 * 0.1);
        boolean var6 = var5 > 0.25 + param0.nextDouble() * 0.9;
        int var7 = (int)(param6 / 3.0 + 3.0 + param0.nextDouble() * 0.25);
        BlockPos.MutableBlockPos var8 = new BlockPos.MutableBlockPos();
        int var9 = -1;
        BlockState var10 = param11.getUnderMaterial();

        for(int var11 = 127; var11 >= 0; --var11) {
            var8.set(var1, var11, var2);
            BlockState var12 = param11.getTopMaterial();
            BlockState var13 = param1.getBlockState(var8);
            if (var13.isAir()) {
                var9 = -1;
            } else if (var13.getBlock() == param7.getBlock()) {
                if (var9 == -1) {
                    if (var7 <= 0) {
                        var12 = AIR;
                        var10 = param11.getUnderMaterial();
                    }

                    if (var4) {
                        var12 = param11.getUnderMaterial();
                    } else if (var6) {
                        var12 = param11.getUnderwaterMaterial();
                    }

                    if (var11 < var0 && var12.isAir()) {
                        var12 = param8;
                    }

                    var9 = var7;
                    if (var11 >= var0 - 1) {
                        param1.setBlockState(var8, var12, false);
                    } else {
                        param1.setBlockState(var8, var10, false);
                    }
                } else if (var9 > 0) {
                    --var9;
                    param1.setBlockState(var8, var10, false);
                }
            }
        }

    }

    @Override
    public void initNoise(long param0) {
        if (this.seed != param0 || this.decorationNoise == null) {
            this.decorationNoise = new PerlinNoise(new WorldgenRandom(param0), ImmutableList.of(0));
        }

        this.seed = param0;
    }
}
