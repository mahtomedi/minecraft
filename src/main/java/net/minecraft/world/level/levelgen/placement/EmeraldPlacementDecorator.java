package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.NoneDecoratorConfiguration;

public class EmeraldPlacementDecorator extends SimpleFeatureDecorator<NoneDecoratorConfiguration> {
    public EmeraldPlacementDecorator(Codec<NoneDecoratorConfiguration> param0) {
        super(param0);
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
