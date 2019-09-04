package net.minecraft.world.level.block.grower;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.AbstractTreeFeature;
import net.minecraft.world.level.levelgen.feature.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.NoneFeatureConfiguration;

public abstract class AbstractTreeGrower {
    @Nullable
    protected abstract AbstractTreeFeature<NoneFeatureConfiguration> getFeature(Random var1);

    public boolean growTree(LevelAccessor param0, ChunkGenerator<?> param1, BlockPos param2, BlockState param3, Random param4) {
        AbstractTreeFeature<NoneFeatureConfiguration> var0 = this.getFeature(param4);
        if (var0 == null) {
            return false;
        } else {
            param0.setBlock(param2, Blocks.AIR.defaultBlockState(), 4);
            if (var0.place(param0, param1, param4, param2, FeatureConfiguration.NONE, false)) {
                return true;
            } else {
                param0.setBlock(param2, param3, 4);
                return false;
            }
        }
    }
}
