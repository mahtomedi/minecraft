package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;

public class RangeDecoratorConfiguration implements DecoratorConfiguration, FeatureConfiguration {
    public static final Codec<RangeDecoratorConfiguration> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(HeightProvider.CODEC.fieldOf("height").forGetter(param0x -> param0x.height)).apply(param0, RangeDecoratorConfiguration::new)
    );
    public final HeightProvider height;

    public RangeDecoratorConfiguration(HeightProvider param0) {
        this.height = param0;
    }
}
