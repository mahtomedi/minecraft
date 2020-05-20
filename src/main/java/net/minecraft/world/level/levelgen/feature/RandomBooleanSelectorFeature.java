package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.RandomBooleanFeatureConfiguration;

public class RandomBooleanSelectorFeature extends Feature<RandomBooleanFeatureConfiguration> {
    public RandomBooleanSelectorFeature(Codec<RandomBooleanFeatureConfiguration> param0) {
        super(param0);
    }

    public boolean place(
        WorldGenLevel param0, StructureFeatureManager param1, ChunkGenerator param2, Random param3, BlockPos param4, RandomBooleanFeatureConfiguration param5
    ) {
        boolean var0 = param3.nextBoolean();
        return var0 ? param5.featureTrue.place(param0, param1, param2, param3, param4) : param5.featureFalse.place(param0, param1, param2, param3, param4);
    }
}
