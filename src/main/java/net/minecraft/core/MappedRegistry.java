package net.minecraft.core;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.Util;
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
    protected final BiMap<ResourceKey<T>, T> keyStorage = HashBiMap.create();
    protected Object[] randomCache;
    private int nextId;

    public MappedRegistry(ResourceKey<Registry<T>> param0, Lifecycle param1) {
        super(param0, param1);
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
    public ResourceKey<T> getResourceKey(T param0) {
        ResourceKey<T> var0 = this.keyStorage.inverse().get(param0);
        if (var0 == null) {
            throw new IllegalStateException("Unregistered registry element: " + param0 + " in " + this);
        } else {
            return var0;
        }
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
    public Optional<T> getOptional(@Nullable ResourceLocation param0) {
        return Optional.ofNullable(this.storage.get(param0));
    }

    @Override
    public Set<ResourceLocation> keySet() {
        return Collections.unmodifiableSet(this.storage.keySet());
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
    public boolean containsKey(ResourceKey<T> param0) {
        return this.keyStorage.containsKey(param0);
    }

    @Override
    public boolean containsId(int param0) {
        return this.map.contains(param0);
    }

    public static <T> Codec<MappedRegistry<T>> codec(ResourceKey<Registry<T>> param0, Lifecycle param1, Codec<T> param2) {
        return Codec.mapPair(ResourceLocation.CODEC.xmap(ResourceKey.elementKey(param0), ResourceKey::location).fieldOf("key"), param2.fieldOf("element"))
            .codec()
            .listOf()
            .xmap(param2x -> {
                MappedRegistry<T> var0x = new MappedRegistry<>(param0, param1);
    
                for(Pair<ResourceKey<T>, T> var1x : param2x) {
                    var0x.register((ResourceKey<T>)var1x.getFirst(), var1x.getSecond());
                }
    
                return var0x;
            }, param0x -> {
                Builder<Pair<ResourceKey<T>, T>> var0x = ImmutableList.builder();
    
                for(T var1x : param0x) {
                    var0x.add(Pair.of(param0x.getResourceKey((T)var1x), var1x));
                }
    
                return var0x.build();
            });
    }
}
