package net.minecraft.util.valueproviders;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;

public class ConstantInt extends IntProvider {
    public static final ConstantInt ZERO = new ConstantInt(0);
    public static final Codec<ConstantInt> CODEC = Codec.either(
            Codec.INT,
            RecordCodecBuilder.create(param0 -> param0.group(Codec.INT.fieldOf("value").forGetter(param0x -> param0x.value)).apply(param0, ConstantInt::new))
        )
        .xmap(param0 -> param0.map(ConstantInt::of, param0x -> param0x), param0 -> Either.left(param0.value));
    private final int value;

    public static ConstantInt of(int param0) {
        return param0 == 0 ? ZERO : new ConstantInt(param0);
    }

    private ConstantInt(int param0) {
        this.value = param0;
    }

    public int getValue() {
        return this.value;
    }

    @Override
    public int sample(Random param0) {
        return this.value;
    }

    @Override
    public int getMinValue() {
        return this.value;
    }

    @Override
    public int getMaxValue() {
        return this.value;
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else if (param0 != null && this.getClass() == param0.getClass()) {
            ConstantInt var0 = (ConstantInt)param0;
            return this.value == var0.value;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(this.value);
    }

    @Override
    public IntProviderType<?> getType() {
        return IntProviderType.CONSTANT;
    }

    @Override
    public String toString() {
        return Integer.toString(this.value);
    }
}
