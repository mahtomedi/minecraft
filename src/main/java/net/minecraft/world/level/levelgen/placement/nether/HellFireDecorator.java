package net.minecraft.world.level.levelgen.placement.nether;

import com.google.common.collect.Lists;
import com.mojang.datafixers.Dynamic;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.placement.DecoratorFrequency;
import net.minecraft.world.level.levelgen.placement.SimpleFeatureDecorator;

public class HellFireDecorator extends SimpleFeatureDecorator<DecoratorFrequency> {
    public HellFireDecorator(Function<Dynamic<?>, ? extends DecoratorFrequency> param0) {
        super(param0);
    }

    public Stream<BlockPos> place(Random param0, DecoratorFrequency param1, BlockPos param2) {
        List<BlockPos> var0 = Lists.newArrayList();

        for(int var1 = 0; var1 < param0.nextInt(param0.nextInt(param1.count) + 1) + 1; ++var1) {
            int var2 = param0.nextInt(16);
            int var3 = param0.nextInt(120) + 4;
            int var4 = param0.nextInt(16);
            var0.add(param2.offset(var2, var3, var4));
        }

        return var0.stream();
    }
}
