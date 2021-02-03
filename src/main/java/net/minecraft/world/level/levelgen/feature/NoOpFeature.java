package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class NoOpFeature extends Feature<NoneFeatureConfiguration> {
    public NoOpFeature(Codec<NoneFeatureConfiguration> param0) {
        super(param0);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> param0) {
        return true;
    }
}
