package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChorusFlowerBlock;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;

public class ChorusPlantFeature extends Feature<NoneFeatureConfiguration> {
    public ChorusPlantFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> param0) {
        super(param0);
    }

    public boolean place(
        LevelAccessor param0, ChunkGenerator<? extends ChunkGeneratorSettings> param1, Random param2, BlockPos param3, NoneFeatureConfiguration param4
    ) {
        if (param0.isEmptyBlock(param3.above()) && param0.getBlockState(param3).getBlock() == Blocks.END_STONE) {
            ChorusFlowerBlock.generatePlant(param0, param3.above(), param2, 8);
            return true;
        } else {
            return false;
        }
    }
}
