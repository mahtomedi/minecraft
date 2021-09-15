package net.minecraft.world.level.levelgen.feature;

import java.util.Optional;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class FeaturePlaceContext<FC extends FeatureConfiguration> {
    private final Optional<ConfiguredFeature<?, ?>> topFeature;
    private final WorldGenLevel level;
    private final ChunkGenerator chunkGenerator;
    private final Random random;
    private final BlockPos origin;
    private final FC config;

    public FeaturePlaceContext(Optional<ConfiguredFeature<?, ?>> param0, WorldGenLevel param1, ChunkGenerator param2, Random param3, BlockPos param4, FC param5) {
        this.topFeature = param0;
        this.level = param1;
        this.chunkGenerator = param2;
        this.random = param3;
        this.origin = param4;
        this.config = param5;
    }

    public Optional<ConfiguredFeature<?, ?>> topFeature() {
        return this.topFeature;
    }

    public WorldGenLevel level() {
        return this.level;
    }

    public ChunkGenerator chunkGenerator() {
        return this.chunkGenerator;
    }

    public Random random() {
        return this.random;
    }

    public BlockPos origin() {
        return this.origin;
    }

    public FC config() {
        return this.config;
    }
}
