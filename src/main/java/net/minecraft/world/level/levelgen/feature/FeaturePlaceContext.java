package net.minecraft.world.level.levelgen.feature;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class FeaturePlaceContext<FC extends FeatureConfiguration> {
    private final WorldGenLevel level;
    private final ChunkGenerator chunkGenerator;
    private final Random random;
    private final BlockPos origin;
    private final FC config;

    public FeaturePlaceContext(WorldGenLevel param0, ChunkGenerator param1, Random param2, BlockPos param3, FC param4) {
        this.level = param0;
        this.chunkGenerator = param1;
        this.random = param2;
        this.origin = param3;
        this.config = param4;
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
