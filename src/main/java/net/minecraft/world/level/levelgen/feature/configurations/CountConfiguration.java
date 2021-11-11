package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;

public class CountConfiguration implements FeatureConfiguration {
    public static final Codec<CountConfiguration> CODEC = IntProvider.codec(0, 256)
        .fieldOf("count")
        .xmap(CountConfiguration::new, CountConfiguration::count)
        .codec();
    private final IntProvider count;

    public CountConfiguration(int param0) {
        this.count = ConstantInt.of(param0);
    }

    public CountConfiguration(IntProvider param0) {
        this.count = param0;
    }

    public IntProvider count() {
        return this.count;
    }
}
