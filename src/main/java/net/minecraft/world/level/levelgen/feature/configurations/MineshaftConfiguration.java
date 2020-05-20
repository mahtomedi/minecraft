package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.MineshaftFeature;

public class MineshaftConfiguration implements FeatureConfiguration {
    public static final Codec<MineshaftConfiguration> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    Codec.DOUBLE.fieldOf("probability").forGetter(param0x -> param0x.probability),
                    MineshaftFeature.Type.CODEC.fieldOf("type").forGetter(param0x -> param0x.type)
                )
                .apply(param0, MineshaftConfiguration::new)
    );
    public final double probability;
    public final MineshaftFeature.Type type;

    public MineshaftConfiguration(double param0, MineshaftFeature.Type param1) {
        this.probability = param0;
        this.type = param1;
    }
}
