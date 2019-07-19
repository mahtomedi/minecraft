package net.minecraft.world.level.block.grower;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.AbstractTreeFeature;
import net.minecraft.world.level.levelgen.feature.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.NoneFeatureConfiguration;

public abstract class AbstractTreeGrower {
    @Nullable
    protected abstract AbstractTreeFeature<NoneFeatureConfiguration> getFeature(Random var1);

    public boolean growTree(LevelAccessor param0, BlockPos param1, BlockState param2, Random param3) {
        AbstractTreeFeature<NoneFeatureConfiguration> var0 = this.getFeature(param3);
        if (var0 == null) {
            return false;
        } else {
            param0.setBlock(param1, Blocks.AIR.defaultBlockState(), 4);
            if (var0.place(param0, param0.getChunkSource().getGenerator(), param3, param1, FeatureConfiguration.NONE)) {
                return true;
            } else {
                param0.setBlock(param1, param2, 4);
                return false;
            }
        }
    }
}
