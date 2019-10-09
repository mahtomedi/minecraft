package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;

public class SeagrassFeatureConfiguration implements FeatureConfiguration {
    public final int count;
    public final double tallSeagrassProbability;

    public SeagrassFeatureConfiguration(int param0, double param1) {
        this.count = param0;
        this.tallSeagrassProbability = param1;
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
        return new Dynamic<>(
            param0,
            param0.createMap(
                ImmutableMap.of(
                    param0.createString("count"),
                    param0.createInt(this.count),
                    param0.createString("tall_seagrass_probability"),
                    param0.createDouble(this.tallSeagrassProbability)
                )
            )
        );
    }

    public static <T> SeagrassFeatureConfiguration deserialize(Dynamic<T> param0) {
        int var0 = param0.get("count").asInt(0);
        double var1 = param0.get("tall_seagrass_probability").asDouble(0.0);
        return new SeagrassFeatureConfiguration(var0, var1);
    }
}
