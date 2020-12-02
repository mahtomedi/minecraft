package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.NoneDecoratorConfiguration;

public class Spread32Decorator extends FeatureDecorator<NoneDecoratorConfiguration> {
    public Spread32Decorator(Codec<NoneDecoratorConfiguration> param0) {
        super(param0);
    }

    public Stream<BlockPos> getPositions(DecorationContext param0, Random param1, NoneDecoratorConfiguration param2, BlockPos param3) {
        int var0 = param1.nextInt(Math.max(param3.getY(), 0) + 32);
        return Stream.of(new BlockPos(param3.getX(), var0, param3.getZ()));
    }
}
