package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.CountConfiguration;

public class SeaPickleFeature extends Feature<CountConfiguration> {
    public SeaPickleFeature(Codec<CountConfiguration> param0) {
        super(param0);
    }

    public boolean place(WorldGenLevel param0, ChunkGenerator param1, Random param2, BlockPos param3, CountConfiguration param4) {
        int var0 = 0;
        int var1 = param4.count().sample(param2);

        for(int var2 = 0; var2 < var1; ++var2) {
            int var3 = param2.nextInt(8) - param2.nextInt(8);
            int var4 = param2.nextInt(8) - param2.nextInt(8);
            int var5 = param0.getHeight(Heightmap.Types.OCEAN_FLOOR, param3.getX() + var3, param3.getZ() + var4);
            BlockPos var6 = new BlockPos(param3.getX() + var3, var5, param3.getZ() + var4);
            BlockState var7 = Blocks.SEA_PICKLE.defaultBlockState().setValue(SeaPickleBlock.PICKLES, Integer.valueOf(param2.nextInt(4) + 1));
            if (param0.getBlockState(var6).is(Blocks.WATER) && var7.canSurvive(param0, var6)) {
                param0.setBlock(var6, var7, 2);
                ++var0;
            }
        }

        return var0 > 0;
    }
}
