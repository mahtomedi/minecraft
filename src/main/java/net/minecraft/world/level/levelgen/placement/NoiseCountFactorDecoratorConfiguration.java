package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public class NoiseCountFactorDecoratorConfiguration implements DecoratorConfiguration {
    public static final Codec<NoiseCountFactorDecoratorConfiguration> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    Codec.INT.fieldOf("noise_to_count_ratio").forGetter(param0x -> param0x.noiseToCountRatio),
                    Codec.DOUBLE.fieldOf("noise_factor").forGetter(param0x -> param0x.noiseFactor),
                    Codec.DOUBLE.fieldOf("noise_offset").orElse(0.0).forGetter(param0x -> param0x.noiseOffset)
                )
                .apply(param0, NoiseCountFactorDecoratorConfiguration::new)
    );
    public final int noiseToCountRatio;
    public final double noiseFactor;
    public final double noiseOffset;

    public NoiseCountFactorDecoratorConfiguration(int param0, double param1, double param2) {
        this.noiseToCountRatio = param0;
        this.noiseFactor = param1;
        this.noiseOffset = param2;
    }
}
