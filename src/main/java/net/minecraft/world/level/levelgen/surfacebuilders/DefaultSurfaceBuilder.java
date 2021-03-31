package net.minecraft.world.level.levelgen.surfacebuilders;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

public class DefaultSurfaceBuilder extends SurfaceBuilder<SurfaceBuilderBaseConfiguration> {
    private static final int LOWEST_Y_TO_BUILD_SURFACE_ON = 50;

    public DefaultSurfaceBuilder(Codec<SurfaceBuilderBaseConfiguration> param0) {
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
        this.apply(
            param0,
            param1,
            param2,
            param3,
            param4,
            param5,
            param6,
            param7,
            param8,
            param11.getTopMaterial(),
            param11.getUnderMaterial(),
            param11.getUnderwaterMaterial(),
            param9
        );
    }

    protected void apply(
        Random param0,
        ChunkAccess param1,
        Biome param2,
        int param3,
        int param4,
        int param5,
        double param6,
        BlockState param7,
        BlockState param8,
        BlockState param9,
        BlockState param10,
        BlockState param11,
        int param12
    ) {
        BlockPos.MutableBlockPos var0 = new BlockPos.MutableBlockPos();
        int var1 = (int)(param6 / 3.0 + 3.0 + param0.nextDouble() * 0.25);
        if (var1 == 0) {
            boolean var2 = false;

            for(int var3 = param5; var3 >= 50; --var3) {
                var0.set(param3, var3, param4);
                BlockState var4 = param1.getBlockState(var0);
                if (var4.isAir()) {
                    var2 = false;
                } else if (var4.is(param7.getBlock())) {
                    if (!var2) {
                        BlockState var5;
                        if (var3 >= param12) {
                            var5 = Blocks.AIR.defaultBlockState();
                        } else if (var3 == param12 - 1) {
                            var5 = param2.getTemperature(var0) < 0.15F ? Blocks.ICE.defaultBlockState() : param8;
                        } else if (var3 >= param12 - (7 + var1)) {
                            var5 = param7;
                        } else {
                            var5 = param11;
                        }

                        param1.setBlockState(var0, var5, false);
                    }

                    var2 = true;
                }
            }
        } else {
            BlockState var9 = param10;
            int var10 = -1;

            for(int var11 = param5; var11 >= 50; --var11) {
                var0.set(param3, var11, param4);
                BlockState var12 = param1.getBlockState(var0);
                if (var12.isAir()) {
                    var10 = -1;
                } else if (var12.is(param7.getBlock())) {
                    if (var10 == -1) {
                        var10 = var1;
                        BlockState var13;
                        if (var11 >= param12 + 2) {
                            var13 = param9;
                        } else if (var11 >= param12 - 1) {
                            var9 = param10;
                            var13 = param9;
                        } else if (var11 >= param12 - 4) {
                            var9 = param10;
                            var13 = param10;
                        } else if (var11 >= param12 - (7 + var1)) {
                            var13 = var9;
                        } else {
                            var9 = param7;
                            var13 = param11;
                        }

                        param1.setBlockState(var0, var13, false);
                    } else if (var10 > 0) {
                        --var10;
                        param1.setBlockState(var0, var9, false);
                        if (var10 == 0 && var9.is(Blocks.SAND) && var1 > 1) {
                            var10 = param0.nextInt(4) + Math.max(0, var11 - param12);
                            var9 = var9.is(Blocks.RED_SAND) ? Blocks.RED_SANDSTONE.defaultBlockState() : Blocks.SANDSTONE.defaultBlockState();
                        }
                    }
                }
            }
        }

    }
}
