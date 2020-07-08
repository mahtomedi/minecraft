package net.minecraft.world.level.block.grower;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.data.worldgen.Features;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

public class OakTreeGrower extends AbstractTreeGrower {
    @Nullable
    @Override
    protected ConfiguredFeature<TreeConfiguration, ?> getConfiguredFeature(Random param0, boolean param1) {
        if (param0.nextInt(10) == 0) {
            return param1 ? Features.FANCY_OAK_BEES_005 : Features.FANCY_OAK;
        } else {
            return param1 ? Features.OAK_BEES_005 : Features.OAK;
        }
    }
}
