package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.NoneDecoratorConfiguration;

public class EndIslandPlacementDecorator extends SimpleFeatureDecorator<NoneDecoratorConfiguration> {
    public EndIslandPlacementDecorator(
        Function<Dynamic<?>, ? extends NoneDecoratorConfiguration> param0, Function<Random, ? extends NoneDecoratorConfiguration> param1
    ) {
        super(param0, param1);
    }

    public Stream<BlockPos> place(Random param0, NoneDecoratorConfiguration param1, BlockPos param2) {
        Stream<BlockPos> var0 = Stream.empty();
        if (param0.nextInt(14) == 0) {
            var0 = Stream.concat(var0, Stream.of(param2.offset(param0.nextInt(16), 55 + param0.nextInt(16), param0.nextInt(16))));
            if (param0.nextInt(4) == 0) {
                var0 = Stream.concat(var0, Stream.of(param2.offset(param0.nextInt(16), 55 + param0.nextInt(16), param0.nextInt(16))));
            }

            return var0;
        } else {
            return Stream.empty();
        }
    }
}
