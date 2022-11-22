package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class RandomBooleanFeatureConfiguration implements FeatureConfiguration {
    public static final Codec<RandomBooleanFeatureConfiguration> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    PlacedFeature.CODEC.fieldOf("feature_true").forGetter(param0x -> param0x.featureTrue),
                    PlacedFeature.CODEC.fieldOf("feature_false").forGetter(param0x -> param0x.featureFalse)
                )
                .apply(param0, RandomBooleanFeatureConfiguration::new)
    );
    public final Holder<PlacedFeature> featureTrue;
    public final Holder<PlacedFeature> featureFalse;

    public RandomBooleanFeatureConfiguration(Holder<PlacedFeature> param0, Holder<PlacedFeature> param1) {
        this.featureTrue = param0;
        this.featureFalse = param1;
    }

    @Override
    public Stream<ConfiguredFeature<?, ?>> getFeatures() {
        return Stream.concat(((PlacedFeature)this.featureTrue.value()).getFeatures(), ((PlacedFeature)this.featureFalse.value()).getFeatures());
    }
}
