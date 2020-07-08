package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;

public class DepthAverageDecorator extends SimpleFeatureDecorator<DepthAverageConfigation> {
    public DepthAverageDecorator(Codec<DepthAverageConfigation> param0) {
        super(param0);
    }

    public Stream<BlockPos> place(Random param0, DepthAverageConfigation param1, BlockPos param2) {
        int var0 = param1.baseline;
        int var1 = param1.spread;
        int var2 = param2.getX();
        int var3 = param2.getZ();
        int var4 = param0.nextInt(var1) + param0.nextInt(var1) - var1 + var0;
        return Stream.of(new BlockPos(var2, var4, var3));
    }
}
