package net.minecraft.resources;

import com.google.common.base.Suppliers;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.server.packs.resources.ResourceManager;

public class RegistryReadOps<T> extends DelegatingOps<T> {
    private final RegistryResourceAccess resources;
    private final RegistryAccess registryAccess;
    private final Map<ResourceKey<? extends Registry<?>>, RegistryReadOps.ReadCache<?>> readCache;
    private final RegistryReadOps<JsonElement> jsonOps;

    public static <T> RegistryReadOps<T> createAndLoad(DynamicOps<T> param0, ResourceManager param1, RegistryAccess param2) {
        return createAndLoad(param0, RegistryResourceAccess.forResourceManager(param1), param2);
    }

    public static <T> RegistryReadOps<T> createAndLoad(DynamicOps<T> param0, RegistryResourceAccess param1, RegistryAccess param2) {
        RegistryReadOps<T> var0 = new RegistryReadOps<>(param0, param1, param2, Maps.newIdentityHashMap());
        RegistryAccess.load(param2, var0);
        return var0;
    }

    public static <T> RegistryReadOps<T> create(DynamicOps<T> param0, ResourceManager param1, RegistryAccess param2) {
        return create(param0, RegistryResourceAccess.forResourceManager(param1), param2);
    }

    public static <T> RegistryReadOps<T> create(DynamicOps<T> param0, RegistryResourceAccess param1, RegistryAccess param2) {
        return new RegistryReadOps<>(param0, param1, param2, Maps.newIdentityHashMap());
    }

    private RegistryReadOps(
        DynamicOps<T> param0,
        RegistryResourceAccess param1,
        RegistryAccess param2,
        IdentityHashMap<ResourceKey<? extends Registry<?>>, RegistryReadOps.ReadCache<?>> param3
    ) {
        super(param0);
        this.resources = param1;
        this.registryAccess = param2;
        this.readCache = param3;
        this.jsonOps = param0 == JsonOps.INSTANCE ? this : new RegistryReadOps<>(JsonOps.INSTANCE, param1, param2, param3);
    }

    protected <E> DataResult<Pair<Supplier<E>, T>> decodeElement(T param0, ResourceKey<? extends Registry<E>> param1, Codec<E> param2, boolean param3) {
        Optional<WritableRegistry<E>> var0 = this.registryAccess.ownedRegistry(param1);
        if (!var0.isPresent()) {
            return DataResult.error("Unknown registry: " + param1);
        } else {
            WritableRegistry<E> var1 = var0.get();
            DataResult<Pair<ResourceLocation, T>> var2 = ResourceLocation.CODEC.decode(this.delegate, param0);
            if (!var2.result().isPresent()) {
                return !param3
                    ? DataResult.error("Inline definitions not allowed here")
                    : param2.decode(this, param0).map(param0x -> param0x.mapFirst(param0xx -> () -> param0xx));
            } else {
                Pair<ResourceLocation, T> var3 = var2.result().get();
                ResourceKey<E> var4 = ResourceKey.create(param1, var3.getFirst());
                return this.readAndRegisterElement(param1, var1, param2, var4).map(param1x -> Pair.of(param1x, var3.getSecond()));
            }
        }
    }

    public <E> DataResult<MappedRegistry<E>> decodeElements(MappedRegistry<E> param0, ResourceKey<? extends Registry<E>> param1, Codec<E> param2) {
        Collection<ResourceKey<E>> var0 = this.resources.listResources(param1);
        DataResult<MappedRegistry<E>> var1 = DataResult.success(param0, Lifecycle.stable());

        for(ResourceKey<E> var2 : var0) {
            var1 = var1.flatMap(param3 -> this.readAndRegisterElement(param1, param3, param2, var2).map(param1x -> param3));
        }

        return var1.setPartial(param0);
    }

    private <E> DataResult<Supplier<E>> readAndRegisterElement(
        ResourceKey<? extends Registry<E>> param0, WritableRegistry<E> param1, Codec<E> param2, ResourceKey<E> param3
    ) {
        RegistryReadOps.ReadCache<E> var0 = this.readCache(param0);
        DataResult<Supplier<E>> var1 = var0.values.get(param3);
        if (var1 != null) {
            return var1;
        } else {
            var0.values.put(param3, DataResult.success(createPlaceholderGetter(param1, param3)));
            Optional<DataResult<RegistryResourceAccess.ParsedEntry<E>>> var2 = this.resources.parseElement(this.jsonOps, param0, param3, param2);
            DataResult<Supplier<E>> var3;
            if (var2.isEmpty()) {
                var3 = DataResult.success(createRegistryGetter(param1, param3), Lifecycle.stable());
            } else {
                DataResult<RegistryResourceAccess.ParsedEntry<E>> var4 = var2.get();
                Optional<RegistryResourceAccess.ParsedEntry<E>> var5 = var4.result();
                if (var5.isPresent()) {
                    RegistryResourceAccess.ParsedEntry<E> var6 = var5.get();
                    param1.registerOrOverride(var6.fixedId(), param3, var6.value(), var4.lifecycle());
                }

                var3 = var4.map(param2x -> createRegistryGetter(param1, param3));
            }

            var0.values.put(param3, var3);
            return var3;
        }
    }

    private static <E> Supplier<E> createPlaceholderGetter(WritableRegistry<E> param0, ResourceKey<E> param1) {
        return Suppliers.memoize(() -> {
            E var0x = param0.get(param1);
            if (var0x == null) {
                throw new RuntimeException("Error during recursive registry parsing, element resolved too early: " + param1);
            } else {
                return (E)var0x;
            }
        });
    }

    private static <E> Supplier<E> createRegistryGetter(final Registry<E> param0, final ResourceKey<E> param1) {
        return new Supplier<E>() {
            @Override
            public E get() {
                return param0.get(param1);
            }

            @Override
            public String toString() {
                return param1.toString();
            }
        };
    }

    private <E> RegistryReadOps.ReadCache<E> readCache(ResourceKey<? extends Registry<E>> param0) {
        return (RegistryReadOps.ReadCache<E>)this.readCache.computeIfAbsent(param0, param0x -> new RegistryReadOps.ReadCache());
    }

    protected <E> DataResult<Registry<E>> registry(ResourceKey<? extends Registry<E>> param0) {
        return this.registryAccess
            .ownedRegistry(param0)
            .map(param0x -> DataResult.success(param0x, param0x.elementsLifecycle()))
            .orElseGet(() -> DataResult.error("Unknown registry: " + param0));
    }

    static final class ReadCache<E> {
        final Map<ResourceKey<E>, DataResult<Supplier<E>>> values = Maps.newIdentityHashMap();
    }
}
