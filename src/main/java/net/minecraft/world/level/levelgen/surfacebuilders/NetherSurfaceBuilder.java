package net.minecraft.world.level.levelgen.surfacebuilders;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.IntStream;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BlockColumn;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;

public class NetherSurfaceBuilder extends SurfaceBuilder<SurfaceBuilderBaseConfiguration> {
    private static final BlockState GRAVEL = Blocks.GRAVEL.defaultBlockState();
    private static final BlockState SOUL_SAND = Blocks.SOUL_SAND.defaultBlockState();
    protected long seed;
    protected PerlinNoise decorationNoise;

    public NetherSurfaceBuilder(Codec<SurfaceBuilderBaseConfiguration> param0) {
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
        int var0 = param9;
        double var1 = 0.03125;
        boolean var2 = this.decorationNoise.getValue((double)param3 * 0.03125, (double)param4 * 0.03125, 0.0) * 75.0 + param0.nextDouble() > 0.0;
        boolean var3 = this.decorationNoise.getValue((double)param3 * 0.03125, 109.0, (double)param4 * 0.03125) * 75.0 + param0.nextDouble() > 0.0;
        int var4 = (int)(param6 / 3.0 + 3.0 + param0.nextDouble() * 0.25);
        int var5 = -1;
        BlockState var6 = param12.getTopMaterial();
        BlockState var7 = param12.getUnderMaterial();

        for(int var8 = 127; var8 >= param10; --var8) {
            BlockState var9 = param1.getBlock(var8);
            if (var9.isAir()) {
                var5 = -1;
            } else if (var9.is(param7.getBlock())) {
                if (var5 == -1) {
                    boolean var10 = false;
                    if (var4 <= 0) {
                        var10 = true;
                        var7 = param12.getUnderMaterial();
                    } else if (var8 >= var0 - 4 && var8 <= var0 + 1) {
                        var6 = param12.getTopMaterial();
                        var7 = param12.getUnderMaterial();
                        if (var3) {
                            var6 = GRAVEL;
                            var7 = param12.getUnderMaterial();
                        }

                        if (var2) {
                            var6 = SOUL_SAND;
                            var7 = SOUL_SAND;
                        }
                    }

                    if (var8 < var0 && var10) {
                        var6 = param8;
                    }

                    var5 = var4;
                    if (var8 >= var0 - 1) {
                        param1.setBlock(var8, var6);
                    } else {
                        param1.setBlock(var8, var7);
                    }
                } else if (var5 > 0) {
                    --var5;
                    param1.setBlock(var8, var7);
                }
            }
        }

    }

    @Override
    public void initNoise(long param0) {
        if (this.seed != param0 || this.decorationNoise == null) {
            this.decorationNoise = new PerlinNoise(new WorldgenRandom(param0), IntStream.rangeClosed(-3, 0));
        }

        this.seed = param0;
    }
}
