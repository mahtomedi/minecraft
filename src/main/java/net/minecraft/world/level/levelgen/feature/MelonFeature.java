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

public class MelonFeature extends Feature<NoneFeatureConfiguration> {
    public MelonFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> param0) {
        super(param0);
    }

    public boolean place(
        LevelAccessor param0, ChunkGenerator<? extends ChunkGeneratorSettings> param1, Random param2, BlockPos param3, NoneFeatureConfiguration param4
    ) {
        for(int var0 = 0; var0 < 64; ++var0) {
            BlockPos var1 = param3.offset(param2.nextInt(8) - param2.nextInt(8), param2.nextInt(4) - param2.nextInt(4), param2.nextInt(8) - param2.nextInt(8));
            BlockState var2 = Blocks.MELON.defaultBlockState();
            if (param0.getBlockState(var1).getMaterial().isReplaceable() && param0.getBlockState(var1.below()).getBlock() == Blocks.GRASS_BLOCK) {
                param0.setBlock(var1, var2, 2);
            }
        }

        return true;
    }
}
