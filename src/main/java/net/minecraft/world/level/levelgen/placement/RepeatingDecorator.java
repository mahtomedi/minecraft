package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public abstract class RepeatingDecorator<DC extends DecoratorConfiguration> extends FeatureDecorator<DC> {
    public RepeatingDecorator(Codec<DC> param0) {
        super(param0);
    }

    protected abstract int count(Random var1, DC var2, BlockPos var3);

    @Override
    public Stream<BlockPos> getPositions(DecorationContext param0, Random param1, DC param2, BlockPos param3) {
        return IntStream.range(0, this.count(param1, param2, param3)).mapToObj(param1x -> param3);
    }
}
