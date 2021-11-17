package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class RandomBooleanFeatureConfiguration implements FeatureConfiguration {
    public static final Codec<RandomBooleanFeatureConfiguration> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    ConfiguredFeature.CODEC.fieldOf("feature_true").forGetter(param0x -> param0x.featureTrue),
                    ConfiguredFeature.CODEC.fieldOf("feature_false").forGetter(param0x -> param0x.featureFalse)
                )
                .apply(param0, RandomBooleanFeatureConfiguration::new)
    );
    public final Supplier<ConfiguredFeature<?, ?>> featureTrue;
    public final Supplier<ConfiguredFeature<?, ?>> featureFalse;

    public RandomBooleanFeatureConfiguration(Supplier<ConfiguredFeature<?, ?>> param0, Supplier<ConfiguredFeature<?, ?>> param1) {
        this.featureTrue = param0;
        this.featureFalse = param1;
    }

    @Override
    public Stream<ConfiguredFeature<?, ?>> getFeatures() {
        return Stream.concat(this.featureTrue.get().getFeatures(), this.featureFalse.get().getFeatures());
    }
}
