package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.NoneDecoratorConfiguration;

public class EmeraldPlacementDecorator extends SimpleFeatureDecorator<NoneDecoratorConfiguration> {
    public EmeraldPlacementDecorator(
        Function<Dynamic<?>, ? extends NoneDecoratorConfiguration> param0, Function<Random, ? extends NoneDecoratorConfiguration> param1
    ) {
        super(param0, param1);
    }

    public Stream<BlockPos> place(Random param0, NoneDecoratorConfiguration param1, BlockPos param2) {
        int var0 = 3 + param0.nextInt(6);
        return IntStream.range(0, var0).mapToObj(param2x -> {
            int var0x = param0.nextInt(16) + param2.getX();
            int var1x = param0.nextInt(16) + param2.getZ();
            int var2x = param0.nextInt(28) + 4;
            return new BlockPos(var0x, var2x, var1x);
        });
    }
}
