package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SimpleRandomFeatureConfig implements FeatureConfiguration {
    public final List<ConfiguredFeature<?>> features;

    public SimpleRandomFeatureConfig(List<ConfiguredFeature<?>> param0) {
        this.features = param0;
    }

    public SimpleRandomFeatureConfig(Feature<?>[] param0, FeatureConfiguration[] param1) {
        this(IntStream.range(0, param0.length).mapToObj(param2 -> getConfiguredFeature(param0[param2], param1[param2])).collect(Collectors.toList()));
    }

    private static <FC extends FeatureConfiguration> ConfiguredFeature<FC> getConfiguredFeature(Feature<FC> param0, FeatureConfiguration param1) {
        return new ConfiguredFeature<>(param0, (FC)param1);
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

    public static <T> SimpleRandomFeatureConfig deserialize(Dynamic<T> param0) {
        List<ConfiguredFeature<?>> var0 = param0.get("features").asList(ConfiguredFeature::deserialize);
        return new SimpleRandomFeatureConfig(var0);
    }
}
