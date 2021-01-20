package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.UniformFloat;
import net.minecraft.util.UniformInt;

public class DripstoneClusterConfiguration implements FeatureConfiguration {
    public static final Codec<DripstoneClusterConfiguration> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    Codec.intRange(1, 512).fieldOf("floor_to_ceiling_search_range").forGetter(param0x -> param0x.floorToCeilingSearchRange),
                    UniformInt.codec(1, 64, 64).fieldOf("height").forGetter(param0x -> param0x.height),
                    UniformInt.codec(1, 64, 64).fieldOf("radius").forGetter(param0x -> param0x.radius),
                    Codec.intRange(0, 64).fieldOf("max_stalagmite_stalactite_height_diff").forGetter(param0x -> param0x.maxStalagmiteStalactiteHeightDiff),
                    Codec.intRange(1, 64).fieldOf("height_deviation").forGetter(param0x -> param0x.heightDeviation),
                    UniformInt.codec(0, 64, 64).fieldOf("dripstone_block_layer_thickness").forGetter(param0x -> param0x.dripstoneBlockLayerThickness),
                    UniformFloat.codec(0.0F, 1.0F, 1.0F).fieldOf("density").forGetter(param0x -> param0x.density),
                    UniformFloat.codec(0.0F, 1.0F, 1.0F).fieldOf("wetness").forGetter(param0x -> param0x.wetness),
                    Codec.floatRange(0.0F, 1.0F).fieldOf("wetness_mean").forGetter(param0x -> param0x.wetnessMean),
                    Codec.floatRange(0.0F, 1.0F).fieldOf("wetness_deviation").forGetter(param0x -> param0x.wetnessDeviation),
                    Codec.floatRange(0.0F, 1.0F)
                        .fieldOf("chance_of_dripstone_column_at_max_distance_from_center")
                        .forGetter(param0x -> param0x.chanceOfDripstoneColumnAtMaxDistanceFromCenter),
                    Codec.intRange(1, 64)
                        .fieldOf("max_distance_from_center_affecting_chance_of_dripstone_column")
                        .forGetter(param0x -> param0x.maxDistanceFromCenterAffectingChanceOfDripstoneColumn),
                    Codec.intRange(1, 64)
                        .fieldOf("max_distance_from_center_affecting_height_bias")
                        .forGetter(param0x -> param0x.maxDistanceFromCenterAffectingHeightBias)
                )
                .apply(param0, DripstoneClusterConfiguration::new)
    );
    public final int floorToCeilingSearchRange;
    public final UniformInt height;
    public final UniformInt radius;
    public final int maxStalagmiteStalactiteHeightDiff;
    public final int heightDeviation;
    public final UniformInt dripstoneBlockLayerThickness;
    public final UniformFloat density;
    public final UniformFloat wetness;
    public final float wetnessMean;
    public final float wetnessDeviation;
    public final float chanceOfDripstoneColumnAtMaxDistanceFromCenter;
    public final int maxDistanceFromCenterAffectingChanceOfDripstoneColumn;
    public final int maxDistanceFromCenterAffectingHeightBias;

    public DripstoneClusterConfiguration(
        int param0,
        UniformInt param1,
        UniformInt param2,
        int param3,
        int param4,
        UniformInt param5,
        UniformFloat param6,
        UniformFloat param7,
        float param8,
        float param9,
        float param10,
        int param11,
        int param12
    ) {
        this.floorToCeilingSearchRange = param0;
        this.height = param1;
        this.radius = param2;
        this.maxStalagmiteStalactiteHeightDiff = param3;
        this.heightDeviation = param4;
        this.dripstoneBlockLayerThickness = param5;
        this.density = param6;
        this.wetness = param7;
        this.wetnessMean = param8;
        this.wetnessDeviation = param9;
        this.chanceOfDripstoneColumnAtMaxDistanceFromCenter = param10;
        this.maxDistanceFromCenterAffectingChanceOfDripstoneColumn = param11;
        this.maxDistanceFromCenterAffectingHeightBias = param12;
    }
}
