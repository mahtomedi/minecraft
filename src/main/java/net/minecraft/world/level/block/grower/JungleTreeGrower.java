package net.minecraft.world.level.block.grower;

import java.util.Random;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class JungleTreeGrower extends AbstractMegaTreeGrower {
    @Override
    protected ConfiguredFeature<?, ?> getConfiguredFeature(Random param0, boolean param1) {
        return TreeFeatures.JUNGLE_TREE_NO_VINE;
    }

    @Override
    protected ConfiguredFeature<?, ?> getConfiguredMegaFeature(Random param0) {
        return TreeFeatures.MEGA_JUNGLE_TREE;
    }
}
