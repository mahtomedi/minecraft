package net.minecraft.util;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;

public class UniformFloat {
    public static final Codec<UniformFloat> CODEC = Codec.either(
            Codec.FLOAT,
            RecordCodecBuilder.<UniformFloat>create(
                    param0 -> param0.group(
                                Codec.FLOAT.fieldOf("base").forGetter(param0x -> param0x.baseValue),
                                Codec.FLOAT.fieldOf("spread").forGetter(param0x -> param0x.spread)
                            )
                            .apply(param0, UniformFloat::new)
                )
                .comapFlatMap(
                    param0 -> param0.spread < 0.0F ? DataResult.error("Spread must be non-negative, got: " + param0.spread) : DataResult.success(param0),
                    Function.identity()
                )
        )
        .xmap(
            param0 -> param0.map(UniformFloat::fixed, param0x -> param0x),
            param0 -> param0.spread == 0.0F ? Either.left(param0.baseValue) : Either.right(param0)
        );
    private final float baseValue;
    private final float spread;

    public static Codec<UniformFloat> codec(float param0, float param1, float param2) {
        Function<UniformFloat, DataResult<UniformFloat>> var0 = param3 -> {
            if (!(param3.baseValue >= param0) || !(param3.baseValue <= param1)) {
                return DataResult.error("Base value out of range: " + param3.baseValue + " [" + param0 + "-" + param1 + "]");
            } else {
                return param3.spread <= param2 ? DataResult.success(param3) : DataResult.error("Spread too big: " + param3.spread + " > " + param2);
            }
        };
        return CODEC.flatXmap(var0, var0);
    }

    private UniformFloat(float param0, float param1) {
        this.baseValue = param0;
        this.spread = param1;
    }

    public static UniformFloat fixed(float param0) {
        return new UniformFloat(param0, 0.0F);
    }

    public static UniformFloat of(float param0, float param1) {
        return new UniformFloat(param0, param1);
    }

    public float sample(Random param0) {
        return this.spread == 0.0F ? this.baseValue : Mth.randomBetween(param0, this.baseValue, this.baseValue + this.spread);
    }

    public float getBaseValue() {
        return this.baseValue;
    }

    public float getMaxValue() {
        return this.baseValue + this.spread;
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
