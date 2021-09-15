package net.minecraft.world.level.levelgen.surfacebuilders;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BlockColumn;

public class ConfiguredSurfaceBuilder<SC extends SurfaceBuilderConfiguration> {
    public static final Codec<ConfiguredSurfaceBuilder<?>> DIRECT_CODEC = Registry.SURFACE_BUILDER
        .dispatch(param0 -> param0.surfaceBuilder, SurfaceBuilder::configuredCodec);
    public static final Codec<Supplier<ConfiguredSurfaceBuilder<?>>> CODEC = RegistryFileCodec.create(
        Registry.CONFIGURED_SURFACE_BUILDER_REGISTRY, DIRECT_CODEC
    );
    public final SurfaceBuilder<SC> surfaceBuilder;
    public final SC config;

    public ConfiguredSurfaceBuilder(SurfaceBuilder<SC> param0, SC param1) {
        this.surfaceBuilder = param0;
        this.config = param1;
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
        long param11
    ) {
        this.surfaceBuilder.apply(param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10, param11, this.config);
    }

    public void initNoise(long param0) {
        this.surfaceBuilder.initNoise(param0);
    }

    public SC config() {
        return this.config;
    }
}
