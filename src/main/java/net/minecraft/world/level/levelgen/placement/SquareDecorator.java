package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.NoneDecoratorConfiguration;

public class SquareDecorator extends FeatureDecorator<NoneDecoratorConfiguration> {
    public SquareDecorator(Codec<NoneDecoratorConfiguration> param0) {
        super(param0);
    }

    public Stream<BlockPos> getPositions(DecorationContext param0, Random param1, NoneDecoratorConfiguration param2, BlockPos param3) {
        int var0 = param1.nextInt(16) + param3.getX();
        int var1 = param1.nextInt(16) + param3.getZ();
        return Stream.of(new BlockPos(var0, param3.getY(), var1));
    }
}
