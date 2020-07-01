package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class NoOpFeature extends Feature<NoneFeatureConfiguration> {
    public NoOpFeature(Codec<NoneFeatureConfiguration> param0) {
        super(param0);
    }

    public boolean place(WorldGenLevel param0, ChunkGenerator param1, Random param2, BlockPos param3, NoneFeatureConfiguration param4) {
        return true;
    }
}
