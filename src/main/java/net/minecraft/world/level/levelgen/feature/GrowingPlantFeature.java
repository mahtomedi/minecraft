package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.GrowingPlantConfiguration;

public class GrowingPlantFeature extends Feature<GrowingPlantConfiguration> {
    public GrowingPlantFeature(Codec<GrowingPlantConfiguration> param0) {
        super(param0);
    }

    @Override
    public boolean place(FeaturePlaceContext<GrowingPlantConfiguration> param0) {
        LevelAccessor var0 = param0.level();
        GrowingPlantConfiguration var1 = param0.config();
        Random var2 = param0.random();
        int var3 = var1.heightDistribution.getOne(var2).sample(var2);
        BlockPos.MutableBlockPos var4 = param0.origin().mutable();
        BlockPos.MutableBlockPos var5 = var4.mutable().move(var1.direction);
        BlockState var6 = var0.getBlockState(var4);

        for(int var7 = 1; var7 <= var3; ++var7) {
            BlockState var8 = var6;
            var6 = var0.getBlockState(var5);
            if (var8.isAir() || var1.allowWater && var8.getFluidState().is(FluidTags.WATER)) {
                if (var7 == var3 || !var6.isAir()) {
                    var0.setBlock(var4, var1.headProvider.getState(var2, var4), 2);
                    break;
                }

                var0.setBlock(var4, var1.bodyProvider.getState(var2, var4), 2);
            }

            var5.move(var1.direction);
            var4.move(var1.direction);
        }

        return true;
    }
}
