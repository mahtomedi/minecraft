package net.minecraft.resources;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import java.util.stream.Stream;
import net.minecraft.core.Registry;

public final class RegistryLookupCodec<E> extends MapCodec<Registry<E>> {
    private final ResourceKey<? extends Registry<E>> registryKey;

    public static <E> RegistryLookupCodec<E> create(ResourceKey<? extends Registry<E>> param0) {
        return new RegistryLookupCodec<>(param0);
    }

    private RegistryLookupCodec(ResourceKey<? extends Registry<E>> param0) {
        this.registryKey = param0;
    }

    public <T> RecordBuilder<T> encode(Registry<E> param0, DynamicOps<T> param1, RecordBuilder<T> param2) {
        return param2;
    }

    @Override
    public <T> DataResult<Registry<E>> decode(DynamicOps<T> param0, MapLike<T> param1) {
        return param0 instanceof RegistryReadOps ? ((RegistryReadOps)param0).registry(this.registryKey) : DataResult.error("Not a registry ops");
    }

    @Override
    public String toString() {
        return "RegistryLookupCodec[" + this.registryKey + "]";
    }

    @Override
    public <T> Stream<T> keys(DynamicOps<T> param0) {
        return Stream.empty();
    }
}
