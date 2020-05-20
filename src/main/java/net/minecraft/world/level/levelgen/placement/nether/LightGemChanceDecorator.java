package net.minecraft.world.level.levelgen.placement.nether;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.placement.FrequencyDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.SimpleFeatureDecorator;

public class LightGemChanceDecorator extends SimpleFeatureDecorator<FrequencyDecoratorConfiguration> {
    public LightGemChanceDecorator(Codec<FrequencyDecoratorConfiguration> param0) {
        super(param0);
    }

    public Stream<BlockPos> place(Random param0, FrequencyDecoratorConfiguration param1, BlockPos param2) {
        return IntStream.range(0, param0.nextInt(param0.nextInt(param1.count) + 1)).mapToObj(param2x -> {
            int var0 = param0.nextInt(16) + param2.getX();
            int var1x = param0.nextInt(16) + param2.getZ();
            int var2x = param0.nextInt(120) + 4;
            return new BlockPos(var0, var2x, var1x);
        });
    }
}
