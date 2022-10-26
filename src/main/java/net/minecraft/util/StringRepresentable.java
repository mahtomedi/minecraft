package net.minecraft.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Keyable;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;

public interface StringRepresentable {
    int PRE_BUILT_MAP_THRESHOLD = 16;

    String getSerializedName();

    static <E extends Enum<E> & StringRepresentable> StringRepresentable.EnumCodec<E> fromEnum(Supplier<E[]> param0) {
        E[] var0 = (E[])param0.get();
        if (var0.length > 16) {
            Map<String, E> var1 = Arrays.stream(var0)
                .collect(Collectors.toMap(param0x -> param0x.getSerializedName(), (Function<? super E, ? extends E>)(param0x -> param0x)));
            return new StringRepresentable.EnumCodec<>(var0, param1 -> param1 == null ? null : var1.get(param1));
        } else {
            return new StringRepresentable.EnumCodec<>(var0, param1 -> {
                for(E var0x : var0) {
                    if (var0x.getSerializedName().equals(param1)) {
                        return var0x;
                    }
                }

                return null;
            });
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
    public static class EnumCodec<E extends Enum<E> & StringRepresentable> implements Codec<E> {
        private final Codec<E> codec;
        private final Function<String, E> resolver;

        public EnumCodec(E[] param0, Function<String, E> param1) {
            this.codec = ExtraCodecs.orCompressed(
                ExtraCodecs.stringResolverCodec(param0x -> param0x.getSerializedName(), param1),
                ExtraCodecs.idResolverCodec(param0x -> param0x.ordinal(), param1x -> param1x >= 0 && param1x < param0.length ? param0[param1x] : null, -1)
            );
            this.resolver = param1;
        }

        @Override
        public <T> DataResult<Pair<E, T>> decode(DynamicOps<T> param0, T param1) {
            return this.codec.decode(param0, param1);
        }

        public <T> DataResult<T> encode(E param0, DynamicOps<T> param1, T param2) {
            return this.codec.encode(param0, param1, param2);
        }

        @Nullable
        public E byName(@Nullable String param0) {
            return this.resolver.apply(param0);
        }
    }
}
