package net.minecraft.world.level.block.grower;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.SmallTreeConfiguration;

public abstract class AbstractTreeGrower {
    @Nullable
    protected abstract ConfiguredFeature<SmallTreeConfiguration, ?> getConfiguredFeature(Random var1);

    public boolean growTree(LevelAccessor param0, ChunkGenerator<?> param1, BlockPos param2, BlockState param3, Random param4) {
        ConfiguredFeature<SmallTreeConfiguration, ?> var0 = this.getConfiguredFeature(param4);
        if (var0 == null) {
            return false;
        } else {
            param0.setBlock(param2, Blocks.AIR.defaultBlockState(), 4);
            var0.config.setFromSapling();
            if (var0.place(param0, param1, param4, param2)) {
                return true;
            } else {
                param0.setBlock(param2, param3, 4);
                return false;
            }
        }
    }
}
