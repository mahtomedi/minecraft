package net.minecraft.world.level.block.grower;

import javax.annotation.Nullable;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class MangroveTreeGrower extends AbstractTreeGrower {
    private final float tallProbability;

    public MangroveTreeGrower(float param0) {
        this.tallProbability = param0;
    }

    @Nullable
    @Override
    protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(RandomSource param0, boolean param1) {
        return param0.nextFloat() < this.tallProbability ? TreeFeatures.TALL_MANGROVE : TreeFeatures.MANGROVE;
    }
}
