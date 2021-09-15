package net.minecraft.world.level.levelgen.surfacebuilders;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BlockColumn;

public class SwampSurfaceBuilder extends SurfaceBuilder<SurfaceBuilderBaseConfiguration> {
    public SwampSurfaceBuilder(Codec<SurfaceBuilderBaseConfiguration> param0) {
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
        double var0 = Biome.BIOME_INFO_NOISE.getValue((double)param3 * 0.25, (double)param4 * 0.25, false);
        if (var0 > 0.0) {
            for(int var1 = param5; var1 >= param10; --var1) {
                if (!param1.getBlock(var1).isAir()) {
                    if (var1 == 62 && !param1.getBlock(var1).is(param8.getBlock())) {
                        param1.setBlock(var1, param8);
                    }
                    break;
                }
            }
        }

        SurfaceBuilder.DEFAULT.apply(param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10, param11, param12);
    }
}
