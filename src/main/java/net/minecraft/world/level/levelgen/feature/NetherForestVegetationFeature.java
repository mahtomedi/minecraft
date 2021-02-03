package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.BlockPileConfiguration;

public class NetherForestVegetationFeature extends Feature<BlockPileConfiguration> {
    public NetherForestVegetationFeature(Codec<BlockPileConfiguration> param0) {
        super(param0);
    }

    @Override
    public boolean place(FeaturePlaceContext<BlockPileConfiguration> param0) {
        return place(param0.level(), param0.random(), param0.origin(), param0.config(), 8, 4);
    }

    public static boolean place(LevelAccessor param0, Random param1, BlockPos param2, BlockPileConfiguration param3, int param4, int param5) {
        BlockState var0 = param0.getBlockState(param2.below());
        if (!var0.is(BlockTags.NYLIUM)) {
            return false;
        } else {
            int var1 = param2.getY();
            if (var1 >= param0.getMinBuildHeight() + 1 && var1 + 1 < param0.getMaxBuildHeight()) {
                int var2 = 0;

                for(int var3 = 0; var3 < param4 * param4; ++var3) {
                    BlockPos var4 = param2.offset(
                        param1.nextInt(param4) - param1.nextInt(param4),
                        param1.nextInt(param5) - param1.nextInt(param5),
                        param1.nextInt(param4) - param1.nextInt(param4)
                    );
                    BlockState var5 = param3.stateProvider.getState(param1, var4);
                    if (param0.isEmptyBlock(var4) && var4.getY() > param0.getMinBuildHeight() && var5.canSurvive(param0, var4)) {
                        param0.setBlock(var4, var5, 2);
                        ++var2;
                    }
                }

                return var2 > 0;
            } else {
                return false;
            }
        }
    }
}
