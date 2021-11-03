package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.Random;
import net.minecraft.core.Registry;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
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
        ConfiguredFeature<?, ?> var5 = var2.feature.get();
        var2.decorator.getPositions(new DecorationContext(var1, var3), var4, param0.origin()).forEach(param6 -> {
            Optional<ConfiguredFeature<?, ?>> var0x = param0.topFeature();
            if (var0x.isPresent() && !(var5.feature() instanceof DecoratedFeature)) {
                Biome var1x = var1.getBiome(param6);
                if (!var1x.getGenerationSettings().hasFeature(var0x.get())) {
                    return;
                }
            }

            if (var1.ensureCanWrite(param6) && var5.placeWithBiomeCheck(var0x, var1, var3, var4, param6)) {
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
