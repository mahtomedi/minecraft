package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;

public class RandomSelectorFeature extends Feature<RandomFeatureConfig> {
    public RandomSelectorFeature(Function<Dynamic<?>, ? extends RandomFeatureConfig> param0) {
        super(param0);
    }

    public boolean place(
        LevelAccessor param0, ChunkGenerator<? extends ChunkGeneratorSettings> param1, Random param2, BlockPos param3, RandomFeatureConfig param4
    ) {
        for(WeightedConfiguredFeature<?> var0 : param4.features) {
            if (param2.nextFloat() < var0.chance) {
                return var0.place(param0, param1, param2, param3);
            }
        }

        return param4.defaultFeature.place(param0, param1, param2, param3);
    }
}
