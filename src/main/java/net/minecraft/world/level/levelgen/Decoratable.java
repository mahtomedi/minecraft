package net.minecraft.world.level.levelgen;

import net.minecraft.util.UniformInt;
import net.minecraft.world.level.levelgen.feature.configurations.CountConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneDecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RangeDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.ChanceDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.ConfiguredDecorator;
import net.minecraft.world.level.levelgen.placement.DepthAverageConfiguration;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;

public interface Decoratable<R> {
    R decorated(ConfiguredDecorator<?> var1);

    default R rarity(int param0) {
        return this.decorated(FeatureDecorator.CHANCE.configured(new ChanceDecoratorConfiguration(param0)));
    }

    default R count(UniformInt param0) {
        return this.decorated(FeatureDecorator.COUNT.configured(new CountConfiguration(param0)));
    }

    default R count(int param0) {
        return this.count(UniformInt.fixed(param0));
    }

    default R countRandom(int param0) {
        return this.count(UniformInt.of(0, param0));
    }

    default R range(VerticalAnchor param0, VerticalAnchor param1) {
        return this.range(new RangeDecoratorConfiguration(param0, param1));
    }

    default R range(RangeDecoratorConfiguration param0) {
        return this.decorated(FeatureDecorator.RANGE.configured(param0));
    }

    default R squared() {
        return this.decorated(FeatureDecorator.SQUARE.configured(NoneDecoratorConfiguration.INSTANCE));
    }

    default R depthAverage(VerticalAnchor param0, int param1) {
        return this.decorated(FeatureDecorator.DEPTH_AVERAGE.configured(new DepthAverageConfiguration(param0, param1)));
    }
}
