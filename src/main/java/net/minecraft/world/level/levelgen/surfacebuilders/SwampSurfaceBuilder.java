package net.minecraft.world.level.levelgen.surfacebuilders;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

public class SwampSurfaceBuilder extends SurfaceBuilder<SurfaceBuilderBaseConfiguration> {
    public SwampSurfaceBuilder(Codec<SurfaceBuilderBaseConfiguration> param0) {
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
        int param10,
        long param11,
        SurfaceBuilderBaseConfiguration param12
    ) {
        double var0 = Biome.BIOME_INFO_NOISE.getValue((double)param3 * 0.25, (double)param4 * 0.25, false);
        if (var0 > 0.0) {
            int var1 = param3 & 15;
            int var2 = param4 & 15;
            BlockPos.MutableBlockPos var3 = new BlockPos.MutableBlockPos();

            for(int var4 = param5; var4 >= param10; --var4) {
                var3.set(var1, var4, var2);
                if (!param1.getBlockState(var3).isAir()) {
                    if (var4 == 62 && !param1.getBlockState(var3).is(param8.getBlock())) {
                        param1.setBlockState(var3, param8, false);
                    }
                    break;
                }
            }
        }

        SurfaceBuilder.DEFAULT.apply(param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10, param11, param12);
    }
}
