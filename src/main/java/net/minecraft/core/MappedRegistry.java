package net.minecraft.core;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.resources.RegistryDataPackCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MappedRegistry<T> extends WritableRegistry<T> {
    protected static final Logger LOGGER = LogManager.getLogger();
    private final ObjectList<T> byId = new ObjectArrayList<>(256);
    private final Object2IntMap<T> toId = new Object2IntOpenCustomHashMap<>(Util.identityStrategy());
    private final BiMap<ResourceLocation, T> storage;
    private final BiMap<ResourceKey<T>, T> keyStorage;
    private final Map<T, Lifecycle> lifecycles;
    private Lifecycle elementsLifecycle;
    protected Object[] randomCache;
    private int nextId;

    public MappedRegistry(ResourceKey<? extends Registry<T>> param0, Lifecycle param1) {
        super(param0, param1);
        this.toId.defaultReturnValue(-1);
        this.storage = HashBiMap.create();
        this.keyStorage = HashBiMap.create();
        this.lifecycles = Maps.newIdentityHashMap();
        this.elementsLifecycle = param1;
    }

    public static <T> MapCodec<MappedRegistry.RegistryEntry<T>> withNameAndId(ResourceKey<? extends Registry<T>> param0, MapCodec<T> param1) {
        return RecordCodecBuilder.mapCodec(
            param2 -> param2.group(
                        ResourceLocation.CODEC.xmap(ResourceKey.elementKey(param0), ResourceKey::location).fieldOf("name").forGetter(param0x -> param0x.key),
                        Codec.INT.fieldOf("id").forGetter(param0x -> param0x.id),
                        param1.forGetter(param0x -> param0x.value)
                    )
                    .apply(param2, MappedRegistry.RegistryEntry::new)
        );
    }

    @Override
    public <V extends T> V registerMapping(int param0, ResourceKey<T> param1, V param2, Lifecycle param3) {
        return this.registerMapping(param0, param1, param2, param3, true);
    }

    private <V extends T> V registerMapping(int param0, ResourceKey<T> param1, V param2, Lifecycle param3, boolean param4) {
        Validate.notNull(param1);
        Validate.notNull((T)param2);
        this.byId.size(Math.max(this.byId.size(), param0 + 1));
        this.byId.set(param0, param2);
        this.toId.put((T)param2, param0);
        this.randomCache = null;
        if (param4 && this.keyStorage.containsKey(param1)) {
            LOGGER.debug("Adding duplicate key '{}' to registry", param1);
        }

        if (this.storage.containsValue(param2)) {
            LOGGER.error("Adding duplicate value '{}' to registry", param2);
        }

        this.storage.put(param1.location(), (T)param2);
        this.keyStorage.put(param1, (T)param2);
        this.lifecycles.put((T)param2, param3);
        this.elementsLifecycle = this.elementsLifecycle.add(param3);
        if (this.nextId <= param0) {
            this.nextId = param0 + 1;
        }

        return param2;
    }

    @Override
    public <V extends T> V register(ResourceKey<T> param0, V param1, Lifecycle param2) {
        return this.registerMapping(this.nextId, param0, param1, param2);
    }

    @Override
    public <V extends T> V registerOrOverride(OptionalInt param0, ResourceKey<T> param1, V param2, Lifecycle param3) {
        Validate.notNull(param1);
        Validate.notNull((T)param2);
        T var0 = this.keyStorage.get(param1);
        int var1;
        if (var0 == null) {
            var1 = param0.isPresent() ? param0.getAsInt() : this.nextId;
        } else {
            var1 = this.toId.getInt(var0);
            if (param0.isPresent() && param0.getAsInt() != var1) {
                throw new IllegalStateException("ID mismatch");
            }

            this.toId.removeInt(var0);
            this.lifecycles.remove(var0);
        }

        return this.registerMapping(var1, param1, param2, param3, false);
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
        return this.toId.getInt(param0);
    }

    @Nullable
    @Override
    public T get(@Nullable ResourceKey<T> param0) {
        return this.keyStorage.get(param0);
    }

    @Nullable
    @Override
    public T byId(int param0) {
        return param0 >= 0 && param0 < this.byId.size() ? this.byId.get(param0) : null;
    }

    @Override
    public Lifecycle lifecycle(T param0) {
        return this.lifecycles.get(param0);
    }

    @Override
    public Lifecycle elementsLifecycle() {
        return this.elementsLifecycle;
    }

    @Override
    public Iterator<T> iterator() {
        return Iterators.filter(this.byId.iterator(), Objects::nonNull);
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

    @Override
    public boolean isEmpty() {
        return this.storage.isEmpty();
    }

    @Nullable
    @Override
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

    public static <T> Codec<MappedRegistry<T>> networkCodec(ResourceKey<? extends Registry<T>> param0, Lifecycle param1, Codec<T> param2) {
        return withNameAndId(param0, param2.fieldOf("element")).codec().listOf().xmap(param2x -> {
            MappedRegistry<T> var0x = new MappedRegistry<>(param0, param1);

            for(MappedRegistry.RegistryEntry<T> var1x : param2x) {
                var0x.registerMapping(var1x.id, var1x.key, var1x.value, param1);
            }

            return var0x;
        }, param0x -> {
            Builder<MappedRegistry.RegistryEntry<T>> var0x = ImmutableList.builder();

            for(T var1x : param0x) {
                var0x.add(new MappedRegistry.RegistryEntry<>(param0x.getResourceKey((T)var1x).get(), param0x.getId((T)var1x), (T)var1x));
            }

            return var0x.build();
        });
    }

    public static <T> Codec<MappedRegistry<T>> dataPackCodec(ResourceKey<? extends Registry<T>> param0, Lifecycle param1, Codec<T> param2) {
        return RegistryDataPackCodec.create(param0, param1, param2);
    }

    public static <T> Codec<MappedRegistry<T>> directCodec(ResourceKey<? extends Registry<T>> param0, Lifecycle param1, Codec<T> param2) {
        return Codec.unboundedMap(ResourceLocation.CODEC.xmap(ResourceKey.elementKey(param0), ResourceKey::location), param2).xmap(param2x -> {
            MappedRegistry<T> var0x = new MappedRegistry<>(param0, param1);
            param2x.forEach((param2xx, param3) -> var0x.register(param2xx, param3, param1));
            return var0x;
        }, param0x -> ImmutableMap.copyOf(param0x.keyStorage));
    }

    public static class RegistryEntry<T> {
        public final ResourceKey<T> key;
        public final int id;
        public final T value;

        public RegistryEntry(ResourceKey<T> param0, int param1, T param2) {
            this.key = param0;
            this.id = param1;
            this.value = param2;
        }
    }
}
