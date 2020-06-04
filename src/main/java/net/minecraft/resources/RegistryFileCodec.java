package net.minecraft.resources;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import java.util.function.Supplier;
import net.minecraft.core.Registry;

public final class RegistryFileCodec<E> implements Codec<Supplier<E>> {
    private final ResourceKey<Registry<E>> registryKey;
    private final Codec<E> elementCodec;

    public static <E> RegistryFileCodec<E> create(ResourceKey<Registry<E>> param0, Codec<E> param1) {
        return new RegistryFileCodec<>(param0, param1);
    }

    private RegistryFileCodec(ResourceKey<Registry<E>> param0, Codec<E> param1) {
        this.registryKey = param0;
        this.elementCodec = param1;
    }

    public <T> DataResult<T> encode(Supplier<E> param0, DynamicOps<T> param1, T param2) {
        return param1 instanceof RegistryWriteOps
            ? ((RegistryWriteOps)param1).encode(param0.get(), param2, this.registryKey, this.elementCodec)
            : this.elementCodec.encode(param0.get(), param1, param2);
    }

    @Override
    public <T> DataResult<Pair<Supplier<E>, T>> decode(DynamicOps<T> param0, T param1) {
        return param0 instanceof RegistryReadOps
            ? ((RegistryReadOps)param0).decodeElement(param1, this.registryKey, this.elementCodec)
            : this.elementCodec.decode(param0, param1).map(param0x -> param0x.mapFirst(param0xx -> () -> param0xx));
    }

    @Override
    public String toString() {
        return "RegistryFileCodec[" + this.registryKey + " " + this.elementCodec + "]";
    }
}
