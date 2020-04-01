package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratedFeatureConfiguration;

public class DecoratedFeature extends Feature<DecoratedFeatureConfiguration> {
    public DecoratedFeature(
        Function<Dynamic<?>, ? extends DecoratedFeatureConfiguration> param0, Function<Random, ? extends DecoratedFeatureConfiguration> param1
    ) {
        super(param0, param1);
    }

    public boolean place(
        LevelAccessor param0, ChunkGenerator<? extends ChunkGeneratorSettings> param1, Random param2, BlockPos param3, DecoratedFeatureConfiguration param4
    ) {
        return param4.decorator.place(param0, param1, param2, param3, param4.feature);
    }

    @Override
    public String toString() {
        return String.format("< %s [%s] >", this.getClass().getSimpleName(), Registry.FEATURE.getKey(this));
    }
}
