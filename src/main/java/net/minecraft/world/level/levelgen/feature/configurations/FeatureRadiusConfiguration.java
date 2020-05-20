package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;

public class FeatureRadiusConfiguration implements FeatureConfiguration {
    public static final Codec<FeatureRadiusConfiguration> CODEC = Codec.INT
        .fieldOf("radius")
        .xmap(FeatureRadiusConfiguration::new, param0 -> param0.radius)
        .codec();
    public final int radius;

    public FeatureRadiusConfiguration(int param0) {
        this.radius = param0;
    }
}
