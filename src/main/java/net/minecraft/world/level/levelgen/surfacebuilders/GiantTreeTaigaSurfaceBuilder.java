package net.minecraft.world.level.levelgen.surfacebuilders;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

public class GiantTreeTaigaSurfaceBuilder extends SurfaceBuilder<SurfaceBuilderBaseConfiguration> {
    public GiantTreeTaigaSurfaceBuilder(
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
        if (param6 > 1.75) {
            SurfaceBuilder.DEFAULT
                .apply(param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10, SurfaceBuilder.CONFIG_COARSE_DIRT);
        } else if (param6 > -0.95) {
            SurfaceBuilder.DEFAULT.apply(param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10, SurfaceBuilder.CONFIG_PODZOL);
        } else {
            SurfaceBuilder.DEFAULT.apply(param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10, SurfaceBuilder.CONFIG_GRASS);
        }

    }
}
