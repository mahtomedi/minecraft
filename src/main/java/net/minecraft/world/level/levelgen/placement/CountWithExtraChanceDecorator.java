package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;

public class CountWithExtraChanceDecorator extends SimpleFeatureDecorator<FrequencyWithExtraChanceDecoratorConfiguration> {
    public CountWithExtraChanceDecorator(Codec<FrequencyWithExtraChanceDecoratorConfiguration> param0) {
        super(param0);
    }

    public Stream<BlockPos> place(Random param0, FrequencyWithExtraChanceDecoratorConfiguration param1, BlockPos param2) {
        int var0 = param1.count + (param0.nextFloat() < param1.extraChance ? param1.extraCount : 0);
        return IntStream.range(0, var0).mapToObj(param1x -> param2);
    }
}
