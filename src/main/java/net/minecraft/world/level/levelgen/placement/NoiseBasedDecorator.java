package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;

public class NoiseBasedDecorator extends RepeatingDecorator<NoiseCountFactorDecoratorConfiguration> {
    public NoiseBasedDecorator(Codec<NoiseCountFactorDecoratorConfiguration> param0) {
        super(param0);
    }

    protected int count(Random param0, NoiseCountFactorDecoratorConfiguration param1, BlockPos param2) {
        double var0 = Biome.BIOME_INFO_NOISE.getValue((double)param2.getX() / param1.noiseFactor, (double)param2.getZ() / param1.noiseFactor, false);
        return (int)Math.ceil((var0 + param1.noiseOffset) * (double)param1.noiseToCountRatio);
    }
}
