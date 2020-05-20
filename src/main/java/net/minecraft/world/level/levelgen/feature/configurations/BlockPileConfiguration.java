package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public class BlockPileConfiguration implements FeatureConfiguration {
    public static final Codec<BlockPileConfiguration> CODEC = BlockStateProvider.CODEC
        .fieldOf("state_provider")
        .xmap(BlockPileConfiguration::new, param0 -> param0.stateProvider)
        .codec();
    public final BlockStateProvider stateProvider;

    public BlockPileConfiguration(BlockStateProvider param0) {
        this.stateProvider = param0;
    }
}
