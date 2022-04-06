package net.minecraft.world.level.block.grower;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public abstract class AbstractTreeGrower {
    @Nullable
    protected abstract Holder<? extends ConfiguredFeature<?, ?>> getConfiguredFeature(RandomSource var1, boolean var2);

    public boolean growTree(ServerLevel param0, ChunkGenerator param1, BlockPos param2, BlockState param3, RandomSource param4) {
        Holder<? extends ConfiguredFeature<?, ?>> var0 = this.getConfiguredFeature(param4, this.hasFlowers(param0, param2));
        if (var0 == null) {
            return false;
        } else {
            ConfiguredFeature<?, ?> var1 = var0.value();
            param0.setBlock(param2, Blocks.AIR.defaultBlockState(), 4);
            if (var1.place(param0, param1, param4, param2)) {
                return true;
            } else {
                param0.setBlock(param2, param3, 4);
                return false;
            }
        }
    }

    private boolean hasFlowers(LevelAccessor param0, BlockPos param1) {
        for(BlockPos var0 : BlockPos.MutableBlockPos.betweenClosed(param1.below().north(2).west(2), param1.above().south(2).east(2))) {
            if (param0.getBlockState(var0).is(BlockTags.FLOWERS)) {
                return true;
            }
        }

        return false;
    }
}
