package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;

public class CountFeatureConfiguration implements FeatureConfiguration {
    public static final Codec<CountFeatureConfiguration> CODEC = Codec.INT
        .fieldOf("count")
        .xmap(CountFeatureConfiguration::new, param0 -> param0.count)
        .codec();
    public final int count;

    public CountFeatureConfiguration(int param0) {
        this.count = param0;
    }
}
