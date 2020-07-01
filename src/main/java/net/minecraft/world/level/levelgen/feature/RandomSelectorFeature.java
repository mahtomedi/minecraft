package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.RandomFeatureConfiguration;

public class RandomSelectorFeature extends Feature<RandomFeatureConfiguration> {
    public RandomSelectorFeature(Codec<RandomFeatureConfiguration> param0) {
        super(param0);
    }

    public boolean place(WorldGenLevel param0, ChunkGenerator param1, Random param2, BlockPos param3, RandomFeatureConfiguration param4) {
        for(WeightedConfiguredFeature<?> var0 : param4.features) {
            if (param2.nextFloat() < var0.chance) {
                return var0.place(param0, param1, param2, param3);
            }
        }

        return param4.defaultFeature.place(param0, param1, param2, param3);
    }
}
