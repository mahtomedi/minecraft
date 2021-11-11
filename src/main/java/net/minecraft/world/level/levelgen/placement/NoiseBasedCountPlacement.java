package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;

public class NoiseBasedCountPlacement extends RepeatingPlacement {
    public static final Codec<NoiseBasedCountPlacement> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    Codec.INT.fieldOf("noise_to_count_ratio").forGetter(param0x -> param0x.noiseToCountRatio),
                    Codec.DOUBLE.fieldOf("noise_factor").forGetter(param0x -> param0x.noiseFactor),
                    Codec.DOUBLE.fieldOf("noise_offset").orElse(0.0).forGetter(param0x -> param0x.noiseOffset)
                )
                .apply(param0, NoiseBasedCountPlacement::new)
    );
    private final int noiseToCountRatio;
    private final double noiseFactor;
    private final double noiseOffset;

    private NoiseBasedCountPlacement(int param0, double param1, double param2) {
        this.noiseToCountRatio = param0;
        this.noiseFactor = param1;
        this.noiseOffset = param2;
    }

    public static NoiseBasedCountPlacement of(int param0, double param1, double param2) {
        return new NoiseBasedCountPlacement(param0, param1, param2);
    }

    @Override
    protected int count(Random param0, BlockPos param1) {
        double var0 = Biome.BIOME_INFO_NOISE.getValue((double)param1.getX() / this.noiseFactor, (double)param1.getZ() / this.noiseFactor, false);
        return (int)Math.ceil((var0 + this.noiseOffset) * (double)this.noiseToCountRatio);
    }

    @Override
    public PlacementModifierType<?> type() {
        return PlacementModifierType.NOISE_BASED_COUNT;
    }
}
