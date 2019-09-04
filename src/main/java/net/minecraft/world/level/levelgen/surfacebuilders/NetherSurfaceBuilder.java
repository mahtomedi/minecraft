package net.minecraft.world.level.levelgen.surfacebuilders;

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

public class NetherSurfaceBuilder extends SurfaceBuilder<SurfaceBuilderBaseConfiguration> {
    private static final BlockState AIR = Blocks.CAVE_AIR.defaultBlockState();
    private static final BlockState NETHERRACK = Blocks.NETHERRACK.defaultBlockState();
    private static final BlockState GRAVEL = Blocks.GRAVEL.defaultBlockState();
    private static final BlockState SOUL_SAND = Blocks.SOUL_SAND.defaultBlockState();
    protected long seed;
    protected PerlinNoise decorationNoise;

    public NetherSurfaceBuilder(Function<Dynamic<?>, ? extends SurfaceBuilderBaseConfiguration> param0) {
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
        int var0 = param9 + 1;
        int var1 = param3 & 15;
        int var2 = param4 & 15;
        double var3 = 0.03125;
        boolean var4 = this.decorationNoise.getValue((double)param3 * 0.03125, (double)param4 * 0.03125, 0.0) * 75.0 + param0.nextDouble() > 0.0;
        boolean var5 = this.decorationNoise.getValue((double)param3 * 0.03125, 109.0, (double)param4 * 0.03125) * 75.0 + param0.nextDouble() > 0.0;
        int var6 = (int)(param6 / 3.0 + 3.0 + param0.nextDouble() * 0.25);
        BlockPos.MutableBlockPos var7 = new BlockPos.MutableBlockPos();
        int var8 = -1;
        BlockState var9 = NETHERRACK;
        BlockState var10 = NETHERRACK;

        for(int var11 = 127; var11 >= 0; --var11) {
            var7.set(var1, var11, var2);
            BlockState var12 = param1.getBlockState(var7);
            if (var12.getBlock() != null && !var12.isAir()) {
                if (var12.getBlock() == param7.getBlock()) {
                    if (var8 == -1) {
                        if (var6 <= 0) {
                            var9 = AIR;
                            var10 = NETHERRACK;
                        } else if (var11 >= var0 - 4 && var11 <= var0 + 1) {
                            var9 = NETHERRACK;
                            var10 = NETHERRACK;
                            if (var5) {
                                var9 = GRAVEL;
                                var10 = NETHERRACK;
                            }

                            if (var4) {
                                var9 = SOUL_SAND;
                                var10 = SOUL_SAND;
                            }
                        }

                        if (var11 < var0 && (var9 == null || var9.isAir())) {
                            var9 = param8;
                        }

                        var8 = var6;
                        if (var11 >= var0 - 1) {
                            param1.setBlockState(var7, var9, false);
                        } else {
                            param1.setBlockState(var7, var10, false);
                        }
                    } else if (var8 > 0) {
                        --var8;
                        param1.setBlockState(var7, var10, false);
                    }
                }
            } else {
                var8 = -1;
            }
        }

    }

    @Override
    public void initNoise(long param0) {
        if (this.seed != param0 || this.decorationNoise == null) {
            this.decorationNoise = new PerlinNoise(new WorldgenRandom(param0), 3, 0);
        }

        this.seed = param0;
    }
}
