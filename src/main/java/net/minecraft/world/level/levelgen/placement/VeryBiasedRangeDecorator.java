package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.RangeDecoratorConfiguration;

public class VeryBiasedRangeDecorator extends SimpleFeatureDecorator<RangeDecoratorConfiguration> {
    public VeryBiasedRangeDecorator(Codec<RangeDecoratorConfiguration> param0) {
        super(param0);
    }

    public Stream<BlockPos> place(Random param0, RangeDecoratorConfiguration param1, BlockPos param2) {
        int var0 = param2.getX();
        int var1 = param2.getZ();
        int var2 = param0.nextInt(param0.nextInt(param0.nextInt(param1.maximum - param1.topOffset) + param1.bottomOffset) + param1.bottomOffset);
        return Stream.of(new BlockPos(var0, var2, var1));
    }
}
