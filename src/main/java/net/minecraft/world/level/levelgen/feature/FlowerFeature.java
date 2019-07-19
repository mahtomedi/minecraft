package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;

public abstract class FlowerFeature extends Feature<NoneFeatureConfiguration> {
    public FlowerFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> param0) {
        super(param0, false);
    }

    public boolean place(
        LevelAccessor param0, ChunkGenerator<? extends ChunkGeneratorSettings> param1, Random param2, BlockPos param3, NoneFeatureConfiguration param4
    ) {
        BlockState var0 = this.getRandomFlower(param2, param3);
        int var1 = 0;

        for(int var2 = 0; var2 < 64; ++var2) {
            BlockPos var3 = param3.offset(param2.nextInt(8) - param2.nextInt(8), param2.nextInt(4) - param2.nextInt(4), param2.nextInt(8) - param2.nextInt(8));
            if (param0.isEmptyBlock(var3) && var3.getY() < 255 && var0.canSurvive(param0, var3)) {
                param0.setBlock(var3, var0, 2);
                ++var1;
            }
        }

        return var1 > 0;
    }

    public abstract BlockState getRandomFlower(Random var1, BlockPos var2);
}
