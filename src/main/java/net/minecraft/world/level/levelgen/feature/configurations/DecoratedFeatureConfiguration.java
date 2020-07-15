package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.ConfiguredDecorator;

public class DecoratedFeatureConfiguration implements FeatureConfiguration {
    public static final Codec<DecoratedFeatureConfiguration> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    ConfiguredFeature.CODEC.fieldOf("feature").forGetter(param0x -> param0x.feature),
                    ConfiguredDecorator.CODEC.fieldOf("decorator").forGetter(param0x -> param0x.decorator)
                )
                .apply(param0, DecoratedFeatureConfiguration::new)
    );
    public final Supplier<ConfiguredFeature<?, ?>> feature;
    public final ConfiguredDecorator<?> decorator;

    public DecoratedFeatureConfiguration(Supplier<ConfiguredFeature<?, ?>> param0, ConfiguredDecorator<?> param1) {
        this.feature = param0;
        this.decorator = param1;
    }

    @Override
    public String toString() {
        return String.format("< %s [%s | %s] >", this.getClass().getSimpleName(), Registry.FEATURE.getKey(this.feature.get().feature()), this.decorator);
    }

    @Override
    public Stream<ConfiguredFeature<?, ?>> getFeatures() {
        return this.feature.get().getFeatures();
    }
}
