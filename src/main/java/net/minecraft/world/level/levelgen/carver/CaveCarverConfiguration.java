package net.minecraft.world.level.levelgen.carver;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;

public class CaveCarverConfiguration extends CarverConfiguration {
    public static final Codec<CaveCarverConfiguration> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    CarverConfiguration.CODEC.forGetter(param0x -> param0x),
                    FloatProvider.CODEC.fieldOf("horizontal_radius_multiplier").forGetter(param0x -> param0x.horizontalRadiusMultiplier),
                    FloatProvider.CODEC.fieldOf("vertical_radius_multiplier").forGetter(param0x -> param0x.verticalRadiusMultiplier),
                    FloatProvider.codec(-1.0F, 1.0F).fieldOf("floor_level").forGetter(param0x -> param0x.floorLevel)
                )
                .apply(param0, CaveCarverConfiguration::new)
    );
    public final FloatProvider horizontalRadiusMultiplier;
    public final FloatProvider verticalRadiusMultiplier;
    final FloatProvider floorLevel;

    public CaveCarverConfiguration(
        float param0,
        HeightProvider param1,
        FloatProvider param2,
        VerticalAnchor param3,
        CarverDebugSettings param4,
        FloatProvider param5,
        FloatProvider param6,
        FloatProvider param7
    ) {
        super(param0, param1, param2, param3, param4);
        this.horizontalRadiusMultiplier = param5;
        this.verticalRadiusMultiplier = param6;
        this.floorLevel = param7;
    }

    public CaveCarverConfiguration(
        float param0, HeightProvider param1, FloatProvider param2, VerticalAnchor param3, FloatProvider param4, FloatProvider param5, FloatProvider param6
    ) {
        this(param0, param1, param2, param3, CarverDebugSettings.DEFAULT, param4, param5, param6);
    }

    public CaveCarverConfiguration(CarverConfiguration param0, FloatProvider param1, FloatProvider param2, FloatProvider param3) {
        this(param0.probability, param0.y, param0.yScale, param0.lavaLevel, param0.debugSettings, param1, param2, param3);
    }
}
