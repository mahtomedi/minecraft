package net.minecraft.world.level.block.grower;

import java.util.Random;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class OakTreeGrower extends AbstractTreeGrower {
    @Override
    protected ConfiguredFeature<?, ?> getConfiguredFeature(Random param0, boolean param1) {
        if (param0.nextInt(10) == 0) {
            return param1 ? TreeFeatures.FANCY_OAK_BEES_005 : TreeFeatures.FANCY_OAK;
        } else {
            return param1 ? TreeFeatures.OAK_BEES_005 : TreeFeatures.OAK;
        }
    }
}
