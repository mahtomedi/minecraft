package net.minecraft.world.level.levelgen.placement.nether;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.CountConfiguration;
import net.minecraft.world.level.levelgen.placement.SimpleFeatureDecorator;

public class GlowstoneDecorator extends SimpleFeatureDecorator<CountConfiguration> {
    public GlowstoneDecorator(Codec<CountConfiguration> param0) {
        super(param0);
    }

    public Stream<BlockPos> place(Random param0, CountConfiguration param1, BlockPos param2) {
        return IntStream.range(0, param0.nextInt(param0.nextInt(param1.count().sample(param0)) + 1)).mapToObj(param2x -> {
            int var0 = param0.nextInt(16) + param2.getX();
            int var1x = param0.nextInt(16) + param2.getZ();
            int var2x = param0.nextInt(120) + 4;
            return new BlockPos(var0, var2x, var1x);
        });
    }
}