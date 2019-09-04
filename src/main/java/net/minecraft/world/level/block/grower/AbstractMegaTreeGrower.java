package net.minecraft.world.level.block.grower;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.AbstractTreeFeature;
import net.minecraft.world.level.levelgen.feature.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.NoneFeatureConfiguration;

public abstract class AbstractMegaTreeGrower extends AbstractTreeGrower {
    @Override
    public boolean growTree(LevelAccessor param0, ChunkGenerator<?> param1, BlockPos param2, BlockState param3, Random param4) {
        for(int var0 = 0; var0 >= -1; --var0) {
            for(int var1 = 0; var1 >= -1; --var1) {
                if (isTwoByTwoSapling(param3, param0, param2, var0, var1)) {
                    return this.placeMega(param0, param1, param2, param3, param4, var0, var1);
                }
            }
        }

        return super.growTree(param0, param1, param2, param3, param4);
    }

    @Nullable
    protected abstract AbstractTreeFeature<NoneFeatureConfiguration> getMegaFeature(Random var1);

    public boolean placeMega(LevelAccessor param0, ChunkGenerator<?> param1, BlockPos param2, BlockState param3, Random param4, int param5, int param6) {
        AbstractTreeFeature<NoneFeatureConfiguration> var0 = this.getMegaFeature(param4);
        if (var0 == null) {
            return false;
        } else {
            BlockState var1 = Blocks.AIR.defaultBlockState();
            param0.setBlock(param2.offset(param5, 0, param6), var1, 4);
            param0.setBlock(param2.offset(param5 + 1, 0, param6), var1, 4);
            param0.setBlock(param2.offset(param5, 0, param6 + 1), var1, 4);
            param0.setBlock(param2.offset(param5 + 1, 0, param6 + 1), var1, 4);
            if (var0.place(param0, param1, param4, param2.offset(param5, 0, param6), FeatureConfiguration.NONE, false)) {
                return true;
            } else {
                param0.setBlock(param2.offset(param5, 0, param6), param3, 4);
                param0.setBlock(param2.offset(param5 + 1, 0, param6), param3, 4);
                param0.setBlock(param2.offset(param5, 0, param6 + 1), param3, 4);
                param0.setBlock(param2.offset(param5 + 1, 0, param6 + 1), param3, 4);
                return false;
            }
        }
    }

    public static boolean isTwoByTwoSapling(BlockState param0, BlockGetter param1, BlockPos param2, int param3, int param4) {
        Block var0 = param0.getBlock();
        return var0 == param1.getBlockState(param2.offset(param3, 0, param4)).getBlock()
            && var0 == param1.getBlockState(param2.offset(param3 + 1, 0, param4)).getBlock()
            && var0 == param1.getBlockState(param2.offset(param3, 0, param4 + 1)).getBlock()
            && var0 == param1.getBlockState(param2.offset(param3 + 1, 0, param4 + 1)).getBlock();
    }
}
