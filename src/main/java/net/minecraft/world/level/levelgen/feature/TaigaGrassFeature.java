package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;

public class TaigaGrassFeature extends Feature<NoneFeatureConfiguration> {
    public TaigaGrassFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> param0) {
        super(param0);
    }

    public BlockState getState(Random param0) {
        return param0.nextInt(5) > 0 ? Blocks.FERN.defaultBlockState() : Blocks.GRASS.defaultBlockState();
    }

    public boolean place(
        LevelAccessor param0, ChunkGenerator<? extends ChunkGeneratorSettings> param1, Random param2, BlockPos param3, NoneFeatureConfiguration param4
    ) {
        BlockState var0 = this.getState(param2);

        for(BlockState var1 = param0.getBlockState(param3);
            (var1.isAir() || var1.is(BlockTags.LEAVES)) && param3.getY() > 0;
            var1 = param0.getBlockState(param3)
        ) {
            param3 = param3.below();
        }

        int var2 = 0;

        for(int var3 = 0; var3 < 128; ++var3) {
            BlockPos var4 = param3.offset(param2.nextInt(8) - param2.nextInt(8), param2.nextInt(4) - param2.nextInt(4), param2.nextInt(8) - param2.nextInt(8));
            if (param0.isEmptyBlock(var4) && var0.canSurvive(param0, var4)) {
                param0.setBlock(var4, var0, 2);
                ++var2;
            }
        }

        return var2 > 0;
    }
}
