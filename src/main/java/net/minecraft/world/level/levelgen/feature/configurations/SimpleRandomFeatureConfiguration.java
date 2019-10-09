package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.List;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class SimpleRandomFeatureConfiguration implements FeatureConfiguration {
    public final List<ConfiguredFeature<?, ?>> features;

    public SimpleRandomFeatureConfiguration(List<ConfiguredFeature<?, ?>> param0) {
        this.features = param0;
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
        return new Dynamic<>(
            param0,
            param0.createMap(
                ImmutableMap.of(param0.createString("features"), param0.createList(this.features.stream().map(param1 -> param1.serialize(param0).getValue())))
            )
        );
    }

    public static <T> SimpleRandomFeatureConfiguration deserialize(Dynamic<T> param0) {
        List<ConfiguredFeature<?, ?>> var0 = param0.get("features").asList(ConfiguredFeature::deserialize);
        return new SimpleRandomFeatureConfiguration(var0);
    }
}
