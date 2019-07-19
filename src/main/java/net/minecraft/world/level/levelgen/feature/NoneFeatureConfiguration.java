package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;

public class NoneFeatureConfiguration implements FeatureConfiguration {
    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
        return new Dynamic<>(param0, param0.emptyMap());
    }

    public static <T> NoneFeatureConfiguration deserialize(Dynamic<T> param0) {
        return FeatureConfiguration.NONE;
    }
}
