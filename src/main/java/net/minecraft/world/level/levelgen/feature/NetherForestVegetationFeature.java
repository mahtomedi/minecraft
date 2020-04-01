package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.configurations.BlockPileConfiguration;

public class NetherForestVegetationFeature extends Feature<BlockPileConfiguration> {
    public NetherForestVegetationFeature(
        Function<Dynamic<?>, ? extends BlockPileConfiguration> param0, Function<Random, ? extends BlockPileConfiguration> param1
    ) {
        super(param0, param1);
    }

    public boolean place(
        LevelAccessor param0, ChunkGenerator<? extends ChunkGeneratorSettings> param1, Random param2, BlockPos param3, BlockPileConfiguration param4
    ) {
        return place(param0, param2, param3, param4, 8, 4);
    }

    public static boolean place(LevelAccessor param0, Random param1, BlockPos param2, BlockPileConfiguration param3, int param4, int param5) {
        for(Block var0 = param0.getBlockState(param2.below()).getBlock();
            !var0.is(BlockTags.NYLIUM) && param2.getY() > 0;
            var0 = param0.getBlockState(param2).getBlock()
        ) {
            param2 = param2.below();
        }

        int var1 = param2.getY();
        if (var1 >= 1 && var1 + 1 < 256) {
            int var2 = 0;

            for(int var3 = 0; var3 < param4 * param4; ++var3) {
                BlockPos var4 = param2.offset(
                    param1.nextInt(param4) - param1.nextInt(param4),
                    param1.nextInt(param5) - param1.nextInt(param5),
                    param1.nextInt(param4) - param1.nextInt(param4)
                );
                BlockState var5 = param3.stateProvider.getState(param1, var4);
                if (param0.isEmptyBlock(var4) && var4.getY() > 0 && var5.canSurvive(param0, var4)) {
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
