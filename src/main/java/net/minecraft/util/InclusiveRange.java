package net.minecraft.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.function.Function;

public record InclusiveRange<T extends Comparable<T>>(T minInclusive, T maxInclusive) {
    public static final Codec<InclusiveRange<Integer>> INT = codec(Codec.INT);

    public InclusiveRange(T param0, T param1) {
        if (param0.compareTo(param1) > 0) {
            throw new IllegalArgumentException("min_inclusive must be less than or equal to max_inclusive");
        } else {
            this.minInclusive = param0;
            this.maxInclusive = param1;
        }
    }

    public static <T extends Comparable<T>> Codec<InclusiveRange<T>> codec(Codec<T> param0) {
        return ExtraCodecs.intervalCodec(
            param0, "min_inclusive", "max_inclusive", InclusiveRange::create, InclusiveRange::minInclusive, InclusiveRange::maxInclusive
        );
    }

    public static <T extends Comparable<T>> Codec<InclusiveRange<T>> codec(Codec<T> param0, T param1, T param2) {
        Function<InclusiveRange<T>, DataResult<InclusiveRange<T>>> var0 = param2x -> {
            if (param2x.minInclusive().compareTo(param1) < 0) {
                return DataResult.error("Range limit too low, expected at least " + param1 + " [" + param2x.minInclusive() + "-" + param2x.maxInclusive() + "]");
            } else {
                return param2x.maxInclusive().compareTo(param2) > 0
                    ? DataResult.error("Range limit too high, expected at most " + param2 + " [" + param2x.minInclusive() + "-" + param2x.maxInclusive() + "]")
                    : DataResult.success(param2x);
            }
        };
        return codec(param0).flatXmap(var0, var0);
    }

    public static <T extends Comparable<T>> DataResult<InclusiveRange<T>> create(T param0x, T param1) {
        return param0x.compareTo(param1) <= 0
            ? DataResult.success(new InclusiveRange(param0x, param1))
            : DataResult.error("min_inclusive must be less than or equal to max_inclusive");
    }

    public boolean isValueInRange(T param0) {
        return param0.compareTo(this.minInclusive) >= 0 && param0.compareTo(this.maxInclusive) <= 0;
    }

    public boolean contains(InclusiveRange<T> param0) {
        return param0.minInclusive().compareTo(this.minInclusive) >= 0 && param0.maxInclusive.compareTo(this.maxInclusive) <= 0;
    }

    public String toString() {
        return "[" + this.minInclusive + ", " + this.maxInclusive + "]";
    }
}
