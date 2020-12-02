package net.minecraft.util;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;

public class UniformInt {
    public static final Codec<UniformInt> CODEC = Codec.either(
            Codec.INT,
            RecordCodecBuilder.<UniformInt>create(
                    param0 -> param0.group(
                                Codec.INT.fieldOf("base").forGetter(param0x -> param0x.baseValue),
                                Codec.INT.fieldOf("spread").forGetter(param0x -> param0x.spread)
                            )
                            .apply(param0, UniformInt::new)
                )
                .comapFlatMap(
                    param0 -> param0.spread < 0 ? DataResult.error("Spread must be non-negative, got: " + param0.spread) : DataResult.success(param0),
                    Function.identity()
                )
        )
        .xmap(param0 -> param0.map(UniformInt::fixed, param0x -> param0x), param0 -> param0.spread == 0 ? Either.left(param0.baseValue) : Either.right(param0));
    private final int baseValue;
    private final int spread;

    public static Codec<UniformInt> codec(int param0, int param1, int param2) {
        Function<UniformInt, DataResult<UniformInt>> var0 = param3 -> {
            if (param3.baseValue < param0 || param3.baseValue > param1) {
                return DataResult.error("Base value out of range: " + param3.baseValue + " [" + param0 + "-" + param1 + "]");
            } else {
                return param3.spread <= param2 ? DataResult.success(param3) : DataResult.error("Spread too big: " + param3.spread + " > " + param2);
            }
        };
        return CODEC.flatXmap(var0, var0);
    }

    private UniformInt(int param0, int param1) {
        this.baseValue = param0;
        this.spread = param1;
    }

    public static UniformInt fixed(int param0) {
        return new UniformInt(param0, 0);
    }

    public static UniformInt of(int param0, int param1) {
        return new UniformInt(param0, param1);
    }

    public int sample(Random param0) {
        return this.spread == 0 ? this.baseValue : this.baseValue + param0.nextInt(this.spread + 1);
    }

    public int getBaseValue() {
        return this.baseValue;
    }

    public int getMaxValue() {
        return this.baseValue + this.spread;
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else if (param0 != null && this.getClass() == param0.getClass()) {
            UniformInt var0 = (UniformInt)param0;
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
