package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.NoneDecoratorConfiguration;

public class NopePlacementDecorator extends SimpleFeatureDecorator<NoneDecoratorConfiguration> {
    public NopePlacementDecorator(
        Function<Dynamic<?>, ? extends NoneDecoratorConfiguration> param0, Function<Random, ? extends NoneDecoratorConfiguration> param1
    ) {
        super(param0, param1);
    }

    public Stream<BlockPos> place(Random param0, NoneDecoratorConfiguration param1, BlockPos param2) {
        return Stream.of(param2);
    }
}
