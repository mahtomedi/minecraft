package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;

public class PillagerOutpostConfiguration implements FeatureConfiguration {
    public final double probability;

    public PillagerOutpostConfiguration(double param0) {
        this.probability = param0;
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
        return new Dynamic<>(param0, param0.createMap(ImmutableMap.of(param0.createString("probability"), param0.createDouble(this.probability))));
    }

    public static <T> PillagerOutpostConfiguration deserialize(Dynamic<T> param0) {
        float var0 = param0.get("probability").asFloat(0.0F);
        return new PillagerOutpostConfiguration((double)var0);
    }
}
