package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class RandomRandomFeatureConfiguration implements FeatureConfiguration {
    public final List<ConfiguredFeature<?, ?>> features;
    public final int count;

    public RandomRandomFeatureConfiguration(List<ConfiguredFeature<?, ?>> param0, int param1) {
        this.features = param0;
        this.count = param1;
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

    public static <T> RandomRandomFeatureConfiguration deserialize(Dynamic<T> param0) {
        List<ConfiguredFeature<?, ?>> var0 = param0.get("features").asList(ConfiguredFeature::deserialize);
        int var1 = param0.get("count").asInt(0);
        return new RandomRandomFeatureConfiguration(var0, var1);
    }

    public static RandomRandomFeatureConfiguration random(Random param0) {
        return new RandomRandomFeatureConfiguration(
            Util.randomObjectStream(param0, 1, 10, Registry.FEATURE).map(param1 -> param1.random(param0)).collect(Collectors.toList()), param0.nextInt(5) + 3
        );
    }
}
