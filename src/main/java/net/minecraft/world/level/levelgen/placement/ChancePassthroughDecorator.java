package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;

public class ChancePassthroughDecorator extends SimpleFeatureDecorator<ChanceDecoratorConfiguration> {
    public ChancePassthroughDecorator(
        Function<Dynamic<?>, ? extends ChanceDecoratorConfiguration> param0, Function<Random, ? extends ChanceDecoratorConfiguration> param1
    ) {
        super(param0, param1);
    }

    public Stream<BlockPos> place(Random param0, ChanceDecoratorConfiguration param1, BlockPos param2) {
        return param0.nextFloat() < 1.0F / (float)param1.chance ? Stream.of(param2) : Stream.empty();
    }
}
