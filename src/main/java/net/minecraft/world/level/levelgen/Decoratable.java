package net.minecraft.world.level.levelgen;

import net.minecraft.core.BlockPos;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.configurations.CountConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneDecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RangeDecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SingleBlockStateConfiguration;
import net.minecraft.world.level.levelgen.heightproviders.TrapezoidHeight;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;
import net.minecraft.world.level.levelgen.placement.BlockFilterConfiguration;
import net.minecraft.world.level.levelgen.placement.ChanceDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.ConfiguredDecorator;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;

public interface Decoratable<R> {
    R decorated(ConfiguredDecorator<?> var1);

    default R rarity(int param0) {
        return this.decorated(FeatureDecorator.CHANCE.configured(new ChanceDecoratorConfiguration(param0)));
    }

    default R count(IntProvider param0) {
        return this.decorated(FeatureDecorator.COUNT.configured(new CountConfiguration(param0)));
    }

    default R count(int param0) {
        return this.count(ConstantInt.of(param0));
    }

    default R countRandom(int param0) {
        return this.count(UniformInt.of(0, param0));
    }

    default R rangeUniform(VerticalAnchor param0, VerticalAnchor param1) {
        return this.range(new RangeDecoratorConfiguration(UniformHeight.of(param0, param1)));
    }

    default R rangeTriangle(VerticalAnchor param0, VerticalAnchor param1) {
        return this.range(new RangeDecoratorConfiguration(TrapezoidHeight.of(param0, param1)));
    }

    default R range(RangeDecoratorConfiguration param0) {
        return this.decorated(FeatureDecorator.RANGE.configured(param0));
    }

    default R squared() {
        return this.decorated(FeatureDecorator.SQUARE.configured(NoneDecoratorConfiguration.INSTANCE));
    }

    default R filteredByBlockSurvival(Block param0) {
        return this.decorated(FeatureDecorator.BLOCK_SURVIVES_FILTER.configured(new SingleBlockStateConfiguration(param0.defaultBlockState())));
    }

    default R onlyWhenEmpty() {
        return this.decorated(FeatureDecorator.BLOCK_FILTER.configured(new BlockFilterConfiguration(BlockPredicate.matchesBlock(Blocks.AIR, BlockPos.ZERO))));
    }
}
