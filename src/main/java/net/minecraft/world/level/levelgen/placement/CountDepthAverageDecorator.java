package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;

public class CountDepthAverageDecorator extends SimpleFeatureDecorator<DepthAverageConfigation> {
    public CountDepthAverageDecorator(
        Function<Dynamic<?>, ? extends DepthAverageConfigation> param0, Function<Random, ? extends DepthAverageConfigation> param1
    ) {
        super(param0, param1);
    }

    public Stream<BlockPos> place(Random param0, DepthAverageConfigation param1, BlockPos param2) {
        int var0 = param1.count;
        int var1 = param1.baseline;
        int var2 = param1.spread;
        return IntStream.range(0, var0).mapToObj(param4 -> {
            int var0x = param0.nextInt(16) + param2.getX();
            int var1x = param0.nextInt(16) + param2.getZ();
            int var2x = param0.nextInt(var2) + param0.nextInt(var2) - var2 + var1;
            return new BlockPos(var0x, var2x, var1x);
        });
    }
}
