package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;

public class RandomPatchFeature extends Feature<RandomPatchConfiguration> {
    public RandomPatchFeature(Codec<RandomPatchConfiguration> param0) {
        super(param0);
    }

    @Override
    public boolean place(FeaturePlaceContext<RandomPatchConfiguration> param0) {
        RandomPatchConfiguration var0 = param0.config();
        Random var1 = param0.random();
        BlockPos var2 = param0.origin();
        WorldGenLevel var3 = param0.level();
        BlockState var4 = var0.stateProvider.getState(var1, var2);
        BlockPos var5;
        if (var0.project) {
            var5 = var3.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, var2);
        } else {
            var5 = var2;
        }

        int var7 = 0;
        BlockPos.MutableBlockPos var8 = new BlockPos.MutableBlockPos();

        for(int var9 = 0; var9 < var0.tries; ++var9) {
            var8.setWithOffset(
                var5,
                var1.nextInt(var0.xspread + 1) - var1.nextInt(var0.xspread + 1),
                var1.nextInt(var0.yspread + 1) - var1.nextInt(var0.yspread + 1),
                var1.nextInt(var0.zspread + 1) - var1.nextInt(var0.zspread + 1)
            );
            BlockPos var10 = var8.below();
            BlockState var11 = var3.getBlockState(var10);
            if ((var3.isEmptyBlock(var8) || var0.canReplace && var3.getBlockState(var8).getMaterial().isReplaceable())
                && var4.canSurvive(var3, var8)
                && (var0.whitelist.isEmpty() || var0.whitelist.contains(var11.getBlock()))
                && !var0.blacklist.contains(var11)
                && (
                    !var0.needWater
                        || var3.getFluidState(var10.west()).is(FluidTags.WATER)
                        || var3.getFluidState(var10.east()).is(FluidTags.WATER)
                        || var3.getFluidState(var10.north()).is(FluidTags.WATER)
                        || var3.getFluidState(var10.south()).is(FluidTags.WATER)
                )) {
                var0.blockPlacer.place(var3, var8, var4, var1);
                ++var7;
            }
        }

        return var7 > 0;
    }
}
