package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.NoneDecoratorConfiguration;

public class NopePlacementDecorator extends SimpleFeatureDecorator<NoneDecoratorConfiguration> {
    public NopePlacementDecorator(Function<Dynamic<?>, ? extends NoneDecoratorConfiguration> param0) {
        super(param0);
    }

    public Stream<BlockPos> place(Random param0, NoneDecoratorConfiguration param1, BlockPos param2) {
        return Stream.of(param2);
    }
}
