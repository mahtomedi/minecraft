package net.minecraft.resources;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.Registry;

public final class RegistryFixedCodec<E> implements Codec<Holder<E>> {
    private final ResourceKey<? extends Registry<E>> registryKey;

    public static <E> RegistryFixedCodec<E> create(ResourceKey<? extends Registry<E>> param0) {
        return new RegistryFixedCodec<>(param0);
    }

    private RegistryFixedCodec(ResourceKey<? extends Registry<E>> param0) {
        this.registryKey = param0;
    }

    public <T> DataResult<T> encode(Holder<E> param0, DynamicOps<T> param1, T param2) {
        if (param1 instanceof RegistryOps var0) {
            Optional<HolderOwner<E>> var1 = var0.owner(this.registryKey);
            if (var1.isPresent()) {
                if (!param0.canSerializeIn(var1.get())) {
                    return DataResult.error("Element " + param0 + " is not valid in current registry set");
                }

                return param0.unwrap()
                    .map(
                        param2x -> ResourceLocation.CODEC.encode(param2x.location(), param1, param2),
                        param0x -> DataResult.error("Elements from registry " + this.registryKey + " can't be serialized to a value")
                    );
            }
        }

        return DataResult.error("Can't access registry " + this.registryKey);
    }

    @Override
    public <T> DataResult<Pair<Holder<E>, T>> decode(DynamicOps<T> param0, T param1) {
        if (param0 instanceof RegistryOps var0) {
            Optional<HolderGetter<E>> var1 = var0.getter(this.registryKey);
            if (var1.isPresent()) {
                return ResourceLocation.CODEC
                    .decode(param0, param1)
                    .flatMap(
                        param1x -> {
                            ResourceLocation var0x = param1x.getFirst();
                            return var1.get()
                                .get(ResourceKey.create(this.registryKey, var0x))
                                .map(DataResult::success)
                                .orElseGet(() -> DataResult.error("Failed to get element " + var0x))
                                .<Pair<? super Holder.Reference<E>, T>>map(param1xx -> Pair.of(param1xx, (T)param1x.getSecond()))
                                .setLifecycle(Lifecycle.stable());
                        }
                    );
            }
        }

        return DataResult.error("Can't access registry " + this.registryKey);
    }

    @Override
    public String toString() {
        return "RegistryFixedCodec[" + this.registryKey + "]";
    }
}
