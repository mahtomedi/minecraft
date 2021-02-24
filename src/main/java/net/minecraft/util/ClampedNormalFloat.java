package net.minecraft.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;

public class ClampedNormalFloat extends FloatProvider {
    public static final Codec<ClampedNormalFloat> CODEC = RecordCodecBuilder.<ClampedNormalFloat>create(
            param0 -> param0.group(
                        Codec.FLOAT.fieldOf("mean").forGetter(param0x -> param0x.mean),
                        Codec.FLOAT.fieldOf("deviation").forGetter(param0x -> param0x.deviation),
                        Codec.FLOAT.fieldOf("min").forGetter(param0x -> param0x.min),
                        Codec.FLOAT.fieldOf("max").forGetter(param0x -> param0x.max)
                    )
                    .apply(param0, ClampedNormalFloat::new)
        )
        .comapFlatMap(
            param0 -> param0.max < param0.min
                    ? DataResult.error("Max must be larger than min: [" + param0.min + ", " + param0.max + "]")
                    : DataResult.success(param0),
            Function.identity()
        );
    private float mean;
    private float deviation;
    private float min;
    private float max;

    public static ClampedNormalFloat of(float param0, float param1, float param2, float param3) {
        return new ClampedNormalFloat(param0, param1, param2, param3);
    }

    private ClampedNormalFloat(float param0, float param1, float param2, float param3) {
        this.mean = param0;
        this.deviation = param1;
        this.min = param2;
        this.max = param3;
    }

    @Override
    public float sample(Random param0) {
        return sample(param0, this.mean, this.deviation, this.min, this.max);
    }

    public static float sample(Random param0, float param1, float param2, float param3, float param4) {
        return Mth.clamp(Mth.normal(param0, param1, param2), param3, param4);
    }

    @Override
    public float getMinValue() {
        return this.min;
    }

    @Override
    public float getMaxValue() {
        return this.max;
    }

    @Override
    public FloatProviderType<?> getType() {
        return FloatProviderType.CLAMPED_NORMAL;
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else if (param0 != null && this.getClass() == param0.getClass()) {
            ClampedNormalFloat var0 = (ClampedNormalFloat)param0;
            return this.mean == var0.mean && this.deviation == var0.deviation && this.min == var0.min && this.max == var0.max;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.mean, this.deviation, this.min, this.max);
    }

    @Override
    public String toString() {
        return "normal(" + this.mean + ", " + this.deviation + ") in [" + this.min + '-' + this.max + ']';
    }
}
