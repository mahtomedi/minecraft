package net.minecraft.world.level.block.grower;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class DarkOakTreeGrower extends AbstractMegaTreeGrower {
    @Nullable
    @Override
    protected ConfiguredFeature<?, ?> getConfiguredFeature(Random param0, boolean param1) {
        return null;
    }

    @Nullable
    @Override
    protected ConfiguredFeature<?, ?> getConfiguredMegaFeature(Random param0) {
        return TreeFeatures.DARK_OAK;
    }
}
