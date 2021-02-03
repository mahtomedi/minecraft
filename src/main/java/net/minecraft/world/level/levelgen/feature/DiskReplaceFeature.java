package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.levelgen.feature.configurations.DiskConfiguration;

public class DiskReplaceFeature extends BaseDiskFeature {
    public DiskReplaceFeature(Codec<DiskConfiguration> param0) {
        super(param0);
    }

    @Override
    public boolean place(FeaturePlaceContext<DiskConfiguration> param0) {
        return !param0.level().getFluidState(param0.origin()).is(FluidTags.WATER) ? false : super.place(param0);
    }
}
