package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;

public class BuriedTreasureConfiguration implements FeatureConfiguration {
    public final float probability;

    public BuriedTreasureConfiguration(float param0) {
        this.probability = param0;
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
        return new Dynamic<>(param0, param0.createMap(ImmutableMap.of(param0.createString("probability"), param0.createFloat(this.probability))));
    }

    public static <T> BuriedTreasureConfiguration deserialize(Dynamic<T> param0) {
        float var0 = param0.get("probability").asFloat(0.0F);
        return new BuriedTreasureConfiguration(var0);
    }
}
