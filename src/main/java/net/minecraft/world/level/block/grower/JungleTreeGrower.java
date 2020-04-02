package net.minecraft.world.level.block.grower;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.world.level.biome.BiomeDefaultFeatures;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.SmallTreeConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

public class JungleTreeGrower extends AbstractMegaTreeGrower {
    @Nullable
    @Override
    protected ConfiguredFeature<? extends TreeConfiguration, ?> getConfiguredFeature(Random param0, boolean param1) {
        return new TreeFeature(SmallTreeConfiguration::deserialize).configured(BiomeDefaultFeatures.JUNGLE_TREE_NOVINE_CONFIG);
    }

    @Nullable
    @Override
    protected ConfiguredFeature<? extends TreeConfiguration, ?> getConfiguredMegaFeature(Random param0) {
        return Feature.MEGA_JUNGLE_TREE.configured(BiomeDefaultFeatures.MEGA_JUNGLE_TREE_CONFIG);
    }
}
