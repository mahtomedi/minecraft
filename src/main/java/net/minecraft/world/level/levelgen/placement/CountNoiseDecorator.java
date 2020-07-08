package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.feature.configurations.NoiseDependantDecoratorConfiguration;

public class CountNoiseDecorator extends FeatureDecorator<NoiseDependantDecoratorConfiguration> {
    public CountNoiseDecorator(Codec<NoiseDependantDecoratorConfiguration> param0) {
        super(param0);
    }

    public Stream<BlockPos> getPositions(DecorationContext param0, Random param1, NoiseDependantDecoratorConfiguration param2, BlockPos param3) {
        double var0 = Biome.BIOME_INFO_NOISE.getValue((double)param3.getX() / 200.0, (double)param3.getZ() / 200.0, false);
        int var1 = var0 < param2.noiseLevel ? param2.belowNoise : param2.aboveNoise;
        return IntStream.range(0, var1).mapToObj(param1x -> param3);
    }
}
