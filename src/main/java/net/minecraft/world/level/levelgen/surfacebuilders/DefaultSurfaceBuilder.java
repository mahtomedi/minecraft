package net.minecraft.world.level.levelgen.surfacebuilders;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

public class DefaultSurfaceBuilder extends SurfaceBuilder<SurfaceBuilderBaseConfiguration> {
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
        BlockState var0 = param9;
        BlockState var1 = param10;
        BlockPos.MutableBlockPos var2 = new BlockPos.MutableBlockPos();
        int var3 = -1;
        int var4 = (int)(param6 / 3.0 + 3.0 + param0.nextDouble() * 0.25);
        int var5 = param3 & 15;
        int var6 = param4 & 15;

        for(int var7 = param5; var7 >= 0; --var7) {
            var2.set(var5, var7, var6);
            BlockState var8 = param1.getBlockState(var2);
            if (var8.isAir()) {
                var3 = -1;
            } else if (var8.is(param7.getBlock())) {
                if (var3 == -1) {
                    if (var4 <= 0) {
                        var0 = Blocks.AIR.defaultBlockState();
                        var1 = param7;
                    } else if (var7 >= param12 - 4 && var7 <= param12 + 1) {
                        var0 = param9;
                        var1 = param10;
                    }

                    if (var7 < param12 && (var0 == null || var0.isAir())) {
                        if (param2.getTemperature(var2.set(param3, var7, param4)) < 0.15F) {
                            var0 = Blocks.ICE.defaultBlockState();
                        } else {
                            var0 = param8;
                        }

                        var2.set(var5, var7, var6);
                    }

                    var3 = var4;
                    if (var7 >= param12 - 1) {
                        param1.setBlockState(var2, var0, false);
                    } else if (var7 < param12 - 7 - var4) {
                        var0 = Blocks.AIR.defaultBlockState();
                        var1 = param7;
                        param1.setBlockState(var2, param11, false);
                    } else {
                        param1.setBlockState(var2, var1, false);
                    }
                } else if (var3 > 0) {
                    --var3;
                    param1.setBlockState(var2, var1, false);
                    if (var3 == 0 && var1.is(Blocks.SAND) && var4 > 1) {
                        var3 = param0.nextInt(4) + Math.max(0, var7 - 63);
                        var1 = var1.is(Blocks.RED_SAND) ? Blocks.RED_SANDSTONE.defaultBlockState() : Blocks.SANDSTONE.defaultBlockState();
                    }
                }
            }
        }

    }
}
