package net.minecraft.world.level.levelgen.carver;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.FloatProvider;
import net.minecraft.util.UniformInt;
import net.minecraft.world.level.levelgen.VerticalAnchor;

public class CanyonCarverConfiguration extends CarverConfiguration {
    public static final Codec<CanyonCarverConfiguration> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    Codec.floatRange(0.0F, 1.0F).fieldOf("probability").forGetter(param0x -> param0x.probability),
                    CarverDebugSettings.CODEC.optionalFieldOf("debug_settings", CarverDebugSettings.DEFAULT).forGetter(CarverConfiguration::getDebugSettings),
                    VerticalAnchor.CODEC.fieldOf("bottom_inclusive").forGetter(CanyonCarverConfiguration::getBottomInclusive),
                    VerticalAnchor.CODEC.fieldOf("top_inclusive").forGetter(CanyonCarverConfiguration::getTopInclusive),
                    UniformInt.CODEC.fieldOf("y_scale").forGetter(CanyonCarverConfiguration::getYScale),
                    FloatProvider.codec(0.0F, 1.0F).fieldOf("distanceFactor").forGetter(CanyonCarverConfiguration::getDistanceFactor),
                    FloatProvider.CODEC.fieldOf("vertical_rotation").forGetter(CanyonCarverConfiguration::getVerticalRotation),
                    FloatProvider.CODEC.fieldOf("thickness").forGetter(CanyonCarverConfiguration::getThickness),
                    Codec.intRange(0, Integer.MAX_VALUE).fieldOf("width_smoothness").forGetter(CanyonCarverConfiguration::getWidthSmoothness),
                    FloatProvider.CODEC.fieldOf("horizontal_radius_factor").forGetter(CanyonCarverConfiguration::getHorizontalRadiusFactor),
                    Codec.FLOAT.fieldOf("vertical_radius_default_factor").forGetter(CanyonCarverConfiguration::getVerticalRadiusDefaultFactor),
                    Codec.FLOAT.fieldOf("vertical_radius_center_factor").forGetter(CanyonCarverConfiguration::getVerticalRadiusCenterFactor)
                )
                .apply(param0, CanyonCarverConfiguration::new)
    );
    private final VerticalAnchor bottomInclusive;
    private final VerticalAnchor topInclusive;
    private final UniformInt yScale;
    private final FloatProvider distanceFactor;
    private final FloatProvider verticalRotation;
    private final FloatProvider thickness;
    private final int widthSmoothness;
    private final FloatProvider horizontalRadiusFactor;
    private final float verticalRadiusDefaultFactor;
    private final float verticalRadiusCenterFactor;

    public CanyonCarverConfiguration(
        float param0,
        CarverDebugSettings param1,
        VerticalAnchor param2,
        VerticalAnchor param3,
        UniformInt param4,
        FloatProvider param5,
        FloatProvider param6,
        FloatProvider param7,
        int param8,
        FloatProvider param9,
        float param10,
        float param11
    ) {
        super(param0, param1);
        this.bottomInclusive = param2;
        this.topInclusive = param3;
        this.yScale = param4;
        this.distanceFactor = param5;
        this.verticalRotation = param6;
        this.thickness = param7;
        this.widthSmoothness = param8;
        this.horizontalRadiusFactor = param9;
        this.verticalRadiusDefaultFactor = param10;
        this.verticalRadiusCenterFactor = param11;
    }

    public VerticalAnchor getBottomInclusive() {
        return this.bottomInclusive;
    }

    public VerticalAnchor getTopInclusive() {
        return this.topInclusive;
    }

    public UniformInt getYScale() {
        return this.yScale;
    }

    public FloatProvider getDistanceFactor() {
        return this.distanceFactor;
    }

    public FloatProvider getVerticalRotation() {
        return this.verticalRotation;
    }

    public FloatProvider getThickness() {
        return this.thickness;
    }

    public int getWidthSmoothness() {
        return this.widthSmoothness;
    }

    public FloatProvider getHorizontalRadiusFactor() {
        return this.horizontalRadiusFactor;
    }

    public float getVerticalRadiusDefaultFactor() {
        return this.verticalRadiusDefaultFactor;
    }

    public float getVerticalRadiusCenterFactor() {
        return this.verticalRadiusCenterFactor;
    }
}
