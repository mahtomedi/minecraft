package net.minecraft.world.level.levelgen.placement.nether;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.CountRangeDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.SimpleFeatureDecorator;

public class RandomCountRangeDecorator extends SimpleFeatureDecorator<CountRangeDecoratorConfiguration> {
    public RandomCountRangeDecorator(
        Function<Dynamic<?>, ? extends CountRangeDecoratorConfiguration> param0, Function<Random, ? extends CountRangeDecoratorConfiguration> param1
    ) {
        super(param0, param1);
    }

    public Stream<BlockPos> place(Random param0, CountRangeDecoratorConfiguration param1, BlockPos param2) {
        int var0 = param0.nextInt(Math.max(param1.count, 1));
        return IntStream.range(0, var0).mapToObj(param3 -> {
            int var0x = param0.nextInt(16) + param2.getX();
            int var1x = param0.nextInt(16) + param2.getZ();
            int var2x = param0.nextInt(param1.maximum - param1.topOffset) + param1.bottomOffset;
            return new BlockPos(var0x, var2x, var1x);
        });
    }
}
