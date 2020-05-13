package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;

public class SimpleBlockFeature extends Feature<SimpleBlockConfiguration> {
    public SimpleBlockFeature(Function<Dynamic<?>, ? extends SimpleBlockConfiguration> param0) {
        super(param0);
    }

    public boolean place(
        WorldGenLevel param0, StructureFeatureManager param1, ChunkGenerator param2, Random param3, BlockPos param4, SimpleBlockConfiguration param5
    ) {
        if (param5.placeOn.contains(param0.getBlockState(param4.below()))
            && param5.placeIn.contains(param0.getBlockState(param4))
            && param5.placeUnder.contains(param0.getBlockState(param4.above()))) {
            param0.setBlock(param4, param5.toPlace, 2);
            return true;
        } else {
            return false;
        }
    }
}
