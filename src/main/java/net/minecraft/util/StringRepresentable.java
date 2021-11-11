package net.minecraft.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Keyable;
import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public interface StringRepresentable {
    String getSerializedName();

    static <E extends Enum<E> & StringRepresentable> Codec<E> fromEnum(Supplier<E[]> param0, Function<String, E> param1) {
        E[] var0 = (E[])param0.get();
        return ExtraCodecs.orCompressed(
            ExtraCodecs.stringResolverCodec(param0x -> param0x.getSerializedName(), param1),
            ExtraCodecs.idResolverCodec(param0x -> param0x.ordinal(), param1x -> param1x >= 0 && param1x < var0.length ? var0[param1x] : null, -1)
        );
    }

    static Keyable keys(final StringRepresentable[] param0) {
        return new Keyable() {
            @Override
            public <T> Stream<T> keys(DynamicOps<T> param0x) {
                return Arrays.stream(param0).map(StringRepresentable::getSerializedName).map(param0::createString);
            }
        };
    }
}
