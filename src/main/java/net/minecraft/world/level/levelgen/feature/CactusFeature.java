package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;

public class CactusFeature extends Feature<NoneFeatureConfiguration> {
    public CactusFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> param0) {
        super(param0);
    }

    public boolean place(
        LevelAccessor param0, ChunkGenerator<? extends ChunkGeneratorSettings> param1, Random param2, BlockPos param3, NoneFeatureConfiguration param4
    ) {
        for(int var0 = 0; var0 < 10; ++var0) {
            BlockPos var1 = param3.offset(param2.nextInt(8) - param2.nextInt(8), param2.nextInt(4) - param2.nextInt(4), param2.nextInt(8) - param2.nextInt(8));
            if (param0.isEmptyBlock(var1)) {
                int var2 = 1 + param2.nextInt(param2.nextInt(3) + 1);

                for(int var3 = 0; var3 < var2; ++var3) {
                    if (Blocks.CACTUS.defaultBlockState().canSurvive(param0, var1)) {
                        param0.setBlock(var1.above(var3), Blocks.CACTUS.defaultBlockState(), 2);
                    }
                }
            }
        }

        return true;
    }
}
