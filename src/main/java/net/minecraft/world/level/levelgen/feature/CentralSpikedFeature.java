package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;

public class CentralSpikedFeature extends Feature<NoneFeatureConfiguration> {
    protected final BlockState blockState;

    public CentralSpikedFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> param0, BlockState param1) {
        super(param0);
        this.blockState = param1;
    }

    public boolean place(
        LevelAccessor param0, ChunkGenerator<? extends ChunkGeneratorSettings> param1, Random param2, BlockPos param3, NoneFeatureConfiguration param4
    ) {
        int var0 = 0;

        for(int var1 = 0; var1 < 64; ++var1) {
            BlockPos var2 = param3.offset(param2.nextInt(8) - param2.nextInt(8), param2.nextInt(4) - param2.nextInt(4), param2.nextInt(8) - param2.nextInt(8));
            if (param0.isEmptyBlock(var2) && param0.getBlockState(var2.below()).getBlock() == Blocks.GRASS_BLOCK) {
                param0.setBlock(var2, this.blockState, 2);
                ++var0;
            }
        }

        return var0 > 0;
    }
}
