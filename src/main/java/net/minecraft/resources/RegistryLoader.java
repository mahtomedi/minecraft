package net.minecraft.resources;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;

public class RegistryLoader {
    private final RegistryResourceAccess resources;
    private final Map<ResourceKey<? extends Registry<?>>, RegistryLoader.ReadCache<?>> readCache = new IdentityHashMap<>();

    RegistryLoader(RegistryResourceAccess param0) {
        this.resources = param0;
    }

    public <E> DataResult<? extends Registry<E>> overrideRegistryFromResources(
        WritableRegistry<E> param0, ResourceKey<? extends Registry<E>> param1, Codec<E> param2, DynamicOps<JsonElement> param3
    ) {
        Collection<ResourceKey<E>> var0 = this.resources.listResources(param1);
        DataResult<WritableRegistry<E>> var1 = DataResult.success(param0, Lifecycle.stable());

        for(ResourceKey<E> var2 : var0) {
            var1 = var1.flatMap(param4 -> this.overrideElementFromResources(param4, param1, param2, var2, param3).map(param1x -> param4));
        }

        return var1.setPartial(param0);
    }

    <E> DataResult<Holder<E>> overrideElementFromResources(
        WritableRegistry<E> param0, ResourceKey<? extends Registry<E>> param1, Codec<E> param2, ResourceKey<E> param3, DynamicOps<JsonElement> param4
    ) {
        RegistryLoader.ReadCache<E> var0 = this.readCache(param1);
        DataResult<Holder<E>> var1 = var0.values.get(param3);
        if (var1 != null) {
            return var1;
        } else {
            Holder<E> var2 = param0.getOrCreateHolder(param3);
            var0.values.put(param3, DataResult.success(var2));
            Optional<DataResult<RegistryResourceAccess.ParsedEntry<E>>> var3 = this.resources.parseElement(param4, param1, param3, param2);
            DataResult<Holder<E>> var4;
            if (var3.isEmpty()) {
                if (param0.containsKey(param3)) {
                    var4 = DataResult.success(var2, Lifecycle.stable());
                } else {
                    var4 = DataResult.error("Missing referenced custom/removed registry entry for registry " + param1 + " named " + param3.location());
                }
            } else {
                DataResult<RegistryResourceAccess.ParsedEntry<E>> var6 = var3.get();
                Optional<RegistryResourceAccess.ParsedEntry<E>> var7 = var6.result();
                if (var7.isPresent()) {
                    RegistryResourceAccess.ParsedEntry<E> var8 = var7.get();
                    param0.registerOrOverride(var8.fixedId(), param3, var8.value(), var6.lifecycle());
                }

                var4 = var6.map(param1x -> var2);
            }

            var0.values.put(param3, var4);
            return var4;
        }
    }

    private <E> RegistryLoader.ReadCache<E> readCache(ResourceKey<? extends Registry<E>> param0) {
        return (RegistryLoader.ReadCache<E>)this.readCache.computeIfAbsent(param0, param0x -> new RegistryLoader.ReadCache());
    }

    public RegistryLoader.Bound bind(RegistryAccess.Writable param0) {
        return new RegistryLoader.Bound(param0, this);
    }

    public static record Bound<E>(RegistryAccess.Writable access, RegistryLoader loader) {
        public <E> DataResult<? extends Registry<E>> overrideRegistryFromResources(
            ResourceKey<? extends Registry<E>> param0, Codec<E> param1, DynamicOps<JsonElement> param2
        ) {
            WritableRegistry<E> var0 = this.access.ownedWritableRegistryOrThrow(param0);
            return this.loader.overrideRegistryFromResources(var0, param0, param1, param2);
        }

        public <E> DataResult<Holder<E>> overrideElementFromResources(
            ResourceKey<? extends Registry<E>> param0, Codec<E> param1, ResourceKey<E> param2, DynamicOps<JsonElement> param3
        ) {
            WritableRegistry<E> var0 = this.access.ownedWritableRegistryOrThrow(param0);
            return this.loader.overrideElementFromResources(var0, param0, param1, param2, param3);
        }
    }

    static final class ReadCache<E> {
        final Map<ResourceKey<E>, DataResult<Holder<E>>> values = Maps.newIdentityHashMap();
    }
}
