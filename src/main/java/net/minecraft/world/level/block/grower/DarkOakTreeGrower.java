package net.minecraft.world.level.block.grower;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.world.level.biome.BiomeDefaultFeatures;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

public class DarkOakTreeGrower extends AbstractMegaTreeGrower {
    @Nullable
    @Override
    protected ConfiguredFeature<? extends TreeConfiguration, ?> getConfiguredFeature(Random param0, boolean param1) {
        return null;
    }

    @Nullable
    @Override
    protected ConfiguredFeature<? extends TreeConfiguration, ?> getConfiguredMegaFeature(Random param0) {
        return Feature.DARK_OAK_TREE.configured(BiomeDefaultFeatures.DARK_OAK_TREE_CONFIG);
    }
}
