package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.WeightedConfiguredFeature;

public class RandomFeatureConfiguration implements FeatureConfiguration {
    public static final Codec<RandomFeatureConfiguration> CODEC = RecordCodecBuilder.create(
        param0 -> param0.apply2(
                RandomFeatureConfiguration::new,
                WeightedConfiguredFeature.CODEC.listOf().fieldOf("features").forGetter(param0x -> param0x.features),
                ConfiguredFeature.CODEC.fieldOf("default").forGetter(param0x -> param0x.defaultFeature)
            )
    );
    public final List<WeightedConfiguredFeature<?>> features;
    public final ConfiguredFeature<?, ?> defaultFeature;

    public RandomFeatureConfiguration(List<WeightedConfiguredFeature<?>> param0, ConfiguredFeature<?, ?> param1) {
        this.features = param0;
        this.defaultFeature = param1;
    }
}
