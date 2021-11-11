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

    @Override
    public boolean place(FeaturePlaceContext<RandomFeatureConfiguration> param0) {
        RandomFeatureConfiguration var0 = param0.config();
        Random var1 = param0.random();
        WorldGenLevel var2 = param0.level();
        ChunkGenerator var3 = param0.chunkGenerator();
        BlockPos var4 = param0.origin();

        for(WeightedPlacedFeature var5 : var0.features) {
            if (var1.nextFloat() < var5.chance) {
                return var5.place(var2, var3, var1, var4);
            }
        }

        return var0.defaultFeature.get().place(var2, var3, var1, var4);
    }
}
