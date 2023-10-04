package net.minecraft.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Keyable;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;

public interface StringRepresentable {
    int PRE_BUILT_MAP_THRESHOLD = 16;

    String getSerializedName();

    static <E extends Enum<E> & StringRepresentable> StringRepresentable.EnumCodec<E> fromEnum(Supplier<E[]> param0) {
        return fromEnumWithMapping(param0, param0x -> param0x);
    }

    static <E extends Enum<E> & StringRepresentable> StringRepresentable.EnumCodec<E> fromEnumWithMapping(Supplier<E[]> param0, Function<String, String> param1) {
        E[] var0 = (E[])param0.get();
        Function<String, E> var1 = createNameLookup(var0, param1);
        return new StringRepresentable.EnumCodec<>(var0, var1);
    }

    static <T extends StringRepresentable> Codec<T> fromValues(Supplier<T[]> param0) {
        T[] var0 = (T[])param0.get();
        Function<String, T> var1 = createNameLookup(var0, param0x -> param0x);
        ToIntFunction<T> var2 = Util.createIndexLookup(Arrays.asList(var0));
        return new StringRepresentable.StringRepresentableCodec<>(var0, var1, var2);
    }

    static <T extends StringRepresentable> Function<String, T> createNameLookup(T[] param0, Function<String, String> param1) {
        if (param0.length > 16) {
            Map<String, T> var0 = Arrays.<StringRepresentable>stream(param0)
                .collect(Collectors.toMap(param1x -> param1.apply(param1x.getSerializedName()), param0x -> (T)param0x));
            return param1x -> param1x == null ? null : var0.get(param1x);
        } else {
            return param2 -> {
                for(T var0x : param0) {
                    if (param1.apply(var0x.getSerializedName()).equals(param2)) {
                        return var0x;
                    }
                }

                return null;
            };
        }
    }

    static Keyable keys(final StringRepresentable[] param0) {
        return new Keyable() {
            @Override
            public <T> Stream<T> keys(DynamicOps<T> param0x) {
                return Arrays.stream(param0).map(StringRepresentable::getSerializedName).map(param0::createString);
            }
        };
    }

    @Deprecated
    public static class EnumCodec<E extends Enum<E> & StringRepresentable> extends StringRepresentable.StringRepresentableCodec<E> {
        private final Function<String, E> resolver;

        public EnumCodec(E[] param0, Function<String, E> param1) {
            super(param0, param1, param0x -> param0x.ordinal());
            this.resolver = param1;
        }

        @Nullable
        public E byName(@Nullable String param0) {
            return this.resolver.apply(param0);
        }

        public E byName(@Nullable String param0, E param1) {
            return Objects.requireNonNullElse(this.byName(param0), param1);
        }
    }

    public static class StringRepresentableCodec<S extends StringRepresentable> implements Codec<S> {
        private final Codec<S> codec;

        public StringRepresentableCodec(S[] param0, Function<String, S> param1, ToIntFunction<S> param2) {
            this.codec = ExtraCodecs.orCompressed(
                ExtraCodecs.stringResolverCodec(StringRepresentable::getSerializedName, param1),
                ExtraCodecs.idResolverCodec(param2, param1x -> param1x >= 0 && param1x < param0.length ? param0[param1x] : null, -1)
            );
        }

        @Override
        public <T> DataResult<Pair<S, T>> decode(DynamicOps<T> param0, T param1) {
            return this.codec.decode(param0, param1);
        }

        public <T> DataResult<T> encode(S param0, DynamicOps<T> param1, T param2) {
            return this.codec.encode(param0, param1, param2);
        }
    }
}
