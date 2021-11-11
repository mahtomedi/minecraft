package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;

public class NoiseThresholdCountPlacement extends RepeatingPlacement {
    public static final Codec<NoiseThresholdCountPlacement> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    Codec.DOUBLE.fieldOf("noise_level").forGetter(param0x -> param0x.noiseLevel),
                    Codec.INT.fieldOf("below_noise").forGetter(param0x -> param0x.belowNoise),
                    Codec.INT.fieldOf("above_noise").forGetter(param0x -> param0x.aboveNoise)
                )
                .apply(param0, NoiseThresholdCountPlacement::new)
    );
    private final double noiseLevel;
    private final int belowNoise;
    private final int aboveNoise;

    private NoiseThresholdCountPlacement(double param0, int param1, int param2) {
        this.noiseLevel = param0;
        this.belowNoise = param1;
        this.aboveNoise = param2;
    }

    public static NoiseThresholdCountPlacement of(double param0, int param1, int param2) {
        return new NoiseThresholdCountPlacement(param0, param1, param2);
    }

    @Override
    protected int count(Random param0, BlockPos param1) {
        double var0 = Biome.BIOME_INFO_NOISE.getValue((double)param1.getX() / 200.0, (double)param1.getZ() / 200.0, false);
        return var0 < this.noiseLevel ? this.belowNoise : this.aboveNoise;
    }

    @Override
    public PlacementModifierType<?> type() {
        return PlacementModifierType.NOISE_THRESHOLD_COUNT;
    }
}
