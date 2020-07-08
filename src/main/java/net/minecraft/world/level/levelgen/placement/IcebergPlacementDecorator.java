package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.NoneDecoratorConfiguration;

public class IcebergPlacementDecorator extends SimpleFeatureDecorator<NoneDecoratorConfiguration> {
    public IcebergPlacementDecorator(Codec<NoneDecoratorConfiguration> param0) {
        super(param0);
    }

    public Stream<BlockPos> place(Random param0, NoneDecoratorConfiguration param1, BlockPos param2) {
        int var0 = param0.nextInt(8) + 4 + param2.getX();
        int var1 = param0.nextInt(8) + 4 + param2.getZ();
        return Stream.of(new BlockPos(var0, param2.getY(), var1));
    }
}
