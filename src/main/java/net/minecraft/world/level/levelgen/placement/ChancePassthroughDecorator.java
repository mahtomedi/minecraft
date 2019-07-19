package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;

public class ChancePassthroughDecorator extends SimpleFeatureDecorator<DecoratorChance> {
    public ChancePassthroughDecorator(Function<Dynamic<?>, ? extends DecoratorChance> param0) {
        super(param0);
    }

    public Stream<BlockPos> place(Random param0, DecoratorChance param1, BlockPos param2) {
        return param0.nextFloat() < 1.0F / (float)param1.chance ? Stream.of(param2) : Stream.empty();
    }
}
