package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;

public class ProbabilityFeatureConfiguration implements CarverConfiguration, FeatureConfiguration {
    public final float probability;

    public ProbabilityFeatureConfiguration(float param0) {
        this.probability = param0;
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
        return new Dynamic<>(param0, param0.createMap(ImmutableMap.of(param0.createString("probability"), param0.createFloat(this.probability))));
    }

    public static <T> ProbabilityFeatureConfiguration deserialize(Dynamic<T> param0) {
        float var0 = param0.get("probability").asFloat(0.0F);
        return new ProbabilityFeatureConfiguration(var0);
    }

    public static ProbabilityFeatureConfiguration random(Random param0) {
        return new ProbabilityFeatureConfiguration(param0.nextFloat() / 2.0F);
    }
}
