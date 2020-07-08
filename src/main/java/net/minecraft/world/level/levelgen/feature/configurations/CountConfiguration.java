package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import net.minecraft.util.UniformInt;

public class CountConfiguration implements DecoratorConfiguration, FeatureConfiguration {
    public static final Codec<CountConfiguration> CODEC = UniformInt.codec(-10, 128, 128)
        .fieldOf("count")
        .xmap(CountConfiguration::new, CountConfiguration::count)
        .codec();
    private final UniformInt count;

    public CountConfiguration(int param0) {
        this.count = UniformInt.fixed(param0);
    }

    public CountConfiguration(UniformInt param0) {
        this.count = param0;
    }

    public UniformInt count() {
        return this.count;
    }
}
