package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.RandomRandomFeatureConfiguration;

public class RandomRandomFeature extends Feature<RandomRandomFeatureConfiguration> {
    public RandomRandomFeature(Codec<RandomRandomFeatureConfiguration> param0) {
        super(param0);
    }

    public boolean place(WorldGenLevel param0, ChunkGenerator param1, Random param2, BlockPos param3, RandomRandomFeatureConfiguration param4) {
        int var0 = param2.nextInt(5) - 3 + param4.count;

        for(int var1 = 0; var1 < var0; ++var1) {
            int var2 = param2.nextInt(param4.features.size());
            ConfiguredFeature<?, ?> var3 = param4.features.get(var2);
            var3.place(param0, param1, param2, param3);
        }

        return true;
    }
}
