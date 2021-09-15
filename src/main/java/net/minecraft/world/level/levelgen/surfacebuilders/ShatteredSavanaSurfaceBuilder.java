package net.minecraft.world.level.levelgen.surfacebuilders;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BlockColumn;

public class ShatteredSavanaSurfaceBuilder extends SurfaceBuilder<SurfaceBuilderBaseConfiguration> {
    public ShatteredSavanaSurfaceBuilder(Codec<SurfaceBuilderBaseConfiguration> param0) {
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
        if (param6 > 1.75) {
            SurfaceBuilder.DEFAULT
                .apply(param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10, param11, SurfaceBuilder.CONFIG_STONE);
        } else if (param6 > -0.5) {
            SurfaceBuilder.DEFAULT
                .apply(param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10, param11, SurfaceBuilder.CONFIG_COARSE_DIRT);
        } else {
            SurfaceBuilder.DEFAULT
                .apply(param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10, param11, SurfaceBuilder.CONFIG_GRASS);
        }

    }
}
