package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;

public class SimpleBlockFeature extends Feature<SimpleBlockConfiguration> {
    public SimpleBlockFeature(Codec<SimpleBlockConfiguration> param0) {
        super(param0);
    }

    public boolean place(WorldGenLevel param0, ChunkGenerator param1, Random param2, BlockPos param3, SimpleBlockConfiguration param4) {
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
