package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class RandomBooleanFeatureConfiguration implements FeatureConfiguration {
    public final ConfiguredFeature<?, ?> featureTrue;
    public final ConfiguredFeature<?, ?> featureFalse;

    public RandomBooleanFeatureConfiguration(ConfiguredFeature<?, ?> param0, ConfiguredFeature<?, ?> param1) {
        this.featureTrue = param0;
        this.featureFalse = param1;
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
        return new Dynamic<>(
            param0,
            param0.createMap(
                ImmutableMap.of(
                    param0.createString("feature_true"),
                    this.featureTrue.serialize(param0).getValue(),
                    param0.createString("feature_false"),
                    this.featureFalse.serialize(param0).getValue()
                )
            )
        );
    }

    public static <T> RandomBooleanFeatureConfiguration deserialize(Dynamic<T> param0) {
        ConfiguredFeature<?, ?> var0 = ConfiguredFeature.deserialize(param0.get("feature_true").orElseEmptyMap());
        ConfiguredFeature<?, ?> var1 = ConfiguredFeature.deserialize(param0.get("feature_false").orElseEmptyMap());
        return new RandomBooleanFeatureConfiguration(var0, var1);
    }
}
