package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;

public class GrassFeature extends Feature<GrassConfiguration> {
    public GrassFeature(Function<Dynamic<?>, ? extends GrassConfiguration> param0) {
        super(param0);
    }

    public boolean place(
        LevelAccessor param0, ChunkGenerator<? extends ChunkGeneratorSettings> param1, Random param2, BlockPos param3, GrassConfiguration param4
    ) {
        for(BlockState var0 = param0.getBlockState(param3);
            (var0.isAir() || var0.is(BlockTags.LEAVES)) && param3.getY() > 0;
            var0 = param0.getBlockState(param3)
        ) {
            param3 = param3.below();
        }

        int var1 = 0;

        for(int var2 = 0; var2 < 128; ++var2) {
            BlockPos var3 = param3.offset(param2.nextInt(8) - param2.nextInt(8), param2.nextInt(4) - param2.nextInt(4), param2.nextInt(8) - param2.nextInt(8));
            if (param0.isEmptyBlock(var3) && param4.state.canSurvive(param0, var3)) {
                param0.setBlock(var3, param4.state, 2);
                ++var1;
            }
        }

        return var1 > 0;
    }
}
