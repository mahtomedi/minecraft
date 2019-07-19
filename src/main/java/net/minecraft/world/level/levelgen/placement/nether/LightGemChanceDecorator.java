package net.minecraft.world.level.levelgen.placement.nether;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.placement.DecoratorFrequency;
import net.minecraft.world.level.levelgen.placement.SimpleFeatureDecorator;

public class LightGemChanceDecorator extends SimpleFeatureDecorator<DecoratorFrequency> {
    public LightGemChanceDecorator(Function<Dynamic<?>, ? extends DecoratorFrequency> param0) {
        super(param0);
    }

    public Stream<BlockPos> place(Random param0, DecoratorFrequency param1, BlockPos param2) {
        return IntStream.range(0, param0.nextInt(param0.nextInt(param1.count) + 1)).mapToObj(param2x -> {
            int var0 = param0.nextInt(16);
            int var1x = param0.nextInt(120) + 4;
            int var2x = param0.nextInt(16);
            return param2.offset(var0, var1x, var2x);
        });
    }
}
