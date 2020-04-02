package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.List;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.WeightedConfiguredFeature;

public class RandomFeatureConfiguration implements FeatureConfiguration {
    public final List<WeightedConfiguredFeature<?>> features;
    public final ConfiguredFeature<?, ?> defaultFeature;

    public RandomFeatureConfiguration(List<WeightedConfiguredFeature<?>> param0, ConfiguredFeature<?, ?> param1) {
        this.features = param0;
        this.defaultFeature = param1;
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
        T var0 = param0.createList(this.features.stream().map(param1 -> param1.serialize(param0).getValue()));
        T var1 = this.defaultFeature.serialize(param0).getValue();
        return new Dynamic<>(param0, param0.createMap(ImmutableMap.of(param0.createString("features"), var0, param0.createString("default"), var1)));
    }

    public static <T> RandomFeatureConfiguration deserialize(Dynamic<T> param0) {
        List<WeightedConfiguredFeature<?>> var0 = param0.get("features").asList(WeightedConfiguredFeature::deserialize);
        ConfiguredFeature<?, ?> var1 = ConfiguredFeature.deserialize(param0.get("default").orElseEmptyMap());
        return new RandomFeatureConfiguration(var0, var1);
    }
}
