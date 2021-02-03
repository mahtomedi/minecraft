package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratedFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.DecorationContext;
import org.apache.commons.lang3.mutable.MutableBoolean;

public class DecoratedFeature extends Feature<DecoratedFeatureConfiguration> {
    public DecoratedFeature(Codec<DecoratedFeatureConfiguration> param0) {
        super(param0);
    }

    @Override
    public boolean place(FeaturePlaceContext<DecoratedFeatureConfiguration> param0) {
        MutableBoolean var0 = new MutableBoolean();
        WorldGenLevel var1 = param0.level();
        DecoratedFeatureConfiguration var2 = param0.config();
        ChunkGenerator var3 = param0.chunkGenerator();
        Random var4 = param0.random();
        BlockPos var5 = param0.origin();
        ConfiguredFeature<?, ?> var6 = var2.feature.get();
        var2.decorator.getPositions(new DecorationContext(var1, var3), var4, var5).forEach(param5 -> {
            if (var6.place(var1, var3, var4, param5)) {
                var0.setTrue();
            }

        });
        return var0.isTrue();
    }

    @Override
    public String toString() {
        return String.format("< %s [%s] >", this.getClass().getSimpleName(), Registry.FEATURE.getKey(this));
    }
}
