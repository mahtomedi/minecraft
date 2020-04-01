package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.configurations.RandomBooleanFeatureConfiguration;

public class RandomBooleanSelectorFeature extends Feature<RandomBooleanFeatureConfiguration> {
    public RandomBooleanSelectorFeature(
        Function<Dynamic<?>, ? extends RandomBooleanFeatureConfiguration> param0, Function<Random, ? extends RandomBooleanFeatureConfiguration> param1
    ) {
        super(param0, param1);
    }

    public boolean place(
        LevelAccessor param0, ChunkGenerator<? extends ChunkGeneratorSettings> param1, Random param2, BlockPos param3, RandomBooleanFeatureConfiguration param4
    ) {
        boolean var0 = param2.nextBoolean();
        return var0 ? param4.featureTrue.place(param0, param1, param2, param3) : param4.featureFalse.place(param0, param1, param2, param3);
    }
}
