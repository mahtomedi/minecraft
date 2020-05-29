package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Codecs;

public class NoiseSamplingSettings {
    private static final Codec<Double> SCALE_RANGE = Codecs.doubleRange(0.001, 1000.0);
    public static final Codec<NoiseSamplingSettings> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    SCALE_RANGE.fieldOf("xz_scale").forGetter(NoiseSamplingSettings::xzScale),
                    SCALE_RANGE.fieldOf("y_scale").forGetter(NoiseSamplingSettings::yScale),
                    SCALE_RANGE.fieldOf("xz_factor").forGetter(NoiseSamplingSettings::xzFactor),
                    SCALE_RANGE.fieldOf("y_factor").forGetter(NoiseSamplingSettings::yFactor)
                )
                .apply(param0, NoiseSamplingSettings::new)
    );
    private final double xzScale;
    private final double yScale;
    private final double xzFactor;
    private final double yFactor;

    public NoiseSamplingSettings(double param0, double param1, double param2, double param3) {
        this.xzScale = param0;
        this.yScale = param1;
        this.xzFactor = param2;
        this.yFactor = param3;
    }

    public double xzScale() {
        return this.xzScale;
    }

    public double yScale() {
        return this.yScale;
    }

    public double xzFactor() {
        return this.xzFactor;
    }

    public double yFactor() {
        return this.yFactor;
    }
}
