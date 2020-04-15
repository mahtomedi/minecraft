package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.function.Function;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public abstract class RandomScatteredFeature<C extends FeatureConfiguration> extends StructureFeature<C> {
    public RandomScatteredFeature(Function<Dynamic<?>, ? extends C> param0) {
        super(param0);
    }

    @Override
    protected int getSpacing(DimensionType param0, ChunkGeneratorSettings param1) {
        return param1.getTemplesSpacing();
    }

    @Override
    protected int getSeparation(DimensionType param0, ChunkGeneratorSettings param1) {
        return param1.getTemplesSeparation();
    }

    @Override
    protected abstract int getRandomSalt(ChunkGeneratorSettings var1);
}
