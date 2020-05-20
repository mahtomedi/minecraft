package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;

public class CountDepthAverageDecorator extends SimpleFeatureDecorator<DepthAverageConfigation> {
    public CountDepthAverageDecorator(Codec<DepthAverageConfigation> param0) {
        super(param0);
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
