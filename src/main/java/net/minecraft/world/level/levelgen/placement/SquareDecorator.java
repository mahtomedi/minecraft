package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.NoneDecoratorConfiguration;

public class SquareDecorator extends SimpleFeatureDecorator<NoneDecoratorConfiguration> {
    public SquareDecorator(Codec<NoneDecoratorConfiguration> param0) {
        super(param0);
    }

    public Stream<BlockPos> place(Random param0, NoneDecoratorConfiguration param1, BlockPos param2) {
        int var0 = param0.nextInt(16) + param2.getX();
        int var1 = param0.nextInt(16) + param2.getZ();
        int var2 = param2.getY();
        return Stream.of(new BlockPos(var0, var2, var1));
    }
}
