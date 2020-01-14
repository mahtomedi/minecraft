package net.minecraft.world.level.block.grower;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.world.level.biome.BiomeDefaultFeatures;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.SmallTreeConfiguration;

public class OakTreeGrower extends AbstractTreeGrower {
    @Nullable
    @Override
    protected ConfiguredFeature<SmallTreeConfiguration, ?> getConfiguredFeature(Random param0, boolean param1) {
        return param0.nextInt(10) == 0
            ? Feature.FANCY_TREE.configured(param1 ? BiomeDefaultFeatures.FANCY_TREE_WITH_BEES_005_CONFIG : BiomeDefaultFeatures.FANCY_TREE_CONFIG)
            : Feature.NORMAL_TREE.configured(param1 ? BiomeDefaultFeatures.NORMAL_TREE_WITH_BEES_005_CONFIG : BiomeDefaultFeatures.NORMAL_TREE_CONFIG);
    }
}
