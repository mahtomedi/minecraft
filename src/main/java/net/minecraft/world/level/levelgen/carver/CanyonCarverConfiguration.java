package net.minecraft.world.level.levelgen.carver;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;

public class CanyonCarverConfiguration extends CarverConfiguration {
    public static final Codec<CanyonCarverConfiguration> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    CarverConfiguration.CODEC.forGetter(param0x -> param0x),
                    FloatProvider.CODEC.fieldOf("vertical_rotation").forGetter(param0x -> param0x.verticalRotation),
                    CanyonCarverConfiguration.CanyonShapeConfiguration.CODEC.fieldOf("shape").forGetter(param0x -> param0x.shape)
                )
                .apply(param0, CanyonCarverConfiguration::new)
    );
    public final FloatProvider verticalRotation;
    public final CanyonCarverConfiguration.CanyonShapeConfiguration shape;

    public CanyonCarverConfiguration(
        float param0,
        HeightProvider param1,
        FloatProvider param2,
        VerticalAnchor param3,
        CarverDebugSettings param4,
        FloatProvider param5,
        CanyonCarverConfiguration.CanyonShapeConfiguration param6
    ) {
        super(param0, param1, param2, param3, param4);
        this.verticalRotation = param5;
        this.shape = param6;
    }

    public CanyonCarverConfiguration(CarverConfiguration param0, FloatProvider param1, CanyonCarverConfiguration.CanyonShapeConfiguration param2) {
        this(param0.probability, param0.y, param0.yScale, param0.lavaLevel, param0.debugSettings, param1, param2);
    }

    public static class CanyonShapeConfiguration {
        public static final Codec<CanyonCarverConfiguration.CanyonShapeConfiguration> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(
                        FloatProvider.CODEC.fieldOf("distance_factor").forGetter(param0x -> param0x.distanceFactor),
                        FloatProvider.CODEC.fieldOf("thickness").forGetter(param0x -> param0x.thickness),
                        Codec.intRange(0, Integer.MAX_VALUE).fieldOf("width_smoothness").forGetter(param0x -> param0x.widthSmoothness),
                        FloatProvider.CODEC.fieldOf("horizontal_radius_factor").forGetter(param0x -> param0x.horizontalRadiusFactor),
                        Codec.FLOAT.fieldOf("vertical_radius_default_factor").forGetter(param0x -> param0x.verticalRadiusDefaultFactor),
                        Codec.FLOAT.fieldOf("vertical_radius_center_factor").forGetter(param0x -> param0x.verticalRadiusCenterFactor)
                    )
                    .apply(param0, CanyonCarverConfiguration.CanyonShapeConfiguration::new)
        );
        public final FloatProvider distanceFactor;
        public final FloatProvider thickness;
        public final int widthSmoothness;
        public final FloatProvider horizontalRadiusFactor;
        public final float verticalRadiusDefaultFactor;
        public final float verticalRadiusCenterFactor;

        public CanyonShapeConfiguration(FloatProvider param0, FloatProvider param1, int param2, FloatProvider param3, float param4, float param5) {
            this.widthSmoothness = param2;
            this.horizontalRadiusFactor = param3;
            this.verticalRadiusDefaultFactor = param4;
            this.verticalRadiusCenterFactor = param5;
            this.distanceFactor = param0;
            this.thickness = param1;
        }
    }
}
