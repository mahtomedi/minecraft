package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class NoiseOctaves {
    public static final Codec<NoiseOctaves> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    NormalNoise.NoiseParameters.CODEC.fieldOf("temperature").forGetter(NoiseOctaves::temperature),
                    NormalNoise.NoiseParameters.CODEC.fieldOf("humidity").forGetter(NoiseOctaves::humidity),
                    NormalNoise.NoiseParameters.CODEC.fieldOf("continentalness").forGetter(NoiseOctaves::continentalness),
                    NormalNoise.NoiseParameters.CODEC.fieldOf("erosion").forGetter(NoiseOctaves::erosion),
                    NormalNoise.NoiseParameters.CODEC.fieldOf("weirdness").forGetter(NoiseOctaves::weirdness),
                    NormalNoise.NoiseParameters.CODEC.fieldOf("shift").forGetter(NoiseOctaves::shift)
                )
                .apply(param0, NoiseOctaves::new)
    );
    private final NormalNoise.NoiseParameters temperature;
    private final NormalNoise.NoiseParameters humidity;
    private final NormalNoise.NoiseParameters continentalness;
    private final NormalNoise.NoiseParameters erosion;
    private final NormalNoise.NoiseParameters weirdness;
    private final NormalNoise.NoiseParameters shift;

    public NoiseOctaves(
        NormalNoise.NoiseParameters param0,
        NormalNoise.NoiseParameters param1,
        NormalNoise.NoiseParameters param2,
        NormalNoise.NoiseParameters param3,
        NormalNoise.NoiseParameters param4,
        NormalNoise.NoiseParameters param5
    ) {
        this.temperature = param0;
        this.humidity = param1;
        this.continentalness = param2;
        this.erosion = param3;
        this.weirdness = param4;
        this.shift = param5;
    }

    public NormalNoise.NoiseParameters temperature() {
        return this.temperature;
    }

    public NormalNoise.NoiseParameters humidity() {
        return this.humidity;
    }

    public NormalNoise.NoiseParameters continentalness() {
        return this.continentalness;
    }

    public NormalNoise.NoiseParameters erosion() {
        return this.erosion;
    }

    public NormalNoise.NoiseParameters weirdness() {
        return this.weirdness;
    }

    public NormalNoise.NoiseParameters shift() {
        return this.shift;
    }
}
