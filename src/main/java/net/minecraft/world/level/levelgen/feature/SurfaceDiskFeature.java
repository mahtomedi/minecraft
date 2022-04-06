package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.DiskConfiguration;

public class SurfaceDiskFeature extends BaseDiskFeature {
    public SurfaceDiskFeature(Codec<DiskConfiguration> param0) {
        super(param0);
    }

    @Override
    public boolean place(FeaturePlaceContext<DiskConfiguration> param0) {
        return !param0.level().getBlockState(param0.origin().below()).is(param0.config().canOriginReplace()) ? false : super.place(param0);
    }

    @Override
    protected boolean placeColumn(DiskConfiguration param0, WorldGenLevel param1, int param2, int param3, BlockPos.MutableBlockPos param4) {
        if (!param1.isEmptyBlock(param4.setY(param2 + 1))) {
            return false;
        } else {
            for(int var0 = param2; var0 > param3; --var0) {
                BlockState var1 = param1.getBlockState(param4.setY(var0));
                if (this.matchesTargetBlock(param0, var1)) {
                    param1.setBlock(param4, param0.state(), 2);
                    this.markAboveForPostProcessing(param1, param4);
                    return true;
                }

                if (!var1.isAir()) {
                    return false;
                }
            }

            return false;
        }
    }
}
