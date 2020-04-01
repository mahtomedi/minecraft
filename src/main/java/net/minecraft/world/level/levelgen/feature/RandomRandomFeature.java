package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.configurations.RandomRandomFeatureConfiguration;

public class RandomRandomFeature extends Feature<RandomRandomFeatureConfiguration> {
    public RandomRandomFeature(
        Function<Dynamic<?>, ? extends RandomRandomFeatureConfiguration> param0, Function<Random, ? extends RandomRandomFeatureConfiguration> param1
    ) {
        super(param0, param1);
    }

    public boolean place(
        LevelAccessor param0, ChunkGenerator<? extends ChunkGeneratorSettings> param1, Random param2, BlockPos param3, RandomRandomFeatureConfiguration param4
    ) {
        int var0 = param2.nextInt(5) - 3 + param4.count;

        for(int var1 = 0; var1 < var0; ++var1) {
            int var2 = param2.nextInt(param4.features.size());
            ConfiguredFeature<?, ?> var3 = param4.features.get(var2);
            var3.place(param0, param1, param2, param3);
        }

        return true;
    }
}
