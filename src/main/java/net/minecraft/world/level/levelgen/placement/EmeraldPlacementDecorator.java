package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.NoneDecoratorConfiguration;

public class EmeraldPlacementDecorator extends SimpleFeatureDecorator<NoneDecoratorConfiguration> {
    public EmeraldPlacementDecorator(Function<Dynamic<?>, ? extends NoneDecoratorConfiguration> param0) {
        super(param0);
    }

    public Stream<BlockPos> place(Random param0, NoneDecoratorConfiguration param1, BlockPos param2) {
        int var0 = 3 + param0.nextInt(6);
        return IntStream.range(0, var0).mapToObj(param2x -> {
            int var0x = param0.nextInt(16);
            int var1x = param0.nextInt(28) + 4;
            int var2x = param0.nextInt(16);
            return param2.offset(var0x, var1x, var2x);
        });
    }
}
