package net.minecraft.resources;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.Registry;

public final class RegistryFileCodec<E> implements Codec<Supplier<E>> {
    private final ResourceKey<? extends Registry<E>> registryKey;
    private final Codec<E> elementCodec;
    private final boolean allowInline;

    public static <E> RegistryFileCodec<E> create(ResourceKey<? extends Registry<E>> param0, Codec<E> param1) {
        return create(param0, param1, true);
    }

    public static <E> Codec<List<Supplier<E>>> homogeneousList(ResourceKey<? extends Registry<E>> param0, Codec<E> param1) {
        return Codec.either(create(param0, param1, false).listOf(), param1.<Supplier<>>xmap(param0x -> () -> param0x, Supplier::get).listOf())
            .xmap(
                param0x -> param0x.map(
                        (Function<? super List, ? extends List<Supplier<E>>>)(param0xx -> param0xx),
                        (Function<? super List, ? extends List<Supplier<E>>>)(param0xx -> param0xx)
                    ),
                Either::left
            );
    }

    private static <E> RegistryFileCodec<E> create(ResourceKey<? extends Registry<E>> param0, Codec<E> param1, boolean param2) {
        return new RegistryFileCodec<>(param0, param1, param2);
    }

    private RegistryFileCodec(ResourceKey<? extends Registry<E>> param0, Codec<E> param1, boolean param2) {
        this.registryKey = param0;
        this.elementCodec = param1;
        this.allowInline = param2;
    }

    public <T> DataResult<T> encode(Supplier<E> param0, DynamicOps<T> param1, T param2) {
        return param1 instanceof RegistryWriteOps
            ? ((RegistryWriteOps)param1).encode(param0.get(), param2, this.registryKey, this.elementCodec)
            : this.elementCodec.encode(param0.get(), param1, param2);
    }

    @Override
    public <T> DataResult<Pair<Supplier<E>, T>> decode(DynamicOps<T> param0, T param1) {
        return param0 instanceof RegistryReadOps
            ? ((RegistryReadOps)param0).decodeElement(param1, this.registryKey, this.elementCodec, this.allowInline)
            : this.elementCodec.decode(param0, param1).map(param0x -> param0x.mapFirst(param0xx -> () -> param0xx));
    }

    @Override
    public String toString() {
        return "RegistryFileCodec[" + this.registryKey + " " + this.elementCodec + "]";
    }
}
