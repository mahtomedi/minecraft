package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Function;
import net.minecraft.world.level.dimension.DimensionType;

public record NoiseSettings(
    int minY,
    int height,
    NoiseSamplingSettings noiseSamplingSettings,
    NoiseSlider topSlideSettings,
    NoiseSlider bottomSlideSettings,
    int noiseSizeHorizontal,
    int noiseSizeVertical,
    double densityFactor,
    double densityOffset,
    @Deprecated boolean islandNoiseOverride,
    @Deprecated boolean isAmplified
) {
    public static final Codec<NoiseSettings> CODEC = RecordCodecBuilder.<NoiseSettings>create(
            param0 -> param0.group(
                        Codec.intRange(DimensionType.MIN_Y, DimensionType.MAX_Y).fieldOf("min_y").forGetter(NoiseSettings::minY),
                        Codec.intRange(0, DimensionType.Y_SIZE).fieldOf("height").forGetter(NoiseSettings::height),
                        NoiseSamplingSettings.CODEC.fieldOf("sampling").forGetter(NoiseSettings::noiseSamplingSettings),
                        NoiseSlider.CODEC.fieldOf("top_slide").forGetter(NoiseSettings::topSlideSettings),
                        NoiseSlider.CODEC.fieldOf("bottom_slide").forGetter(NoiseSettings::bottomSlideSettings),
                        Codec.intRange(1, 4).fieldOf("size_horizontal").forGetter(NoiseSettings::noiseSizeHorizontal),
                        Codec.intRange(1, 4).fieldOf("size_vertical").forGetter(NoiseSettings::noiseSizeVertical),
                        Codec.DOUBLE.fieldOf("density_factor").forGetter(NoiseSettings::densityFactor),
                        Codec.DOUBLE.fieldOf("density_offset").forGetter(NoiseSettings::densityOffset),
                        Codec.BOOL
                            .optionalFieldOf("island_noise_override", Boolean.valueOf(false), Lifecycle.experimental())
                            .forGetter(NoiseSettings::islandNoiseOverride),
                        Codec.BOOL.optionalFieldOf("amplified", Boolean.valueOf(false), Lifecycle.experimental()).forGetter(NoiseSettings::isAmplified)
                    )
                    .apply(param0, NoiseSettings::new)
        )
        .comapFlatMap(NoiseSettings::guardY, Function.identity());

    private static DataResult<NoiseSettings> guardY(NoiseSettings param0) {
        if (param0.minY() + param0.height() > DimensionType.MAX_Y + 1) {
            return DataResult.error("min_y + height cannot be higher than: " + (DimensionType.MAX_Y + 1));
        } else if (param0.height() % 16 != 0) {
            return DataResult.error("height has to be a multiple of 16");
        } else {
            return param0.minY() % 16 != 0 ? DataResult.error("min_y has to be a multiple of 16") : DataResult.success(param0);
        }
    }

    public static NoiseSettings create(
        int param0,
        int param1,
        NoiseSamplingSettings param2,
        NoiseSlider param3,
        NoiseSlider param4,
        int param5,
        int param6,
        double param7,
        double param8,
        boolean param9,
        boolean param10
    ) {
        NoiseSettings var0 = new NoiseSettings(param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10);
        guardY(var0).error().ifPresent(param0x -> {
            throw new IllegalStateException(param0x.message());
        });
        return var0;
    }
}
