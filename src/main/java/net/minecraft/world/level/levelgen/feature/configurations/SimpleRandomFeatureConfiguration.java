package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class SimpleRandomFeatureConfiguration implements FeatureConfiguration {
    public static final Codec<SimpleRandomFeatureConfiguration> CODEC = ExtraCodecs.nonEmptyList(ConfiguredFeature.LIST_CODEC)
        .fieldOf("features")
        .xmap(SimpleRandomFeatureConfiguration::new, param0 -> param0.features)
        .codec();
    public final List<Supplier<ConfiguredFeature<?, ?>>> features;

    public SimpleRandomFeatureConfiguration(List<Supplier<ConfiguredFeature<?, ?>>> param0) {
        this.features = param0;
    }

    @Override
    public Stream<ConfiguredFeature<?, ?>> getFeatures() {
        return this.features.stream().flatMap(param0 -> param0.get().getFeatures());
    }
}
