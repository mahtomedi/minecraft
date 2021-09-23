package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.block.state.BlockState;

public record SingleBlockStateConfiguration(BlockState state) implements DecoratorConfiguration {
    public static final Codec<SingleBlockStateConfiguration> CODEC = BlockState.CODEC
        .fieldOf("state")
        .xmap(SingleBlockStateConfiguration::new, SingleBlockStateConfiguration::state)
        .codec();
}
