package net.minecraft.world.level.levelgen.carver;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;

public abstract class ProbabilityCarverBase extends WorldCarver<ProbabilityFeatureConfiguration> {
    public ProbabilityCarverBase(Function<Dynamic<?>, ? extends ProbabilityFeatureConfiguration> param0, int param1) {
        super(param0, param1);
    }

    public ProbabilityFeatureConfiguration randomConfig(Random param0) {
        return new ProbabilityFeatureConfiguration(param0.nextFloat() / 2.0F);
    }
}
