package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleRandomFeatureConfiguration;

public class SimpleRandomSelectorFeature extends Feature<SimpleRandomFeatureConfiguration> {
    public SimpleRandomSelectorFeature(Codec<SimpleRandomFeatureConfiguration> param0) {
        super(param0);
    }

    public boolean place(
        WorldGenLevel param0, StructureFeatureManager param1, ChunkGenerator param2, Random param3, BlockPos param4, SimpleRandomFeatureConfiguration param5
    ) {
        int var0 = param3.nextInt(param5.features.size());
        ConfiguredFeature<?, ?> var1 = param5.features.get(var0);
        return var1.place(param0, param1, param2, param3, param4);
    }
}
