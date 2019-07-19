package net.minecraft.world.level.levelgen.placement.nether;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.DecoratorChanceRange;
import net.minecraft.world.level.levelgen.placement.SimpleFeatureDecorator;

public class ChanceRangeDecorator extends SimpleFeatureDecorator<DecoratorChanceRange> {
    public ChanceRangeDecorator(Function<Dynamic<?>, ? extends DecoratorChanceRange> param0) {
        super(param0);
    }

    public Stream<BlockPos> place(Random param0, DecoratorChanceRange param1, BlockPos param2) {
        if (param0.nextFloat() < param1.chance) {
            int var0 = param0.nextInt(16);
            int var1 = param0.nextInt(param1.top - param1.topOffset) + param1.bottomOffset;
            int var2 = param0.nextInt(16);
            return Stream.of(param2.offset(var0, var1, var2));
        } else {
            return Stream.empty();
        }
    }
}
