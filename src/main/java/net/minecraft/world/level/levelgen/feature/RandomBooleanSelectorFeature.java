package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.RandomBooleanFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class RandomBooleanSelectorFeature extends Feature<RandomBooleanFeatureConfiguration> {
    public RandomBooleanSelectorFeature(Codec<RandomBooleanFeatureConfiguration> param0) {
        super(param0);
    }

    @Override
    public boolean place(FeaturePlaceContext<RandomBooleanFeatureConfiguration> param0) {
        RandomSource var0 = param0.random();
        RandomBooleanFeatureConfiguration var1 = param0.config();
        WorldGenLevel var2 = param0.level();
        ChunkGenerator var3 = param0.chunkGenerator();
        BlockPos var4 = param0.origin();
        boolean var5 = var0.nextBoolean();
        return ((PlacedFeature)(var5 ? var1.featureTrue : var1.featureFalse).value()).place(var2, var3, var0, var4);
    }
}
