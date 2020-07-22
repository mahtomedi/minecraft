package net.minecraft.core;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.resources.RegistryDataPackCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.CrudeIncrementalIntIdentityHashBiMap;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MappedRegistry<T> extends WritableRegistry<T> {
    protected static final Logger LOGGER = LogManager.getLogger();
    protected final CrudeIncrementalIntIdentityHashBiMap<T> map = new CrudeIncrementalIntIdentityHashBiMap<>(256);
    protected final BiMap<ResourceLocation, T> storage = HashBiMap.create();
    private final BiMap<ResourceKey<T>, T> keyStorage = HashBiMap.create();
    private final Set<ResourceKey<T>> persistent = Sets.newIdentityHashSet();
    protected Object[] randomCache;
    private int nextId;

    public MappedRegistry(ResourceKey<? extends Registry<T>> param0, Lifecycle param1) {
        super(param0, param1);
    }

    public static <T> MapCodec<Pair<ResourceKey<T>, T>> withName(ResourceKey<? extends Registry<T>> param0, MapCodec<T> param1) {
        return Codec.mapPair(ResourceLocation.CODEC.xmap(ResourceKey.elementKey(param0), ResourceKey::location).fieldOf("name"), param1);
    }

    public static <T> MapCodec<Pair<Pair<ResourceKey<T>, Integer>, T>> withNameAndId(ResourceKey<? extends Registry<T>> param0, MapCodec<T> param1) {
        return Codec.mapPair(
            Codec.mapPair(ResourceLocation.CODEC.xmap(ResourceKey.elementKey(param0), ResourceKey::location).fieldOf("name"), Codec.INT.fieldOf("id")), param1
        );
    }

    @Override
    public <V extends T> V registerMapping(int param0, ResourceKey<T> param1, V param2) {
        this.map.addMapping((T)param2, param0);
        Validate.notNull(param1);
        Validate.notNull((T)param2);
        this.randomCache = null;
        if (this.keyStorage.containsKey(param1)) {
            LOGGER.debug("Adding duplicate key '{}' to registry", param1);
        }

        if (this.storage.containsValue(param2)) {
            LOGGER.error("Adding duplicate value '{}' to registry", param2);
        }

        this.storage.put(param1.location(), (T)param2);
        this.keyStorage.put(param1, (T)param2);
        if (this.nextId <= param0) {
            this.nextId = param0 + 1;
        }

        return param2;
    }

    @Override
    public <V extends T> V register(ResourceKey<T> param0, V param1) {
        return this.registerMapping(this.nextId, param0, param1);
    }

    @Nullable
    @Override
    public ResourceLocation getKey(T param0) {
        return this.storage.inverse().get(param0);
    }

    @Override
    public Optional<ResourceKey<T>> getResourceKey(T param0) {
        return Optional.ofNullable(this.keyStorage.inverse().get(param0));
    }

    @Override
    public int getId(@Nullable T param0) {
        return this.map.getId(param0);
    }

    @Nullable
    @Override
    public T get(@Nullable ResourceKey<T> param0) {
        return this.keyStorage.get(param0);
    }

    @Nullable
    @Override
    public T byId(int param0) {
        return this.map.byId(param0);
    }

    @Override
    public Iterator<T> iterator() {
        return this.map.iterator();
    }

    @Nullable
    @Override
    public T get(@Nullable ResourceLocation param0) {
        return this.storage.get(param0);
    }

    @Override
    public Set<ResourceLocation> keySet() {
        return Collections.unmodifiableSet(this.storage.keySet());
    }

    @Override
    public Set<Entry<ResourceKey<T>, T>> entrySet() {
        return Collections.unmodifiableMap(this.keyStorage).entrySet();
    }

    @Nullable
    public T getRandom(Random param0) {
        if (this.randomCache == null) {
            Collection<?> var0 = this.storage.values();
            if (var0.isEmpty()) {
                return null;
            }

            this.randomCache = var0.toArray(new Object[var0.size()]);
        }

        return Util.getRandom((T[])this.randomCache, param0);
    }

    @Override
    public boolean containsKey(ResourceLocation param0) {
        return this.storage.containsKey(param0);
    }

    @Override
    public boolean containsId(int param0) {
        return this.map.contains(param0);
    }

    @Override
    public boolean persistent(ResourceKey<T> param0) {
        return this.persistent.contains(param0);
    }

    @Override
    public void setPersistent(ResourceKey<T> param0) {
        this.persistent.add(param0);
    }

    public static <T> Codec<MappedRegistry<T>> networkCodec(ResourceKey<? extends Registry<T>> param0, Lifecycle param1, MapCodec<T> param2) {
        return withNameAndId(param0, param2).codec().listOf().xmap(param2x -> {
            MappedRegistry<T> var0x = new MappedRegistry<>(param0, param1);

            for(Pair<Pair<ResourceKey<T>, Integer>, T> var1x : param2x) {
                var0x.registerMapping(((Pair)var1x.getFirst()).getSecond(), (ResourceKey<T>)((Pair)var1x.getFirst()).getFirst(), var1x.getSecond());
            }

            return var0x;
        }, param0x -> {
            com.google.common.collect.ImmutableList.Builder<Pair<Pair<ResourceKey<T>, Integer>, T>> var0x = ImmutableList.builder();

            for(T var1x : param0x.map) {
                var0x.add(Pair.of(Pair.of(param0x.getResourceKey((T)var1x).get(), param0x.getId((T)var1x)), var1x));
            }

            return var0x.build();
        });
    }

    public static <T> Codec<MappedRegistry<T>> dataPackCodec(ResourceKey<? extends Registry<T>> param0, Lifecycle param1, MapCodec<T> param2) {
        return RegistryDataPackCodec.create(param0, param1, param2);
    }

    public static <T> Codec<MappedRegistry<T>> directCodec(ResourceKey<? extends Registry<T>> param0, Lifecycle param1, MapCodec<T> param2) {
        return Codec.unboundedMap(ResourceLocation.CODEC.xmap(ResourceKey.elementKey(param0), ResourceKey::location), param2.codec()).xmap(param2x -> {
            MappedRegistry<T> var0x = new MappedRegistry<>(param0, param1);
            param2x.forEach((param1x, param2xx) -> {
                var0x.registerMapping(var0x.nextId, param1x, param2xx);
                var0x.setPersistent(param1x);
            });
            return var0x;
        }, param0x -> {
            Builder<ResourceKey<T>, T> var0x = ImmutableMap.builder();
            param0x.keyStorage.entrySet().stream().filter(param1x -> param0x.persistent(param1x.getKey())).forEach(var0x::put);
            return var0x.build();
        });
    }
}
