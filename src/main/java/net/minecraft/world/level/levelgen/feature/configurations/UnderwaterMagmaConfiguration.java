package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class UnderwaterMagmaConfiguration implements FeatureConfiguration {
    public static final Codec<UnderwaterMagmaConfiguration> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    Codec.intRange(0, 512).fieldOf("floor_search_range").forGetter(param0x -> param0x.floorSearchRange),
                    Codec.intRange(0, 64).fieldOf("placement_radius_around_floor").forGetter(param0x -> param0x.placementRadiusAroundFloor),
                    Codec.floatRange(0.0F, 1.0F)
                        .fieldOf("placement_probability_per_valid_position")
                        .forGetter(param0x -> param0x.placementProbabilityPerValidPosition)
                )
                .apply(param0, UnderwaterMagmaConfiguration::new)
    );
    public final int floorSearchRange;
    public final int placementRadiusAroundFloor;
    public final float placementProbabilityPerValidPosition;

    public UnderwaterMagmaConfiguration(int param0, int param1, float param2) {
        this.floorSearchRange = param0;
        this.placementRadiusAroundFloor = param1;
        this.placementProbabilityPerValidPosition = param2;
    }
}
