package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class RandomRandomFeatureConfiguration implements FeatureConfiguration {
    public static final Codec<RandomRandomFeatureConfiguration> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    ConfiguredFeature.CODEC.listOf().fieldOf("features").forGetter(param0x -> param0x.features),
                    Codec.INT.fieldOf("count").withDefault(0).forGetter(param0x -> param0x.count)
                )
                .apply(param0, RandomRandomFeatureConfiguration::new)
    );
    public final List<ConfiguredFeature<?, ?>> features;
    public final int count;

    public RandomRandomFeatureConfiguration(List<ConfiguredFeature<?, ?>> param0, int param1) {
        this.features = param0;
        this.count = param1;
    }
}
