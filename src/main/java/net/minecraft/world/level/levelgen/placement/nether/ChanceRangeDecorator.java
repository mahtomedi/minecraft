package net.minecraft.world.level.levelgen.placement.nether;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.ChanceRangeDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.SimpleFeatureDecorator;

public class ChanceRangeDecorator extends SimpleFeatureDecorator<ChanceRangeDecoratorConfiguration> {
    public ChanceRangeDecorator(
        Function<Dynamic<?>, ? extends ChanceRangeDecoratorConfiguration> param0, Function<Random, ? extends ChanceRangeDecoratorConfiguration> param1
    ) {
        super(param0, param1);
    }

    public Stream<BlockPos> place(Random param0, ChanceRangeDecoratorConfiguration param1, BlockPos param2) {
        if (param0.nextFloat() < param1.chance) {
            int var0 = param0.nextInt(16) + param2.getX();
            int var1 = param0.nextInt(16) + param2.getZ();
            int var2 = param0.nextInt(param1.top - param1.topOffset) + param1.bottomOffset;
            return Stream.of(new BlockPos(var0, var2, var1));
        } else {
            return Stream.empty();
        }
    }
}
