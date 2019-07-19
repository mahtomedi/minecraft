package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;

public class ReedsFeature extends Feature<NoneFeatureConfiguration> {
    public ReedsFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> param0) {
        super(param0);
    }

    public boolean place(
        LevelAccessor param0, ChunkGenerator<? extends ChunkGeneratorSettings> param1, Random param2, BlockPos param3, NoneFeatureConfiguration param4
    ) {
        int var0 = 0;

        for(int var1 = 0; var1 < 20; ++var1) {
            BlockPos var2 = param3.offset(param2.nextInt(4) - param2.nextInt(4), 0, param2.nextInt(4) - param2.nextInt(4));
            if (param0.isEmptyBlock(var2)) {
                BlockPos var3 = var2.below();
                if (param0.getFluidState(var3.west()).is(FluidTags.WATER)
                    || param0.getFluidState(var3.east()).is(FluidTags.WATER)
                    || param0.getFluidState(var3.north()).is(FluidTags.WATER)
                    || param0.getFluidState(var3.south()).is(FluidTags.WATER)) {
                    int var4 = 2 + param2.nextInt(param2.nextInt(3) + 1);

                    for(int var5 = 0; var5 < var4; ++var5) {
                        if (Blocks.SUGAR_CANE.defaultBlockState().canSurvive(param0, var2)) {
                            param0.setBlock(var2.above(var5), Blocks.SUGAR_CANE.defaultBlockState(), 2);
                            ++var0;
                        }
                    }
                }
            }
        }

        return var0 > 0;
    }
}
