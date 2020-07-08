package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.CountConfiguration;

public class CountDecorator extends SimpleFeatureDecorator<CountConfiguration> {
    public CountDecorator(Codec<CountConfiguration> param0) {
        super(param0);
    }

    public Stream<BlockPos> place(Random param0, CountConfiguration param1, BlockPos param2) {
        return IntStream.range(0, param1.count().sample(param0)).mapToObj(param1x -> param2);
    }
}
