package net.minecraft.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Keyable;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public interface StringRepresentable {
    String getSerializedName();

    static <E extends Enum<E> & StringRepresentable> Codec<E> fromEnum(Supplier<E[]> param0, Function<? super String, ? extends E> param1) {
        E[] var0 = (E[])param0.get();
        return fromStringResolver(Enum::ordinal, param1x -> var0[param1x], param1);
    }

    static <E extends StringRepresentable> Codec<E> fromStringResolver(
        final ToIntFunction<E> param0, final IntFunction<E> param1, final Function<? super String, ? extends E> param2
    ) {
        return new Codec<E>() {
            public <T> DataResult<T> encode(E param0x, DynamicOps<T> param1x, T param2x) {
                return param1.compressMaps()
                    ? param1.mergeToPrimitive(param2, param1.createInt(param0.applyAsInt(param0)))
                    : param1.mergeToPrimitive(param2, param1.createString(param0.getSerializedName()));
            }

            @Override
            public <T> DataResult<Pair<E, T>> decode(DynamicOps<T> param0x, T param1x) {
                return param0.compressMaps()
                    ? param0.getNumberValue(param1)
                        .flatMap(
                            param1xxx -> Optional.ofNullable(param1.apply(param1xxx.intValue()))
                                    .map(DataResult::success)
                                    .orElseGet(() -> DataResult.error("Unknown element id: " + param1xxx))
                        )
                        .map(param1xxx -> Pair.of(param1xxx, param0.empty()))
                    : param0.getStringValue(param1)
                        .flatMap(
                            param1xxx -> Optional.ofNullable(param2.apply(param1xxx))
                                    .map(DataResult::success)
                                    .orElseGet(() -> DataResult.error("Unknown element name: " + param1xxx))
                        )
                        .map(param1xxx -> Pair.of(param1xxx, param0.empty()));
            }

            @Override
            public String toString() {
                return "StringRepresentable[" + param0 + "]";
            }
        };
    }

    static Keyable keys(final StringRepresentable[] param0) {
        return new Keyable() {
            @Override
            public <T> Stream<T> keys(DynamicOps<T> param0x) {
                return param0.compressMaps()
                    ? IntStream.range(0, param0.length).mapToObj(param0::createInt)
                    : Arrays.stream(param0).map(StringRepresentable::getSerializedName).map(param0::createString);
            }
        };
    }
}
