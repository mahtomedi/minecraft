package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.ConfiguredDecorator;

public class DecoratedFeatureConfiguration implements FeatureConfiguration {
    public final ConfiguredFeature<?, ?> feature;
    public final ConfiguredDecorator<?> decorator;

    public DecoratedFeatureConfiguration(ConfiguredFeature<?, ?> param0, ConfiguredDecorator<?> param1) {
        this.feature = param0;
        this.decorator = param1;
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
        return new Dynamic<>(
            param0,
            param0.createMap(
                ImmutableMap.of(
                    param0.createString("feature"),
                    this.feature.serialize(param0).getValue(),
                    param0.createString("decorator"),
                    this.decorator.serialize(param0).getValue()
                )
            )
        );
    }

    @Override
    public String toString() {
        return String.format(
            "< %s [%s | %s] >",
            this.getClass().getSimpleName(),
            Registry.FEATURE.getKey(this.feature.feature),
            Registry.DECORATOR.getKey(this.decorator.decorator)
        );
    }

    public static <T> DecoratedFeatureConfiguration deserialize(Dynamic<T> param0) {
        ConfiguredFeature<?, ?> var0 = ConfiguredFeature.deserialize(param0.get("feature").orElseEmptyMap());
        ConfiguredDecorator<?> var1 = ConfiguredDecorator.deserialize(param0.get("decorator").orElseEmptyMap());
        return new DecoratedFeatureConfiguration(var0, var1);
    }

    public static DecoratedFeatureConfiguration random(Random param0) {
        return new DecoratedFeatureConfiguration(Registry.FEATURE.getRandom(param0).random(param0), Registry.DECORATOR.getRandom(param0).random(param0));
    }
}
