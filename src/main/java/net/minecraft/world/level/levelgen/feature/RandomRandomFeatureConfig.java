package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RandomRandomFeatureConfig implements FeatureConfiguration {
    public final List<ConfiguredFeature<?>> features;
    public final int count;

    public RandomRandomFeatureConfig(List<ConfiguredFeature<?>> param0, int param1) {
        this.features = param0;
        this.count = param1;
    }

    public RandomRandomFeatureConfig(Feature<?>[] param0, FeatureConfiguration[] param1, int param2) {
        this(IntStream.range(0, param0.length).mapToObj(param2x -> getConfiguredFeature(param0[param2x], param1[param2x])).collect(Collectors.toList()), param2);
    }

    private static <FC extends FeatureConfiguration> ConfiguredFeature<?> getConfiguredFeature(Feature<FC> param0, FeatureConfiguration param1) {
        return new ConfiguredFeature<>(param0, (FC)param1);
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
        return new Dynamic<>(
            param0,
            param0.createMap(
                ImmutableMap.of(
                    param0.createString("features"),
                    param0.createList(this.features.stream().map(param1 -> param1.serialize(param0).getValue())),
                    param0.createString("count"),
                    param0.createInt(this.count)
                )
            )
        );
    }

    public static <T> RandomRandomFeatureConfig deserialize(Dynamic<T> param0) {
        List<ConfiguredFeature<?>> var0 = param0.get("features").asList(ConfiguredFeature::deserialize);
        int var1 = param0.get("count").asInt(0);
        return new RandomRandomFeatureConfig(var0, var1);
    }
}
