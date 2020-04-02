package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.configurations.RandomFeatureConfiguration;

public class RandomSelectorFeature extends Feature<RandomFeatureConfiguration> {
    public RandomSelectorFeature(Function<Dynamic<?>, ? extends RandomFeatureConfiguration> param0) {
        super(param0);
    }

    public boolean place(
        LevelAccessor param0,
        StructureFeatureManager param1,
        ChunkGenerator<? extends ChunkGeneratorSettings> param2,
        Random param3,
        BlockPos param4,
        RandomFeatureConfiguration param5
    ) {
        for(WeightedConfiguredFeature<?> var0 : param5.features) {
            if (param3.nextFloat() < var0.chance) {
                return var0.place(param0, param1, param2, param3, param4);
            }
        }

        return param5.defaultFeature.place(param0, param1, param2, param3, param4);
    }
}
