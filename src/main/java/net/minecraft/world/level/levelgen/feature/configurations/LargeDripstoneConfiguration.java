package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.UniformFloat;
import net.minecraft.util.UniformInt;

public class LargeDripstoneConfiguration implements FeatureConfiguration {
    public static final Codec<LargeDripstoneConfiguration> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    Codec.intRange(1, 512).fieldOf("floor_to_ceiling_search_range").orElse(30).forGetter(param0x -> param0x.floorToCeilingSearchRange),
                    UniformInt.codec(1, 30, 30).fieldOf("column_radius").forGetter(param0x -> param0x.columnRadius),
                    UniformFloat.codec(0.0F, 10.0F, 10.0F).fieldOf("height_scale").forGetter(param0x -> param0x.heightScale),
                    Codec.floatRange(0.1F, 1.0F)
                        .fieldOf("max_column_radius_to_cave_height_ratio")
                        .forGetter(param0x -> param0x.maxColumnRadiusToCaveHeightRatio),
                    UniformFloat.codec(0.1F, 5.0F, 5.0F).fieldOf("stalactite_bluntness").forGetter(param0x -> param0x.stalactiteBluntness),
                    UniformFloat.codec(0.1F, 5.0F, 5.0F).fieldOf("stalagmite_bluntness").forGetter(param0x -> param0x.stalagmiteBluntness),
                    UniformFloat.codec(0.0F, 1.0F, 1.0F).fieldOf("wind_speed").forGetter(param0x -> param0x.windSpeed),
                    Codec.intRange(0, 100).fieldOf("min_radius_for_wind").forGetter(param0x -> param0x.minRadiusForWind),
                    Codec.floatRange(0.0F, 5.0F).fieldOf("min_bluntness_for_wind").forGetter(param0x -> param0x.minBluntnessForWind)
                )
                .apply(param0, LargeDripstoneConfiguration::new)
    );
    public final int floorToCeilingSearchRange;
    public final UniformInt columnRadius;
    public final UniformFloat heightScale;
    public final float maxColumnRadiusToCaveHeightRatio;
    public final UniformFloat stalactiteBluntness;
    public final UniformFloat stalagmiteBluntness;
    public final UniformFloat windSpeed;
    public final int minRadiusForWind;
    public final float minBluntnessForWind;

    public LargeDripstoneConfiguration(
        int param0,
        UniformInt param1,
        UniformFloat param2,
        float param3,
        UniformFloat param4,
        UniformFloat param5,
        UniformFloat param6,
        int param7,
        float param8
    ) {
        this.floorToCeilingSearchRange = param0;
        this.columnRadius = param1;
        this.heightScale = param2;
        this.maxColumnRadiusToCaveHeightRatio = param3;
        this.stalactiteBluntness = param4;
        this.stalagmiteBluntness = param5;
        this.windSpeed = param6;
        this.minRadiusForWind = param7;
        this.minBluntnessForWind = param8;
    }
}
