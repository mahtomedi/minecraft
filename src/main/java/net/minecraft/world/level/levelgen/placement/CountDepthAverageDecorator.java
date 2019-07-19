package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;

public class CountDepthAverageDecorator extends SimpleFeatureDecorator<DepthAverageConfigation> {
    public CountDepthAverageDecorator(Function<Dynamic<?>, ? extends DepthAverageConfigation> param0) {
        super(param0);
    }

    public Stream<BlockPos> place(Random param0, DepthAverageConfigation param1, BlockPos param2) {
        int var0 = param1.count;
        int var1 = param1.baseline;
        int var2 = param1.spread;
        return IntStream.range(0, var0).mapToObj(param4 -> {
            int var0x = param0.nextInt(16);
            int var1x = param0.nextInt(var2) + param0.nextInt(var2) - var2 + var1;
            int var2x = param0.nextInt(16);
            return param2.offset(var0x, var1x, var2x);
        });
    }
}
