package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.feature.configurations.NoiseDependantDecoratorConfiguration;

public class CountNoiseDecorator extends RepeatingDecorator<NoiseDependantDecoratorConfiguration> {
    public CountNoiseDecorator(Codec<NoiseDependantDecoratorConfiguration> param0) {
        super(param0);
    }

    protected int count(Random param0, NoiseDependantDecoratorConfiguration param1, BlockPos param2) {
        double var0 = Biome.BIOME_INFO_NOISE.getValue((double)param2.getX() / 200.0, (double)param2.getZ() / 200.0, false);
        return var0 < param1.noiseLevel ? param1.belowNoise : param1.aboveNoise;
    }
}
