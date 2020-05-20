package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class NoiseDependantDecoratorConfiguration implements DecoratorConfiguration {
    public static final Codec<NoiseDependantDecoratorConfiguration> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    Codec.DOUBLE.fieldOf("noise_level").forGetter(param0x -> param0x.noiseLevel),
                    Codec.INT.fieldOf("below_noise").forGetter(param0x -> param0x.belowNoise),
                    Codec.INT.fieldOf("above_noise").forGetter(param0x -> param0x.aboveNoise)
                )
                .apply(param0, NoiseDependantDecoratorConfiguration::new)
    );
    public final double noiseLevel;
    public final int belowNoise;
    public final int aboveNoise;

    public NoiseDependantDecoratorConfiguration(double param0, int param1, int param2) {
        this.noiseLevel = param0;
        this.belowNoise = param1;
        this.aboveNoise = param2;
    }
}
