package net.minecraft.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.Codec.ResultFunction;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.Util;
import org.apache.commons.lang3.mutable.MutableObject;

public class ExtraCodecs {
    public static final Codec<Integer> NON_NEGATIVE_INT = intRangeWithMessage(0, Integer.MAX_VALUE, param0 -> "Value must be non-negative: " + param0);
    public static final Codec<Integer> POSITIVE_INT = intRangeWithMessage(1, Integer.MAX_VALUE, param0 -> "Value must be positive: " + param0);
    public static final Codec<Float> POSITIVE_FLOAT = floatRangeMinExclusiveWithMessage(0.0F, Float.MAX_VALUE, param0 -> "Value must be positive: " + param0);

    public static <F, S> Codec<Either<F, S>> xor(Codec<F> param0, Codec<S> param1) {
        return new ExtraCodecs.XorCodec<>(param0, param1);
    }

    public static <P, I> Codec<I> intervalCodec(
        Codec<P> param0, String param1, String param2, BiFunction<P, P, DataResult<I>> param3, Function<I, P> param4, Function<I, P> param5
    ) {
        Codec<I> var0 = Codec.list(param0).comapFlatMap(param1x -> Util.fixedSize(param1x, 2).flatMap(param1xx -> {
                P var0x = param1xx.get(0);
                P var1x = param1xx.get(1);
                return param3.apply(var0x, var1x);
            }), param2x -> ImmutableList.of(param4.apply(param2x), param5.apply(param2x)));
        Codec<I> var1 = RecordCodecBuilder.<Pair>create(
                param3x -> param3x.group(param0.fieldOf(param1).forGetter(Pair::getFirst), param0.fieldOf(param2).forGetter(Pair::getSecond))
                        .apply(param3x, Pair::of)
            )
            .comapFlatMap(
                param1x -> param3.apply((P)param1x.getFirst(), (P)param1x.getSecond()), param2x -> Pair.of(param4.apply(param2x), param5.apply(param2x))
            );
        Codec<I> var2 = new ExtraCodecs.EitherCodec<>(var0, var1).xmap(param0x -> param0x.map(param0xx -> param0xx, param0xx -> param0xx), Either::left);
        return Codec.either(param0, var2).comapFlatMap(param1x -> param1x.map(param1xx -> param3.apply(param1xx, param1xx), DataResult::success), param2x -> {
            P var0x = param4.apply(param2x);
            P var1x = param5.apply(param2x);
            return Objects.equals(var0x, var1x) ? Either.left(var0x) : Either.right(param2x);
        });
    }

    public static <A> ResultFunction<A> orElsePartial(final A param0) {
        return new ResultFunction<A>() {
            @Override
            public <T> DataResult<Pair<A, T>> apply(DynamicOps<T> param0x, T param1, DataResult<Pair<A, T>> param2) {
                MutableObject<String> var0 = new MutableObject<>();
                Optional<Pair<A, T>> var1 = param2.resultOrPartial(var0::setValue);
                return var1.isPresent() ? param2 : DataResult.error("(" + (String)var0.getValue() + " -> using default)", Pair.of(param0, param1));
            }

            @Override
            public <T> DataResult<T> coApply(DynamicOps<T> param0x, A param1, DataResult<T> param2) {
                return param2;
            }

            @Override
            public String toString() {
                return "OrElsePartial[" + param0 + "]";
            }
        };
    }

    private static <N extends Number & Comparable<N>> Function<N, DataResult<N>> checkRangeWithMessage(N param0, N param1, Function<N, String> param2) {
        return param3 -> param3.compareTo(param0) >= 0 && param3.compareTo(param1) <= 0 ? DataResult.success(param3) : DataResult.error(param2.apply(param3));
    }

    private static Codec<Integer> intRangeWithMessage(int param0, int param1, Function<Integer, String> param2) {
        Function<Integer, DataResult<Integer>> var0 = checkRangeWithMessage(param0, param1, param2);
        return Codec.INT.flatXmap(var0, var0);
    }

    private static <N extends Number & Comparable<N>> Function<N, DataResult<N>> checkRangeMinExclusiveWithMessage(
        N param0, N param1, Function<N, String> param2
    ) {
        return param3 -> param3.compareTo(param0) > 0 && param3.compareTo(param1) <= 0 ? DataResult.success(param3) : DataResult.error(param2.apply(param3));
    }

    private static Codec<Float> floatRangeMinExclusiveWithMessage(float param0, float param1, Function<Float, String> param2) {
        Function<Float, DataResult<Float>> var0 = checkRangeMinExclusiveWithMessage(param0, param1, param2);
        return Codec.FLOAT.flatXmap(var0, var0);
    }

    public static <T> Function<List<T>, DataResult<List<T>>> nonEmptyListCheck() {
        return param0 -> param0.isEmpty() ? DataResult.error("List must have contents") : DataResult.success(param0);
    }

    public static <T> Codec<List<T>> nonEmptyList(Codec<List<T>> param0) {
        return param0.flatXmap(nonEmptyListCheck(), nonEmptyListCheck());
    }

    public static <T> Function<List<Supplier<T>>, DataResult<List<Supplier<T>>>> nonNullSupplierListCheck() {
        return param0 -> {
            List<String> var0 = Lists.newArrayList();

            for(int var1 = 0; var1 < param0.size(); ++var1) {
                Supplier<T> var2 = param0.get(var1);

                try {
                    if (var2.get() == null) {
                        var0.add("Missing value [" + var1 + "] : " + var2);
                    }
                } catch (Exception var5) {
                    var0.add("Invalid value [" + var1 + "]: " + var2 + ", message: " + var5.getMessage());
                }
            }

            return !var0.isEmpty() ? DataResult.error(String.join("; ", var0)) : DataResult.success(param0, Lifecycle.stable());
        };
    }

    public static <T> Function<Supplier<T>, DataResult<Supplier<T>>> nonNullSupplierCheck() {
        return param0 -> {
            try {
                if (param0.get() == null) {
                    return DataResult.error("Missing value: " + param0);
                }
            } catch (Exception var2) {
                return DataResult.error("Invalid value: " + param0 + ", message: " + var2.getMessage());
            }

            return DataResult.success(param0, Lifecycle.stable());
        };
    }

    static final class EitherCodec<F, S> implements Codec<Either<F, S>> {
        private final Codec<F> first;
        private final Codec<S> second;

        public EitherCodec(Codec<F> param0, Codec<S> param1) {
            this.first = param0;
            this.second = param1;
        }

        @Override
        public <T> DataResult<Pair<Either<F, S>, T>> decode(DynamicOps<T> param0, T param1) {
            DataResult<Pair<Either<F, S>, T>> var0 = this.first.decode(param0, param1).map(param0x -> param0x.mapFirst(Either::left));
            if (!var0.error().isPresent()) {
                return var0;
            } else {
                DataResult<Pair<Either<F, S>, T>> var1 = this.second.decode(param0, param1).map(param0x -> param0x.mapFirst(Either::right));
                return !var1.error().isPresent() ? var1 : var0.apply2((param0x, param1x) -> param1x, var1);
            }
        }

        public <T> DataResult<T> encode(Either<F, S> param0, DynamicOps<T> param1, T param2) {
            return param0.map(param2x -> this.first.encode(param2x, param1, param2), param2x -> this.second.encode(param2x, param1, param2));
        }

        @Override
        public boolean equals(Object param0) {
            if (this == param0) {
                return true;
            } else if (param0 != null && this.getClass() == param0.getClass()) {
                ExtraCodecs.EitherCodec<?, ?> var0 = (ExtraCodecs.EitherCodec)param0;
                return Objects.equals(this.first, var0.first) && Objects.equals(this.second, var0.second);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.first, this.second);
        }

        @Override
        public String toString() {
            return "EitherCodec[" + this.first + ", " + this.second + "]";
        }
    }

    static final class XorCodec<F, S> implements Codec<Either<F, S>> {
        private final Codec<F> first;
        private final Codec<S> second;

        public XorCodec(Codec<F> param0, Codec<S> param1) {
            this.first = param0;
            this.second = param1;
        }

        @Override
        public <T> DataResult<Pair<Either<F, S>, T>> decode(DynamicOps<T> param0, T param1) {
            DataResult<Pair<Either<F, S>, T>> var0 = this.first.decode(param0, param1).map(param0x -> param0x.mapFirst(Either::left));
            DataResult<Pair<Either<F, S>, T>> var1 = this.second.decode(param0, param1).map(param0x -> param0x.mapFirst(Either::right));
            Optional<Pair<Either<F, S>, T>> var2 = var0.result();
            Optional<Pair<Either<F, S>, T>> var3 = var1.result();
            if (var2.isPresent() && var3.isPresent()) {
                return DataResult.error(
                    "Both alternatives read successfully, can not pick the correct one; first: " + var2.get() + " second: " + var3.get(), var2.get()
                );
            } else {
                return var2.isPresent() ? var0 : var1;
            }
        }

        public <T> DataResult<T> encode(Either<F, S> param0, DynamicOps<T> param1, T param2) {
            return param0.map(param2x -> this.first.encode(param2x, param1, param2), param2x -> this.second.encode(param2x, param1, param2));
        }

        @Override
        public boolean equals(Object param0) {
            if (this == param0) {
                return true;
            } else if (param0 != null && this.getClass() == param0.getClass()) {
                ExtraCodecs.XorCodec<?, ?> var0 = (ExtraCodecs.XorCodec)param0;
                return Objects.equals(this.first, var0.first) && Objects.equals(this.second, var0.second);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.first, this.second);
        }

        @Override
        public String toString() {
            return "XorCodec[" + this.first + ", " + this.second + "]";
        }
    }
}
