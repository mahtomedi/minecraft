package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.placement.ConfiguredDecorator;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;

public class DecoratedFeatureConfiguration implements FeatureConfiguration {
    public final ConfiguredFeature<?> feature;
    public final ConfiguredDecorator<?> decorator;

    public DecoratedFeatureConfiguration(ConfiguredFeature<?> param0, ConfiguredDecorator<?> param1) {
        this.feature = param0;
        this.decorator = param1;
    }

    public <F extends FeatureConfiguration, D extends DecoratorConfiguration> DecoratedFeatureConfiguration(
        Feature<F> param0, F param1, FeatureDecorator<D> param2, D param3
    ) {
        this(new ConfiguredFeature<>(param0, param1), new ConfiguredDecorator<>(param2, param3));
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
        ConfiguredFeature<?> var0 = ConfiguredFeature.deserialize(param0.get("feature").orElseEmptyMap());
        ConfiguredDecorator<?> var1 = ConfiguredDecorator.deserialize(param0.get("decorator").orElseEmptyMap());
        return new DecoratedFeatureConfiguration(var0, var1);
    }
}
