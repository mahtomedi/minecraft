package net.minecraft.resources;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;

public final class RegistryDataPackCodec<E> implements Codec<MappedRegistry<E>> {
    private final Codec<MappedRegistry<E>> directCodec;
    private final ResourceKey<Registry<E>> registryKey;
    private final Codec<E> elementCodec;

    public static <E> RegistryDataPackCodec<E> create(ResourceKey<Registry<E>> param0, Lifecycle param1, Codec<E> param2) {
        return new RegistryDataPackCodec<>(param0, param1, param2);
    }

    private RegistryDataPackCodec(ResourceKey<Registry<E>> param0, Lifecycle param1, Codec<E> param2) {
        this.directCodec = MappedRegistry.directCodec(param0, param1, param2);
        this.registryKey = param0;
        this.elementCodec = param2;
    }

    public <T> DataResult<T> encode(MappedRegistry<E> param0, DynamicOps<T> param1, T param2) {
        return this.directCodec.encode(param0, param1, param2);
    }

    @Override
    public <T> DataResult<Pair<MappedRegistry<E>, T>> decode(DynamicOps<T> param0, T param1) {
        DataResult<Pair<MappedRegistry<E>, T>> var0 = this.directCodec.decode(param0, param1);
        return param0 instanceof RegistryReadOps
            ? var0.flatMap(
                param1x -> ((RegistryReadOps)param0)
                        .decodeElements(param1x.getFirst(), this.registryKey, this.elementCodec)
                        .map(param1xx -> Pair.of(param1xx, (T)param1x.getSecond()))
            )
            : var0;
    }

    @Override
    public String toString() {
        return "RegistryFileCodec[" + this.registryKey + " " + this.elementCodec + "]";
    }
}
