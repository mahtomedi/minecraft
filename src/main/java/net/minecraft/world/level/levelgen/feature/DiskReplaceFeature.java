package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.feature.configurations.DiskConfiguration;

public class DiskReplaceFeature extends BaseDiskFeature {
    public DiskReplaceFeature(Codec<DiskConfiguration> param0) {
        super(param0);
    }

    @Override
    public boolean place(FeaturePlaceContext<DiskConfiguration> param0) {
        return !param0.level().getBlockState(param0.origin()).is(param0.config().canOriginReplace()) ? false : super.place(param0);
    }
}
