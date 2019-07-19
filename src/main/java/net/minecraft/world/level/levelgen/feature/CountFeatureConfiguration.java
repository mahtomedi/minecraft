package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;

public class CountFeatureConfiguration implements FeatureConfiguration {
    public final int count;

    public CountFeatureConfiguration(int param0) {
        this.count = param0;
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
        return new Dynamic<>(param0, param0.createMap(ImmutableMap.of(param0.createString("count"), param0.createInt(this.count))));
    }

    public static <T> CountFeatureConfiguration deserialize(Dynamic<T> param0) {
        int var0 = param0.get("count").asInt(0);
        return new CountFeatureConfiguration(var0);
    }
}
