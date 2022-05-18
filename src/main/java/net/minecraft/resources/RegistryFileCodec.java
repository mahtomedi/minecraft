package net.minecraft.resources;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;

public final class RegistryFileCodec<E> implements Codec<Holder<E>> {
    private final ResourceKey<? extends Registry<E>> registryKey;
    private final Codec<E> elementCodec;
    private final boolean allowInline;

    public static <E> RegistryFileCodec<E> create(ResourceKey<? extends Registry<E>> param0, Codec<E> param1) {
        return create(param0, param1, true);
    }

    public static <E> RegistryFileCodec<E> create(ResourceKey<? extends Registry<E>> param0, Codec<E> param1, boolean param2) {
        return new RegistryFileCodec<>(param0, param1, param2);
    }

    private RegistryFileCodec(ResourceKey<? extends Registry<E>> param0, Codec<E> param1, boolean param2) {
        this.registryKey = param0;
        this.elementCodec = param1;
        this.allowInline = param2;
    }

    public <T> DataResult<T> encode(Holder<E> param0, DynamicOps<T> param1, T param2) {
        if (param1 instanceof RegistryOps var0) {
            Optional<? extends Registry<E>> var1 = var0.registry(this.registryKey);
            if (var1.isPresent()) {
                if (!param0.isValidInRegistry(var1.get())) {
                    return DataResult.error("Element " + param0 + " is not valid in current registry set");
                }

                return param0.unwrap()
                    .map(
                        param2x -> ResourceLocation.CODEC.encode(param2x.location(), param1, param2),
                        param2x -> this.elementCodec.encode(param2x, param1, param2)
                    );
            }
        }

        return this.elementCodec.encode(param0.value(), param1, param2);
    }

    @Override
    public <T> DataResult<Pair<Holder<E>, T>> decode(DynamicOps<T> param0, T param1) {
        if (param0 instanceof RegistryOps var0) {
            Optional<? extends Registry<E>> var1 = var0.registry(this.registryKey);
            if (var1.isEmpty()) {
                return DataResult.error("Registry does not exist: " + this.registryKey);
            } else {
                Registry<E> var2 = var1.get();
                DataResult<Pair<ResourceLocation, T>> var3 = ResourceLocation.CODEC.decode(param0, param1);
                if (var3.result().isEmpty()) {
                    return !this.allowInline
                        ? DataResult.error("Inline definitions not allowed here")
                        : this.elementCodec.decode(param0, param1).map(param0x -> param0x.mapFirst(Holder::direct));
                } else {
                    Pair<ResourceLocation, T> var4 = var3.result().get();
                    ResourceKey<E> var5 = ResourceKey.create(this.registryKey, var4.getFirst());
                    Optional<RegistryLoader.Bound> var6 = var0.registryLoader();
                    if (var6.isPresent()) {
                        return var6.get()
                            .overrideElementFromResources(this.registryKey, this.elementCodec, var5, var0.getAsJson())
                            .map(param1x -> Pair.of(param1x, var4.getSecond()));
                    } else {
                        DataResult<Holder<E>> var7 = var2.getOrCreateHolder(var5);
                        return var7.<Pair<Holder<E>, T>>map(param1x -> Pair.of(param1x, var4.getSecond())).setLifecycle(Lifecycle.stable());
                    }
                }
            }
        } else {
            return this.elementCodec.decode(param0, param1).map(param0x -> param0x.mapFirst(Holder::direct));
        }
    }

    @Override
    public String toString() {
        return "RegistryFileCodec[" + this.registryKey + " " + this.elementCodec + "]";
    }
}
