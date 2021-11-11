package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.WeightedPlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class RandomFeatureConfiguration implements FeatureConfiguration {
    public static final Codec<RandomFeatureConfiguration> CODEC = RecordCodecBuilder.create(
        param0 -> param0.apply2(
                RandomFeatureConfiguration::new,
                WeightedPlacedFeature.CODEC.listOf().fieldOf("features").forGetter(param0x -> param0x.features),
                PlacedFeature.CODEC.fieldOf("default").forGetter(param0x -> param0x.defaultFeature)
            )
    );
    public final List<WeightedPlacedFeature> features;
    public final Supplier<PlacedFeature> defaultFeature;

    public RandomFeatureConfiguration(List<WeightedPlacedFeature> param0, PlacedFeature param1) {
        this(param0, () -> param1);
    }

    private RandomFeatureConfiguration(List<WeightedPlacedFeature> param0, Supplier<PlacedFeature> param1) {
        this.features = param0;
        this.defaultFeature = param1;
    }

    @Override
    public Stream<ConfiguredFeature<?, ?>> getFeatures() {
        return Stream.concat(this.features.stream().flatMap(param0 -> param0.feature.get().getFeatures()), this.defaultFeature.get().getFeatures());
    }
}
