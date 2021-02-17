package net.minecraft.util;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.PrimitiveCodec;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

public class ExtraCodecs {
    public static final Codec<DoubleStream> DOUBLE_STREAM = new PrimitiveCodec<DoubleStream>() {
        @Override
        public <T> DataResult<DoubleStream> read(DynamicOps<T> param0, T param1) {
            return ExtraCodecs.asDoubleStream(param0, param1);
        }

        public <T> T write(DynamicOps<T> param0, DoubleStream param1) {
            return ExtraCodecs.createDoubleList(param0, param1);
        }

        @Override
        public String toString() {
            return "DoubleStream";
        }
    };

    private static <T> DataResult<DoubleStream> asDoubleStream(DynamicOps<T> param0, T param1) {
        return param0.getStream(param1)
            .flatMap(
                param2 -> {
                    List<T> var0x = param2.collect(Collectors.toList());
                    return var0x.stream().allMatch(param1x -> param0.getNumberValue((T)param1x).result().isPresent())
                        ? DataResult.success(var0x.stream().mapToDouble(param1x -> param0.getNumberValue((T)param1x).result().get().doubleValue()))
                        : DataResult.error("Some elements are not doubles: " + param1);
                }
            );
    }

    private static <T> T createDoubleList(DynamicOps<T> param0, DoubleStream param1) {
        return param0.createList(param1.mapToObj(param0::createDouble));
    }

    public static <F, S> Codec<Either<F, S>> xor(Codec<F> param0, Codec<S> param1) {
        return new ExtraCodecs.XorCodec<>(param0, param1);
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
            return "XorCodec[" + this.first + ", " + this.second + ']';
        }
    }
}
