package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;

public class RandomPatchFeature extends Feature<RandomPatchConfiguration> {
    public RandomPatchFeature(Function<Dynamic<?>, ? extends RandomPatchConfiguration> param0, Function<Random, ? extends RandomPatchConfiguration> param1) {
        super(param0, param1);
    }

    public boolean place(
        LevelAccessor param0, ChunkGenerator<? extends ChunkGeneratorSettings> param1, Random param2, BlockPos param3, RandomPatchConfiguration param4
    ) {
        BlockState var0 = param4.stateProvider.getState(param2, param3);
        BlockPos var1;
        if (param4.project) {
            var1 = param0.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, param3);
        } else {
            var1 = param3;
        }

        int var3 = 0;
        BlockPos.MutableBlockPos var4 = new BlockPos.MutableBlockPos();

        for(int var5 = 0; var5 < param4.tries; ++var5) {
            var4.setWithOffset(
                var1,
                param2.nextInt(param4.xspread + 1) - param2.nextInt(param4.xspread + 1),
                param2.nextInt(param4.yspread + 1) - param2.nextInt(param4.yspread + 1),
                param2.nextInt(param4.zspread + 1) - param2.nextInt(param4.zspread + 1)
            );
            BlockPos var6 = var4.below();
            BlockState var7 = param0.getBlockState(var6);
            if ((param0.isEmptyBlock(var4) || param4.canReplace && param0.getBlockState(var4).getMaterial().isReplaceable())
                && var0.canSurvive(param0, var4)
                && (param4.whitelist.isEmpty() || param4.whitelist.contains(var7.getBlock()))
                && !param4.blacklist.contains(var7)
                && (
                    !param4.needWater
                        || param0.getFluidState(var6.west()).is(FluidTags.WATER)
                        || param0.getFluidState(var6.east()).is(FluidTags.WATER)
                        || param0.getFluidState(var6.north()).is(FluidTags.WATER)
                        || param0.getFluidState(var6.south()).is(FluidTags.WATER)
                )) {
                param4.blockPlacer.place(param0, var4, var0, param2);
                ++var3;
            }
        }

        return var3 > 0;
    }
}
