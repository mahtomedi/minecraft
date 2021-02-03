package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleRandomFeatureConfiguration;

public class SimpleRandomSelectorFeature extends Feature<SimpleRandomFeatureConfiguration> {
    public SimpleRandomSelectorFeature(Codec<SimpleRandomFeatureConfiguration> param0) {
        super(param0);
    }

    @Override
    public boolean place(FeaturePlaceContext<SimpleRandomFeatureConfiguration> param0) {
        Random var0 = param0.random();
        SimpleRandomFeatureConfiguration var1 = param0.config();
        WorldGenLevel var2 = param0.level();
        BlockPos var3 = param0.origin();
        ChunkGenerator var4 = param0.chunkGenerator();
        int var5 = var0.nextInt(var1.features.size());
        ConfiguredFeature<?, ?> var6 = var1.features.get(var5).get();
        return var6.place(var2, var4, var0, var3);
    }
}
