package net.minecraft.world.level.levelgen.surfacebuilders;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BlockColumn;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;

public class NetherForestSurfaceBuilder extends SurfaceBuilder<SurfaceBuilderBaseConfiguration> {
    protected long seed;
    private PerlinNoise decorationNoise;

    public NetherForestSurfaceBuilder(Codec<SurfaceBuilderBaseConfiguration> param0) {
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
        double var0 = this.decorationNoise.getValue((double)param3 * 0.1, (double)param9, (double)param4 * 0.1);
        boolean var1 = var0 > 0.15 + param0.nextDouble() * 0.35;
        double var2 = this.decorationNoise.getValue((double)param3 * 0.1, 109.0, (double)param4 * 0.1);
        boolean var3 = var2 > 0.25 + param0.nextDouble() * 0.9;
        int var4 = (int)(param6 / 3.0 + 3.0 + param0.nextDouble() * 0.25);
        int var5 = -1;
        BlockState var6 = param12.getUnderMaterial();

        for(int var7 = 127; var7 >= param10; --var7) {
            BlockState var8 = param12.getTopMaterial();
            BlockState var9 = param1.getBlock(var7);
            if (var9.isAir()) {
                var5 = -1;
            } else if (var9.is(param7.getBlock())) {
                if (var5 == -1) {
                    boolean var10 = false;
                    if (var4 <= 0) {
                        var10 = true;
                        var6 = param12.getUnderMaterial();
                    }

                    if (var1) {
                        var8 = param12.getUnderMaterial();
                    } else if (var3) {
                        var8 = param12.getUnderwaterMaterial();
                    }

                    if (var7 < param9 && var10) {
                        var8 = param8;
                    }

                    var5 = var4;
                    if (var7 >= param9 - 1) {
                        param1.setBlock(var7, var8);
                    } else {
                        param1.setBlock(var7, var6);
                    }
                } else if (var5 > 0) {
                    --var5;
                    param1.setBlock(var7, var6);
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
