package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;

public class DecoratedDecorator extends FeatureDecorator<DecoratedDecoratorConfiguration> {
    public DecoratedDecorator(Codec<DecoratedDecoratorConfiguration> param0) {
        super(param0);
    }

    public Stream<BlockPos> getPositions(DecorationContext param0, Random param1, DecoratedDecoratorConfiguration param2, BlockPos param3) {
        return param2.outer().getPositions(param0, param1, param3).flatMap(param3x -> param2.inner().getPositions(param0, param1, param3x));
    }
}
