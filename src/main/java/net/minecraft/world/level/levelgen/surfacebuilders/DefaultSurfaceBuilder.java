package net.minecraft.world.level.levelgen.surfacebuilders;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BlockColumn;

public class DefaultSurfaceBuilder extends SurfaceBuilder<SurfaceBuilderBaseConfiguration> {
    public DefaultSurfaceBuilder(Codec<SurfaceBuilderBaseConfiguration> param0) {
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
            param12.getTopMaterial(),
            param12.getUnderMaterial(),
            param12.getUnderwaterMaterial(),
            param9,
            param10
        );
    }

    protected void apply(
        Random param0,
        BlockColumn param1,
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
        int param12,
        int param13
    ) {
        param12 = Integer.MIN_VALUE;
        int var0 = (int)(param6 / 3.0 + 3.0 + param0.nextDouble() * 0.25);
        BlockState var1 = param10;
        int var2 = -1;

        for(int var3 = param5; var3 >= param13; --var3) {
            BlockState var4 = param1.getBlock(var3);
            if (var4.isAir()) {
                var2 = -1;
                param12 = Integer.MIN_VALUE;
            } else if (!var4.is(param7.getBlock())) {
                param12 = Math.max(var3 + 1, param12);
            } else if (var2 == -1) {
                var2 = var0;
                BlockState var5;
                if (var3 >= param12 + 2) {
                    var5 = param9;
                } else if (var3 >= param12 - 1) {
                    var1 = param10;
                    var5 = param9;
                } else if (var3 >= param12 - 4) {
                    var1 = param10;
                    var5 = param10;
                } else if (var3 >= param12 - (7 + var0)) {
                    var5 = var1;
                } else {
                    var1 = param7;
                    var5 = param11;
                }

                param1.setBlock(var3, maybeReplaceState(var5, param1, var3, param12));
            } else if (var2 > 0) {
                --var2;
                param1.setBlock(var3, maybeReplaceState(var1, param1, var3, param12));
                if (var2 == 0 && var1.is(Blocks.SAND) && var0 > 1) {
                    var2 = param0.nextInt(4) + Math.max(0, var3 - param12);
                    var1 = var1.is(Blocks.RED_SAND) ? Blocks.RED_SANDSTONE.defaultBlockState() : Blocks.SANDSTONE.defaultBlockState();
                }
            }
        }

    }

    private static BlockState maybeReplaceState(BlockState param0, BlockColumn param1, int param2, int param3) {
        if (param2 <= param3 && param0.is(Blocks.GRASS_BLOCK)) {
            return Blocks.DIRT.defaultBlockState();
        } else if (param0.is(Blocks.SAND) && isEmptyBelow(param1, param2)) {
            return Blocks.SANDSTONE.defaultBlockState();
        } else if (param0.is(Blocks.RED_SAND) && isEmptyBelow(param1, param2)) {
            return Blocks.RED_SANDSTONE.defaultBlockState();
        } else {
            return param0.is(Blocks.GRAVEL) && isEmptyBelow(param1, param2) ? Blocks.STONE.defaultBlockState() : param0;
        }
    }

    private static boolean isEmptyBelow(BlockColumn param0, int param1) {
        BlockState var0 = param0.getBlock(param1 - 1);
        return var0.isAir() || !var0.getFluidState().isEmpty();
    }
}
