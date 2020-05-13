package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.CountFeatureConfiguration;

public class SeaPickleFeature extends Feature<CountFeatureConfiguration> {
    public SeaPickleFeature(Function<Dynamic<?>, ? extends CountFeatureConfiguration> param0) {
        super(param0);
    }

    public boolean place(
        WorldGenLevel param0, StructureFeatureManager param1, ChunkGenerator param2, Random param3, BlockPos param4, CountFeatureConfiguration param5
    ) {
        int var0 = 0;

        for(int var1 = 0; var1 < param5.count; ++var1) {
            int var2 = param3.nextInt(8) - param3.nextInt(8);
            int var3 = param3.nextInt(8) - param3.nextInt(8);
            int var4 = param0.getHeight(Heightmap.Types.OCEAN_FLOOR, param4.getX() + var2, param4.getZ() + var3);
            BlockPos var5 = new BlockPos(param4.getX() + var2, var4, param4.getZ() + var3);
            BlockState var6 = Blocks.SEA_PICKLE.defaultBlockState().setValue(SeaPickleBlock.PICKLES, Integer.valueOf(param3.nextInt(4) + 1));
            if (param0.getBlockState(var5).is(Blocks.WATER) && var6.canSurvive(param0, var5)) {
                param0.setBlock(var5, var6, 2);
                ++var0;
            }
        }

        return var0 > 0;
    }
}
