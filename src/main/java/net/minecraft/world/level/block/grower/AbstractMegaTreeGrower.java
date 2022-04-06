package net.minecraft.world.level.block.grower;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public abstract class AbstractMegaTreeGrower extends AbstractTreeGrower {
    @Override
    public boolean growTree(ServerLevel param0, ChunkGenerator param1, BlockPos param2, BlockState param3, RandomSource param4) {
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
    protected abstract Holder<? extends ConfiguredFeature<?, ?>> getConfiguredMegaFeature(RandomSource var1);

    public boolean placeMega(ServerLevel param0, ChunkGenerator param1, BlockPos param2, BlockState param3, RandomSource param4, int param5, int param6) {
        Holder<? extends ConfiguredFeature<?, ?>> var0 = this.getConfiguredMegaFeature(param4);
        if (var0 == null) {
            return false;
        } else {
            ConfiguredFeature<?, ?> var1 = var0.value();
            BlockState var2 = Blocks.AIR.defaultBlockState();
            param0.setBlock(param2.offset(param5, 0, param6), var2, 4);
            param0.setBlock(param2.offset(param5 + 1, 0, param6), var2, 4);
            param0.setBlock(param2.offset(param5, 0, param6 + 1), var2, 4);
            param0.setBlock(param2.offset(param5 + 1, 0, param6 + 1), var2, 4);
            if (var1.place(param0, param1, param4, param2.offset(param5, 0, param6))) {
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
        return param1.getBlockState(param2.offset(param3, 0, param4)).is(var0)
            && param1.getBlockState(param2.offset(param3 + 1, 0, param4)).is(var0)
            && param1.getBlockState(param2.offset(param3, 0, param4 + 1)).is(var0)
            && param1.getBlockState(param2.offset(param3 + 1, 0, param4 + 1)).is(var0);
    }
}
