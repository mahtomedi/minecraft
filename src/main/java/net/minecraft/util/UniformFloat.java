package net.minecraft.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;

public class UniformFloat extends FloatProvider {
    public static final Codec<UniformFloat> CODEC = RecordCodecBuilder.<UniformFloat>create(
            param0 -> param0.group(
                        Codec.FLOAT.fieldOf("base").forGetter(param0x -> param0x.baseValue), Codec.FLOAT.fieldOf("spread").forGetter(param0x -> param0x.spread)
                    )
                    .apply(param0, UniformFloat::new)
        )
        .comapFlatMap(
            param0 -> param0.spread < 0.0F ? DataResult.error("Spread must be non-negative, got: " + param0.spread) : DataResult.success(param0),
            Function.identity()
        );
    private final float baseValue;
    private final float spread;

    private UniformFloat(float param0, float param1) {
        this.baseValue = param0;
        this.spread = param1;
    }

    public static UniformFloat of(float param0, float param1) {
        return new UniformFloat(param0, param1);
    }

    @Override
    public float sample(Random param0) {
        return this.spread == 0.0F ? this.baseValue : Mth.randomBetween(param0, this.baseValue, this.baseValue + this.spread);
    }

    @Override
    public float getMinValue() {
        return this.baseValue;
    }

    @Override
    public float getMaxValue() {
        return this.baseValue + this.spread;
    }

    @Override
    public FloatProviderType<?> getType() {
        return FloatProviderType.UNIFORM;
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else if (param0 != null && this.getClass() == param0.getClass()) {
            UniformFloat var0 = (UniformFloat)param0;
            return this.baseValue == var0.baseValue && this.spread == var0.spread;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.baseValue, this.spread);
    }

    @Override
    public String toString() {
        return "[" + this.baseValue + '-' + (this.baseValue + this.spread) + ']';
    }
}
