package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public record RandomPatchConfiguration(
    int tries, int xzSpread, int ySpread, Set<Block> allowedOn, Set<BlockState> disallowedOn, boolean onlyInAir, Supplier<ConfiguredFeature<?, ?>> feature
) implements FeatureConfiguration {
    public static final Codec<RandomPatchConfiguration> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    ExtraCodecs.POSITIVE_INT.fieldOf("tries").orElse(128).forGetter(RandomPatchConfiguration::tries),
                    ExtraCodecs.NON_NEGATIVE_INT.fieldOf("xz_spread").orElse(7).forGetter(RandomPatchConfiguration::xzSpread),
                    ExtraCodecs.NON_NEGATIVE_INT.fieldOf("y_spread").orElse(3).forGetter(RandomPatchConfiguration::ySpread),
                    Registry.BLOCK.listOf().xmap(Set::copyOf, List::copyOf).fieldOf("allowed_on").forGetter(RandomPatchConfiguration::allowedOn),
                    BlockState.CODEC.listOf().xmap(Set::copyOf, List::copyOf).fieldOf("disallowed_on").forGetter(RandomPatchConfiguration::disallowedOn),
                    Codec.BOOL.fieldOf("only_in_air").forGetter(RandomPatchConfiguration::onlyInAir),
                    ConfiguredFeature.CODEC.fieldOf("feature").forGetter(RandomPatchConfiguration::feature)
                )
                .apply(param0, RandomPatchConfiguration::new)
    );
}
