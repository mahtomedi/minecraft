package net.minecraft.world.level.block.grower;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.world.level.biome.BiomeDefaultFeatures;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

public class AcaciaTreeGrower extends AbstractTreeGrower {
    @Nullable
    @Override
    protected ConfiguredFeature<TreeConfiguration, ?> getConfiguredFeature(Random param0, boolean param1) {
        return Feature.TREE.configured(BiomeDefaultFeatures.ACACIA_TREE_CONFIG);
    }
}
