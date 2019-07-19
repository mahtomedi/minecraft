package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RandomFeatureConfig implements FeatureConfiguration {
    public final List<WeightedConfiguredFeature<?>> features;
    public final ConfiguredFeature<?> defaultFeature;

    public RandomFeatureConfig(List<WeightedConfiguredFeature<?>> param0, ConfiguredFeature<?> param1) {
        this.features = param0;
        this.defaultFeature = param1;
    }

    public RandomFeatureConfig(Feature<?>[] param0, FeatureConfiguration[] param1, float[] param2, Feature<?> param3, FeatureConfiguration param4) {
        this(
            IntStream.range(0, param0.length)
                .mapToObj(param3x -> getWeightedConfiguredFeature(param0[param3x], param1[param3x], param2[param3x]))
                .collect(Collectors.toList()),
            getDefaultFeature(param3, param4)
        );
    }

    private static <FC extends FeatureConfiguration> WeightedConfiguredFeature<FC> getWeightedConfiguredFeature(
        Feature<FC> param0, FeatureConfiguration param1, float param2
    ) {
        return new WeightedConfiguredFeature<>(param0, (FC)param1, Float.valueOf(param2));
    }

    private static <FC extends FeatureConfiguration> ConfiguredFeature<FC> getDefaultFeature(Feature<FC> param0, FeatureConfiguration param1) {
        return new ConfiguredFeature<>(param0, (FC)param1);
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
        T var0 = param0.createList(this.features.stream().map(param1 -> param1.serialize(param0).getValue()));
        T var1 = this.defaultFeature.serialize(param0).getValue();
        return new Dynamic<>(param0, param0.createMap(ImmutableMap.of(param0.createString("features"), var0, param0.createString("default"), var1)));
    }

    public static <T> RandomFeatureConfig deserialize(Dynamic<T> param0) {
        List<WeightedConfiguredFeature<?>> var0 = param0.get("features").asList(WeightedConfiguredFeature::deserialize);
        ConfiguredFeature<?> var1 = ConfiguredFeature.deserialize(param0.get("default").orElseEmptyMap());
        return new RandomFeatureConfig(var0, var1);
    }
}
