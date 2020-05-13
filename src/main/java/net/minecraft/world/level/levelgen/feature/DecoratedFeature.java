package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratedFeatureConfiguration;

public class DecoratedFeature extends Feature<DecoratedFeatureConfiguration> {
    public DecoratedFeature(Function<Dynamic<?>, ? extends DecoratedFeatureConfiguration> param0) {
        super(param0);
    }

    public boolean place(
        WorldGenLevel param0, StructureFeatureManager param1, ChunkGenerator param2, Random param3, BlockPos param4, DecoratedFeatureConfiguration param5
    ) {
        return param5.decorator.place(param0, param1, param2, param3, param4, param5.feature);
    }

    @Override
    public String toString() {
        return String.format("< %s [%s] >", this.getClass().getSimpleName(), Registry.FEATURE.getKey(this));
    }
}
