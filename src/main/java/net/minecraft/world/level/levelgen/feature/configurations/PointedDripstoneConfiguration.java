package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class PointedDripstoneConfiguration implements FeatureConfiguration {
    public static final Codec<PointedDripstoneConfiguration> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    Codec.floatRange(0.0F, 1.0F).fieldOf("chance_of_taller_dripstone").orElse(0.2F).forGetter(param0x -> param0x.chanceOfTallerDripstone),
                    Codec.floatRange(0.0F, 1.0F).fieldOf("chance_of_directional_spread").orElse(0.7F).forGetter(param0x -> param0x.chanceOfDirectionalSpread),
                    Codec.floatRange(0.0F, 1.0F).fieldOf("chance_of_spread_radius2").orElse(0.5F).forGetter(param0x -> param0x.chanceOfSpreadRadius2),
                    Codec.floatRange(0.0F, 1.0F).fieldOf("chance_of_spread_radius3").orElse(0.5F).forGetter(param0x -> param0x.chanceOfSpreadRadius3)
                )
                .apply(param0, PointedDripstoneConfiguration::new)
    );
    public final float chanceOfTallerDripstone;
    public final float chanceOfDirectionalSpread;
    public final float chanceOfSpreadRadius2;
    public final float chanceOfSpreadRadius3;

    public PointedDripstoneConfiguration(float param0, float param1, float param2, float param3) {
        this.chanceOfTallerDripstone = param0;
        this.chanceOfDirectionalSpread = param1;
        this.chanceOfSpreadRadius2 = param2;
        this.chanceOfSpreadRadius3 = param3;
    }
}
