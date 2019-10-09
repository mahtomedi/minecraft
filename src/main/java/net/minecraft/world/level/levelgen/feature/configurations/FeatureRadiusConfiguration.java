package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;

public class FeatureRadiusConfiguration implements FeatureConfiguration {
    public final int radius;

    public FeatureRadiusConfiguration(int param0) {
        this.radius = param0;
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
        return new Dynamic<>(param0, param0.createMap(ImmutableMap.of(param0.createString("radius"), param0.createInt(this.radius))));
    }

    public static <T> FeatureRadiusConfiguration deserialize(Dynamic<T> param0) {
        int var0 = param0.get("radius").asInt(0);
        return new FeatureRadiusConfiguration(var0);
    }
}
