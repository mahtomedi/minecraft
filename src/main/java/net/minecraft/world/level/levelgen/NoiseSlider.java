package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;

public record NoiseSlider(double target, int size, int offset) {
    public static final Codec<NoiseSlider> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    Codec.DOUBLE.fieldOf("target").forGetter(param0x -> param0x.target),
                    ExtraCodecs.NON_NEGATIVE_INT.fieldOf("size").forGetter(param0x -> param0x.size),
                    Codec.INT.fieldOf("offset").forGetter(param0x -> param0x.offset)
                )
                .apply(param0, NoiseSlider::new)
    );

    public double applySlide(double param0, double param1) {
        if (this.size <= 0) {
            return param0;
        } else {
            double var0 = (param1 - (double)this.offset) / (double)this.size;
            return Mth.clampedLerp(this.target, param0, var0);
        }
    }
}
