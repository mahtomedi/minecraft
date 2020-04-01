package net.minecraft.world.level.levelgen.surfacebuilders;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

public class ConfiguredSurfaceBuilder<SC extends SurfaceBuilderConfiguration> {
    public final SurfaceBuilder<SC> surfaceBuilder;
    public final SC config;

    public ConfiguredSurfaceBuilder(SurfaceBuilder<SC> param0, SC param1) {
        this.surfaceBuilder = param0;
        this.config = param1;
    }

    public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
        return new Dynamic<>(
            param0,
            param0.createMap(
                ImmutableMap.of(
                    param0.createString("name"),
                    param0.createString(Registry.SURFACE_BUILDER.getKey(this.surfaceBuilder).toString()),
                    param0.createString("config"),
                    this.config.serialize(param0).getValue()
                )
            )
        );
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
        long param10
    ) {
        this.surfaceBuilder.apply(param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10, this.config);
    }

    public void initNoise(long param0) {
        this.surfaceBuilder.initNoise(param0);
    }

    public SC getSurfaceBuilderConfiguration() {
        return this.config;
    }
}
