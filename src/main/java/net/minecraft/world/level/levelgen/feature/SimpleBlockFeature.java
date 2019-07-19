package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;

public class SimpleBlockFeature extends Feature<SimpleBlockConfiguration> {
    public SimpleBlockFeature(Function<Dynamic<?>, ? extends SimpleBlockConfiguration> param0) {
        super(param0);
    }

    public boolean place(
        LevelAccessor param0, ChunkGenerator<? extends ChunkGeneratorSettings> param1, Random param2, BlockPos param3, SimpleBlockConfiguration param4
    ) {
        if (param4.placeOn.contains(param0.getBlockState(param3.below()))
            && param4.placeIn.contains(param0.getBlockState(param3))
            && param4.placeUnder.contains(param0.getBlockState(param3.above()))) {
            param0.setBlock(param3, param4.toPlace, 2);
            return true;
        } else {
            return false;
        }
    }
}
