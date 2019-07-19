package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;

public class SeaPickleFeature extends Feature<CountFeatureConfiguration> {
    public SeaPickleFeature(Function<Dynamic<?>, ? extends CountFeatureConfiguration> param0) {
        super(param0);
    }

    public boolean place(LevelAccessor param0, ChunkGenerator<?> param1, Random param2, BlockPos param3, CountFeatureConfiguration param4) {
        int var0 = 0;

        for(int var1 = 0; var1 < param4.count; ++var1) {
            int var2 = param2.nextInt(8) - param2.nextInt(8);
            int var3 = param2.nextInt(8) - param2.nextInt(8);
            int var4 = param0.getHeight(Heightmap.Types.OCEAN_FLOOR, param3.getX() + var2, param3.getZ() + var3);
            BlockPos var5 = new BlockPos(param3.getX() + var2, var4, param3.getZ() + var3);
            BlockState var6 = Blocks.SEA_PICKLE.defaultBlockState().setValue(SeaPickleBlock.PICKLES, Integer.valueOf(param2.nextInt(4) + 1));
            if (param0.getBlockState(var5).getBlock() == Blocks.WATER && var6.canSurvive(param0, var5)) {
                param0.setBlock(var5, var6, 2);
                ++var0;
            }
        }

        return var0 > 0;
    }
}
