package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;

public class NoiseSlider {
    public static final Codec<NoiseSlider> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    Codec.DOUBLE.fieldOf("target").forGetter(param0x -> param0x.target),
                    ExtraCodecs.NON_NEGATIVE_INT.fieldOf("size").forGetter(param0x -> param0x.size),
                    Codec.INT.fieldOf("offset").forGetter(param0x -> param0x.offset)
                )
                .apply(param0, NoiseSlider::new)
    );
    private final double target;
    private final int size;
    private final int offset;

    public NoiseSlider(double param0, int param1, int param2) {
        this.target = param0;
        this.size = param1;
        this.offset = param2;
    }

    public double applySlide(double param0, int param1) {
        if (this.size <= 0) {
            return param0;
        } else {
            double var0 = (double)(param1 - this.offset) / (double)this.size;
            return Mth.clampedLerp(this.target, param0, var0);
        }
    }
}
