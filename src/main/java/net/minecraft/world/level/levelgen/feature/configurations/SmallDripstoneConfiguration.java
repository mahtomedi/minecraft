package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class SmallDripstoneConfiguration implements FeatureConfiguration {
    public static final Codec<SmallDripstoneConfiguration> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    Codec.intRange(0, 100).fieldOf("max_placements").orElse(5).forGetter(param0x -> param0x.maxPlacements),
                    Codec.intRange(0, 20).fieldOf("empty_space_search_radius").orElse(10).forGetter(param0x -> param0x.emptySpaceSearchRadius),
                    Codec.intRange(0, 20).fieldOf("max_offset_from_origin").orElse(2).forGetter(param0x -> param0x.maxOffsetFromOrigin),
                    Codec.floatRange(0.0F, 1.0F).fieldOf("chance_of_taller_dripstone").orElse(0.2F).forGetter(param0x -> param0x.chanceOfTallerDripstone)
                )
                .apply(param0, SmallDripstoneConfiguration::new)
    );
    public final int maxPlacements;
    public final int emptySpaceSearchRadius;
    public final int maxOffsetFromOrigin;
    public final float chanceOfTallerDripstone;

    public SmallDripstoneConfiguration(int param0, int param1, int param2, float param3) {
        this.maxPlacements = param0;
        this.emptySpaceSearchRadius = param1;
        this.maxOffsetFromOrigin = param2;
        this.chanceOfTallerDripstone = param3;
    }
}
