package net.minecraft.util.valueproviders;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;

public class ConstantFloat extends FloatProvider {
    public static final ConstantFloat ZERO = new ConstantFloat(0.0F);
    public static final Codec<ConstantFloat> CODEC = Codec.either(
            Codec.FLOAT,
            RecordCodecBuilder.create(
                param0 -> param0.group(Codec.FLOAT.fieldOf("value").forGetter(param0x -> param0x.value)).apply(param0, ConstantFloat::new)
            )
        )
        .xmap(param0 -> param0.map(ConstantFloat::of, param0x -> param0x), param0 -> Either.left(param0.value));
    private final float value;

    public static ConstantFloat of(float param0) {
        return param0 == 0.0F ? ZERO : new ConstantFloat(param0);
    }

    private ConstantFloat(float param0) {
        this.value = param0;
    }

    public float getValue() {
        return this.value;
    }

    @Override
    public float sample(Random param0) {
        return this.value;
    }

    @Override
    public float getMinValue() {
        return this.value;
    }

    @Override
    public float getMaxValue() {
        return this.value + 1.0F;
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else if (param0 != null && this.getClass() == param0.getClass()) {
            ConstantFloat var0 = (ConstantFloat)param0;
            return this.value == var0.value;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Float.hashCode(this.value);
    }

    @Override
    public FloatProviderType<?> getType() {
        return FloatProviderType.CONSTANT;
    }

    @Override
    public String toString() {
        return Float.toString(this.value);
    }
}
