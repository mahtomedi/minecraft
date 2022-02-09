package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public record RandomPatchConfiguration(int tries, int xzSpread, int ySpread, Holder<PlacedFeature> feature) implements FeatureConfiguration {
    public static final Codec<RandomPatchConfiguration> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    ExtraCodecs.POSITIVE_INT.fieldOf("tries").orElse(128).forGetter(RandomPatchConfiguration::tries),
                    ExtraCodecs.NON_NEGATIVE_INT.fieldOf("xz_spread").orElse(7).forGetter(RandomPatchConfiguration::xzSpread),
                    ExtraCodecs.NON_NEGATIVE_INT.fieldOf("y_spread").orElse(3).forGetter(RandomPatchConfiguration::ySpread),
                    PlacedFeature.CODEC.fieldOf("feature").forGetter(RandomPatchConfiguration::feature)
                )
                .apply(param0, RandomPatchConfiguration::new)
    );
}
