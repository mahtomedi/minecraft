package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class ConfiguredDecorator<DC extends DecoratorConfiguration> {
    public static final Codec<ConfiguredDecorator<?>> CODEC = Registry.DECORATOR
        .dispatch("name", param0 -> param0.decorator, FeatureDecorator::configuredCodec);
    public final FeatureDecorator<DC> decorator;
    public final DC config;

    public ConfiguredDecorator(FeatureDecorator<DC> param0, DC param1) {
        this.decorator = param0;
        this.config = param1;
    }

    public <FC extends FeatureConfiguration, F extends Feature<FC>> boolean place(
        WorldGenLevel param0, ChunkGenerator param1, Random param2, BlockPos param3, ConfiguredFeature<FC, F> param4
    ) {
        return this.decorator.placeFeature(param0, param1, param2, param3, this.config, param4);
    }
}
