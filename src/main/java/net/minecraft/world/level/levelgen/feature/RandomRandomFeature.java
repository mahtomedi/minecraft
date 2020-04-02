package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.configurations.RandomRandomFeatureConfiguration;

public class RandomRandomFeature extends Feature<RandomRandomFeatureConfiguration> {
    public RandomRandomFeature(Function<Dynamic<?>, ? extends RandomRandomFeatureConfiguration> param0) {
        super(param0);
    }

    public boolean place(
        LevelAccessor param0,
        StructureFeatureManager param1,
        ChunkGenerator<? extends ChunkGeneratorSettings> param2,
        Random param3,
        BlockPos param4,
        RandomRandomFeatureConfiguration param5
    ) {
        int var0 = param3.nextInt(5) - 3 + param5.count;

        for(int var1 = 0; var1 < var0; ++var1) {
            int var2 = param3.nextInt(param5.features.size());
            ConfiguredFeature<?, ?> var3 = param5.features.get(var2);
            var3.place(param0, param1, param2, param3, param4);
        }

        return true;
    }
}
