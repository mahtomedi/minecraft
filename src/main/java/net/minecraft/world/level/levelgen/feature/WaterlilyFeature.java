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

public class WaterlilyFeature extends Feature<NoneFeatureConfiguration> {
    public WaterlilyFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> param0) {
        super(param0);
    }

    public boolean place(
        LevelAccessor param0, ChunkGenerator<? extends ChunkGeneratorSettings> param1, Random param2, BlockPos param3, NoneFeatureConfiguration param4
    ) {
        BlockPos var1;
        for(BlockPos var0 = param3; var0.getY() > 0; var0 = var1) {
            var1 = var0.below();
            if (!param0.isEmptyBlock(var1)) {
                break;
            }
        }

        for(int var2 = 0; var2 < 10; ++var2) {
            BlockPos var3 = param3.offset(param2.nextInt(8) - param2.nextInt(8), param2.nextInt(4) - param2.nextInt(4), param2.nextInt(8) - param2.nextInt(8));
            BlockState var4 = Blocks.LILY_PAD.defaultBlockState();
            if (param0.isEmptyBlock(var3) && var4.canSurvive(param0, var3)) {
                param0.setBlock(var3, var4, 2);
            }
        }

        return true;
    }
}
