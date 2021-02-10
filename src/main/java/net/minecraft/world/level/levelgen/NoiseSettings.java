package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Function;
import net.minecraft.world.level.dimension.DimensionType;

public class NoiseSettings {
    public static final Codec<NoiseSettings> CODEC = RecordCodecBuilder.<NoiseSettings>create(
            param0 -> param0.group(
                        Codec.intRange(DimensionType.MIN_Y, DimensionType.MAX_Y).fieldOf("min_y").forGetter(NoiseSettings::minY),
                        Codec.intRange(0, DimensionType.Y_SIZE).fieldOf("height").forGetter(NoiseSettings::height),
                        NoiseSamplingSettings.CODEC.fieldOf("sampling").forGetter(NoiseSettings::noiseSamplingSettings),
                        NoiseSlideSettings.CODEC.fieldOf("top_slide").forGetter(NoiseSettings::topSlideSettings),
                        NoiseSlideSettings.CODEC.fieldOf("bottom_slide").forGetter(NoiseSettings::bottomSlideSettings),
                        Codec.intRange(1, 4).fieldOf("size_horizontal").forGetter(NoiseSettings::noiseSizeHorizontal),
                        Codec.intRange(1, 4).fieldOf("size_vertical").forGetter(NoiseSettings::noiseSizeVertical),
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
        )
        .comapFlatMap(NoiseSettings::guardY, Function.identity());
    private final int minY;
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

    private static DataResult<NoiseSettings> guardY(NoiseSettings param0) {
        if (param0.minY() + param0.height() > DimensionType.MAX_Y + 1) {
            return DataResult.error("min_y + height cannot be higher than: " + (DimensionType.MAX_Y + 1));
        } else if (param0.height() % 16 != 0) {
            return DataResult.error("height has to be a multiple of 16");
        } else {
            return param0.minY() % 16 != 0 ? DataResult.error("min_y has to be a multiple of 16") : DataResult.success(param0);
        }
    }

    private NoiseSettings(
        int param0,
        int param1,
        NoiseSamplingSettings param2,
        NoiseSlideSettings param3,
        NoiseSlideSettings param4,
        int param5,
        int param6,
        double param7,
        double param8,
        boolean param9,
        boolean param10,
        boolean param11,
        boolean param12
    ) {
        this.minY = param0;
        this.height = param1;
        this.noiseSamplingSettings = param2;
        this.topSlideSettings = param3;
        this.bottomSlideSettings = param4;
        this.noiseSizeHorizontal = param5;
        this.noiseSizeVertical = param6;
        this.densityFactor = param7;
        this.densityOffset = param8;
        this.useSimplexSurfaceNoise = param9;
        this.randomDensityOffset = param10;
        this.islandNoiseOverride = param11;
        this.isAmplified = param12;
    }

    public static NoiseSettings create(
        int param0,
        int param1,
        NoiseSamplingSettings param2,
        NoiseSlideSettings param3,
        NoiseSlideSettings param4,
        int param5,
        int param6,
        double param7,
        double param8,
        boolean param9,
        boolean param10,
        boolean param11,
        boolean param12
    ) {
        NoiseSettings var0 = new NoiseSettings(param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10, param11, param12);
        guardY(var0).error().ifPresent(param0x -> {
            throw new IllegalStateException(param0x.message());
        });
        return var0;
    }

    public int minY() {
        return this.minY;
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
