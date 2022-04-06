package net.minecraft.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

public record KeyDispatchDataCodec<A>(Codec<A> codec) {
    public static <A> KeyDispatchDataCodec<A> of(Codec<A> param0) {
        return new KeyDispatchDataCodec<>(param0);
    }

    public static <A> KeyDispatchDataCodec<A> of(MapCodec<A> param0) {
        return new KeyDispatchDataCodec<>(param0.codec());
    }
}
