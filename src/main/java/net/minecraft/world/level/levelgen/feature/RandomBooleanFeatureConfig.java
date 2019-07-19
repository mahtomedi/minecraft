package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;

public class RandomBooleanFeatureConfig implements FeatureConfiguration {
    public final ConfiguredFeature<?> featureTrue;
    public final ConfiguredFeature<?> featureFalse;

    public RandomBooleanFeatureConfig(ConfiguredFeature<?> param0, ConfiguredFeature<?> param1) {
        this.featureTrue = param0;
        this.featureFalse = param1;
    }

    public RandomBooleanFeatureConfig(Feature<?> param0, FeatureConfiguration param1, Feature<?> param2, FeatureConfiguration param3) {
        this(getFeature(param0, param1), getFeature(param2, param3));
    }

    private static <FC extends FeatureConfiguration> ConfiguredFeature<FC> getFeature(Feature<FC> param0, FeatureConfiguration param1) {
        return new ConfiguredFeature<>(param0, (FC)param1);
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

    public static <T> RandomBooleanFeatureConfig deserialize(Dynamic<T> param0) {
        ConfiguredFeature<?> var0 = ConfiguredFeature.deserialize(param0.get("feature_true").orElseEmptyMap());
        ConfiguredFeature<?> var1 = ConfiguredFeature.deserialize(param0.get("feature_false").orElseEmptyMap());
        return new RandomBooleanFeatureConfig(var0, var1);
    }
}
