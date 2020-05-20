package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class SeagrassFeatureConfiguration implements FeatureConfiguration {
    public static final Codec<SeagrassFeatureConfiguration> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    Codec.INT.fieldOf("count").forGetter(param0x -> param0x.count),
                    Codec.DOUBLE.fieldOf("probability").forGetter(param0x -> param0x.tallSeagrassProbability)
                )
                .apply(param0, SeagrassFeatureConfiguration::new)
    );
    public final int count;
    public final double tallSeagrassProbability;

    public SeagrassFeatureConfiguration(int param0, double param1) {
        this.count = param0;
        this.tallSeagrassProbability = param1;
    }
}
