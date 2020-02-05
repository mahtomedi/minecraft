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
    public NetherForestVegetationFeature(Function<Dynamic<?>, ? extends BlockPileConfiguration> param0) {
        super(param0);
    }

    public boolean place(
        LevelAccessor param0, ChunkGenerator<? extends ChunkGeneratorSettings> param1, Random param2, BlockPos param3, BlockPileConfiguration param4
    ) {
        for(Block var0 = param0.getBlockState(param3.below()).getBlock();
            !var0.is(BlockTags.NYLIUM) && param3.getY() > 0;
            var0 = param0.getBlockState(param3).getBlock()
        ) {
            param3 = param3.below();
        }

        int var1 = param3.getY();
        if (var1 >= 1 && var1 + 1 < 256) {
            int var2 = 0;

            for(int var3 = 0; var3 < 64; ++var3) {
                BlockPos var4 = param3.offset(
                    param2.nextInt(8) - param2.nextInt(8), param2.nextInt(4) - param2.nextInt(4), param2.nextInt(8) - param2.nextInt(8)
                );
                BlockState var5 = param4.stateProvider.getState(param2, var4);
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
