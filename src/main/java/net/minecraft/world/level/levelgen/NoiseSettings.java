package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Codecs;

public class NoiseSettings {
    public static final Codec<NoiseSettings> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    Codecs.intRange(0, 256).fieldOf("height").forGetter(NoiseSettings::height),
                    NoiseSamplingSettings.CODEC.fieldOf("sampling").forGetter(NoiseSettings::noiseSamplingSettings),
                    NoiseSlideSettings.CODEC.fieldOf("top_slide").forGetter(NoiseSettings::topSlideSettings),
                    NoiseSlideSettings.CODEC.fieldOf("bottom_slide").forGetter(NoiseSettings::bottomSlideSettings),
                    Codecs.intRange(1, 4).fieldOf("size_horizontal").forGetter(NoiseSettings::noiseSizeHorizontal),
                    Codecs.intRange(1, 4).fieldOf("size_vertical").forGetter(NoiseSettings::noiseSizeVertical),
                    Codec.DOUBLE.fieldOf("density_factor").forGetter(NoiseSettings::densityFactor),
                    Codec.DOUBLE.fieldOf("density_offset").forGetter(NoiseSettings::densityOffset),
                    Codec.BOOL.fieldOf("simplex_surface_noise").forGetter(NoiseSettings::useSimplexSurfaceNoise),
                    Codec.BOOL
                        .optionalFieldOf("random_density_offset", Boolean.valueOf(false), Lifecycle.experimental())
                        .forGetter(NoiseSettings::randomDensityOffset),
                    Codec.BOOL
                        .optionalFieldOf("island_noise_override", Boolean.valueOf(false), Lifecycle.experimental())
                        .forGetter(NoiseSettings::islandNoiseOverride),
                    Codec.BOOL.optionalFieldOf("amplified", Boolean.valueOf(false), Lifecycle.experimental()).forGetter(NoiseSettings::isAmplified)
                )
                .apply(param0, NoiseSettings::new)
    );
    private final int height;
    private final NoiseSamplingSettings noiseSamplingSettings;
    private final NoiseSlideSettings topSlideSettings;
    private final NoiseSlideSettings bottomSlideSettings;
    private final int noiseSizeHorizontal;
    private final int noiseSizeVertical;
    private final double densityFactor;
    private final double densityOffset;
    private final boolean useSimplexSurfaceNoise;
    private final boolean randomDensityOffset;
    private final boolean islandNoiseOverride;
    private final boolean isAmplified;

    public NoiseSettings(
        int param0,
        NoiseSamplingSettings param1,
        NoiseSlideSettings param2,
        NoiseSlideSettings param3,
        int param4,
        int param5,
        double param6,
        double param7,
        boolean param8,
        boolean param9,
        boolean param10,
        boolean param11
    ) {
        this.height = param0;
        this.noiseSamplingSettings = param1;
        this.topSlideSettings = param2;
        this.bottomSlideSettings = param3;
        this.noiseSizeHorizontal = param4;
        this.noiseSizeVertical = param5;
        this.densityFactor = param6;
        this.densityOffset = param7;
        this.useSimplexSurfaceNoise = param8;
        this.randomDensityOffset = param9;
        this.islandNoiseOverride = param10;
        this.isAmplified = param11;
    }

    public int height() {
        return this.height;
    }

    public NoiseSamplingSettings noiseSamplingSettings() {
        return this.noiseSamplingSettings;
    }

    public NoiseSlideSettings topSlideSettings() {
        return this.topSlideSettings;
    }

    public NoiseSlideSettings bottomSlideSettings() {
        return this.bottomSlideSettings;
    }

    public int noiseSizeHorizontal() {
        return this.noiseSizeHorizontal;
    }

    public int noiseSizeVertical() {
        return this.noiseSizeVertical;
    }

    public double densityFactor() {
        return this.densityFactor;
    }

    public double densityOffset() {
        return this.densityOffset;
    }

    @Deprecated
    public boolean useSimplexSurfaceNoise() {
        return this.useSimplexSurfaceNoise;
    }

    @Deprecated
    public boolean randomDensityOffset() {
        return this.randomDensityOffset;
    }

    @Deprecated
    public boolean islandNoiseOverride() {
        return this.islandNoiseOverride;
    }

    @Deprecated
    public boolean isAmplified() {
        return this.isAmplified;
    }
}
