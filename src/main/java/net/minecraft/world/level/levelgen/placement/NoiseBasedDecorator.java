package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;

public class NoiseBasedDecorator extends SimpleFeatureDecorator<NoiseCountFactorDecoratorConfiguration> {
    public NoiseBasedDecorator(Codec<NoiseCountFactorDecoratorConfiguration> param0) {
        super(param0);
    }

    public Stream<BlockPos> place(Random param0, NoiseCountFactorDecoratorConfiguration param1, BlockPos param2) {
        double var0 = Biome.BIOME_INFO_NOISE.getValue((double)param2.getX() / param1.noiseFactor, (double)param2.getZ() / param1.noiseFactor, false);
        int var1 = (int)Math.ceil((var0 + param1.noiseOffset) * (double)param1.noiseToCountRatio);
        return IntStream.range(0, var1).mapToObj(param1x -> param2);
    }
}
