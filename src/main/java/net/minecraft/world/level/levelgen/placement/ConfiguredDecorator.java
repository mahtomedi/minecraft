package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.Decoratable;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public class ConfiguredDecorator<DC extends DecoratorConfiguration> implements Decoratable<ConfiguredDecorator<?>> {
    public static final Codec<ConfiguredDecorator<?>> CODEC = Registry.DECORATOR
        .dispatch("name", param0 -> param0.decorator, FeatureDecorator::configuredCodec);
    private final FeatureDecorator<DC> decorator;
    private final DC config;

    public ConfiguredDecorator(FeatureDecorator<DC> param0, DC param1) {
        this.decorator = param0;
        this.config = param1;
    }

    public Stream<BlockPos> getPositions(DecorationContext param0, Random param1, BlockPos param2) {
        return this.decorator.getPositions(param0, param1, this.config, param2);
    }

    @Override
    public String toString() {
        return String.format("[%s %s]", Registry.DECORATOR.getKey(this.decorator), this.config);
    }

    public ConfiguredDecorator<?> decorated(ConfiguredDecorator<?> param0) {
        return new ConfiguredDecorator<>(FeatureDecorator.DECORATED, new DecoratedDecoratorConfiguration(param0, this));
    }

    public DC config() {
        return this.config;
    }
}
