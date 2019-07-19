package net.minecraft.world.level.levelgen.placement.nether;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.DecoratorCountRange;
import net.minecraft.world.level.levelgen.placement.SimpleFeatureDecorator;

public class CountRangeDecorator extends SimpleFeatureDecorator<DecoratorCountRange> {
    public CountRangeDecorator(Function<Dynamic<?>, ? extends DecoratorCountRange> param0) {
        super(param0);
    }

    public Stream<BlockPos> place(Random param0, DecoratorCountRange param1, BlockPos param2) {
        return IntStream.range(0, param1.count).mapToObj(param3 -> {
            int var0 = param0.nextInt(16);
            int var1x = param0.nextInt(param1.maximum - param1.topOffset) + param1.bottomOffset;
            int var2x = param0.nextInt(16);
            return param2.offset(var0, var1x, var2x);
        });
    }
}
