package net.minecraft.world.level.block.grower;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.world.level.levelgen.feature.AbstractTreeFeature;
import net.minecraft.world.level.levelgen.feature.MegaPineTreeFeature;
import net.minecraft.world.level.levelgen.feature.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.SpruceFeature;

public class SpruceTreeGrower extends AbstractMegaTreeGrower {
    @Nullable
    @Override
    protected AbstractTreeFeature<NoneFeatureConfiguration> getFeature(Random param0) {
        return new SpruceFeature(NoneFeatureConfiguration::deserialize, true);
    }

    @Nullable
    @Override
    protected AbstractTreeFeature<NoneFeatureConfiguration> getMegaFeature(Random param0) {
        return new MegaPineTreeFeature(NoneFeatureConfiguration::deserialize, false, param0.nextBoolean());
    }
}
