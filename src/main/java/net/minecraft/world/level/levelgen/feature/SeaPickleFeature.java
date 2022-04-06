package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.CountConfiguration;

public class SeaPickleFeature extends Feature<CountConfiguration> {
    public SeaPickleFeature(Codec<CountConfiguration> param0) {
        super(param0);
    }

    @Override
    public boolean place(FeaturePlaceContext<CountConfiguration> param0) {
        int var0 = 0;
        RandomSource var1 = param0.random();
        WorldGenLevel var2 = param0.level();
        BlockPos var3 = param0.origin();
        int var4 = param0.config().count().sample(var1);

        for(int var5 = 0; var5 < var4; ++var5) {
            int var6 = var1.nextInt(8) - var1.nextInt(8);
            int var7 = var1.nextInt(8) - var1.nextInt(8);
            int var8 = var2.getHeight(Heightmap.Types.OCEAN_FLOOR, var3.getX() + var6, var3.getZ() + var7);
            BlockPos var9 = new BlockPos(var3.getX() + var6, var8, var3.getZ() + var7);
            BlockState var10 = Blocks.SEA_PICKLE.defaultBlockState().setValue(SeaPickleBlock.PICKLES, Integer.valueOf(var1.nextInt(4) + 1));
            if (var2.getBlockState(var9).is(Blocks.WATER) && var10.canSurvive(var2, var9)) {
                var2.setBlock(var9, var10, 2);
                ++var0;
            }
        }

        return var0 > 0;
    }
}
