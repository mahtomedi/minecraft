package net.minecraft.core;

import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;

public class MappedRegistry<T> implements WritableRegistry<T> {
    private static final Logger LOGGER = LogUtils.getLogger();
    final ResourceKey<? extends Registry<T>> key;
    private final ObjectList<Holder.Reference<T>> byId = new ObjectArrayList<>(256);
    private final Object2IntMap<T> toId = Util.make(new Object2IntOpenCustomHashMap<>(Util.identityStrategy()), param0x -> param0x.defaultReturnValue(-1));
    private final Map<ResourceLocation, Holder.Reference<T>> byLocation = new HashMap<>();
    private final Map<ResourceKey<T>, Holder.Reference<T>> byKey = new HashMap<>();
    private final Map<T, Holder.Reference<T>> byValue = new IdentityHashMap<>();
    private final Map<T, Lifecycle> lifecycles = new IdentityHashMap<>();
    private Lifecycle registryLifecycle;
    private volatile Map<TagKey<T>, HolderSet.Named<T>> tags = new IdentityHashMap<>();
    private boolean frozen;
    @Nullable
    private Map<T, Holder.Reference<T>> unregisteredIntrusiveHolders;
    @Nullable
    private List<Holder.Reference<T>> holdersInOrder;
    private int nextId;
    private final HolderLookup.RegistryLookup<T> lookup = new HolderLookup.RegistryLookup<T>() {
        @Override
        public ResourceKey<? extends Registry<? extends T>> key() {
            return MappedRegistry.this.key;
        }

        @Override
        public Lifecycle registryLifecycle() {
            return MappedRegistry.this.registryLifecycle();
        }

        @Override
        public Optional<Holder.Reference<T>> get(ResourceKey<T> param0) {
            return MappedRegistry.this.getHolder(param0);
        }

        @Override
        public Stream<Holder.Reference<T>> listElements() {
            return MappedRegistry.this.holders();
        }

        @Override
        public Optional<HolderSet.Named<T>> get(TagKey<T> param0) {
            return MappedRegistry.this.getTag(param0);
        }

        @Override
        public Stream<HolderSet.Named<T>> listTags() {
            return MappedRegistry.this.getTags().map(Pair::getSecond);
        }
    };

    public MappedRegistry(ResourceKey<? extends Registry<T>> param0, Lifecycle param1) {
        this(param0, param1, false);
    }

    public MappedRegistry(ResourceKey<? extends Registry<T>> param0, Lifecycle param1, boolean param2) {
        Bootstrap.checkBootstrapCalled(() -> "registry " + param0);
        this.key = param0;
        this.registryLifecycle = param1;
        if (param2) {
            this.unregisteredIntrusiveHolders = new IdentityHashMap<>();
        }

    }

    @Override
    public ResourceKey<? extends Registry<T>> key() {
        return this.key;
    }

    @Override
    public String toString() {
        return "Registry[" + this.key + " (" + this.registryLifecycle + ")]";
    }

    private List<Holder.Reference<T>> holdersInOrder() {
        if (this.holdersInOrder == null) {
            this.holdersInOrder = this.byId.stream().filter(Objects::nonNull).toList();
        }

        return this.holdersInOrder;
    }

    private void validateWrite() {
        if (this.frozen) {
            throw new IllegalStateException("Registry is already frozen");
        }
    }

    private void validateWrite(ResourceKey<T> param0) {
        if (this.frozen) {
            throw new IllegalStateException("Registry is already frozen (trying to add key " + param0 + ")");
        }
    }

    public Holder.Reference<T> registerMapping(int param0, ResourceKey<T> param1, T param2, Lifecycle param3) {
        this.validateWrite(param1);
        Validate.notNull(param1);
        Validate.notNull(param2);
        if (this.byLocation.containsKey(param1.location())) {
            Util.pauseInIde(new IllegalStateException("Adding duplicate key '" + param1 + "' to registry"));
        }

        if (this.byValue.containsKey(param2)) {
            Util.pauseInIde(new IllegalStateException("Adding duplicate value '" + param2 + "' to registry"));
        }

        Holder.Reference<T> var0;
        if (this.unregisteredIntrusiveHolders != null) {
            var0 = this.unregisteredIntrusiveHolders.remove(param2);
            if (var0 == null) {
                throw new AssertionError("Missing intrusive holder for " + param1 + ":" + param2);
            }

            var0.bindKey(param1);
        } else {
            var0 = this.byKey.computeIfAbsent(param1, param0x -> Holder.Reference.createStandAlone(this.holderOwner(), param0x));
        }

        this.byKey.put(param1, var0);
        this.byLocation.put(param1.location(), var0);
        this.byValue.put(param2, var0);
        this.byId.size(Math.max(this.byId.size(), param0 + 1));
        this.byId.set(param0, var0);
        this.toId.put(param2, param0);
        if (this.nextId <= param0) {
            this.nextId = param0 + 1;
        }

        this.lifecycles.put(param2, param3);
        this.registryLifecycle = this.registryLifecycle.add(param3);
        this.holdersInOrder = null;
        return var0;
    }

    @Override
    public Holder.Reference<T> register(ResourceKey<T> param0, T param1, Lifecycle param2) {
        return this.registerMapping(this.nextId, param0, param1, param2);
    }

    @Nullable
    @Override
    public ResourceLocation getKey(T param0) {
        Holder.Reference<T> var0 = this.byValue.get(param0);
        return var0 != null ? var0.key().location() : null;
    }

    @Override
    public Optional<ResourceKey<T>> getResourceKey(T param0) {
        return Optional.ofNullable(this.byValue.get(param0)).map(Holder.Reference::key);
    }

    @Override
    public int getId(@Nullable T param0) {
        return this.toId.getInt(param0);
    }

    @Nullable
    @Override
    public T get(@Nullable ResourceKey<T> param0) {
        return getValueFromNullable(this.byKey.get(param0));
    }

    @Nullable
    @Override
    public T byId(int param0) {
        return param0 >= 0 && param0 < this.byId.size() ? getValueFromNullable(this.byId.get(param0)) : null;
    }

    @Override
    public Optional<Holder.Reference<T>> getHolder(int param0) {
        return param0 >= 0 && param0 < this.byId.size() ? Optional.ofNullable(this.byId.get(param0)) : Optional.empty();
    }

    @Override
    public Optional<Holder.Reference<T>> getHolder(ResourceKey<T> param0) {
        return Optional.ofNullable(this.byKey.get(param0));
    }

    @Override
    public Holder<T> wrapAsHolder(T param0) {
        Holder.Reference<T> var0 = this.byValue.get(param0);
        return (Holder<T>)(var0 != null ? var0 : Holder.direct(param0));
    }

    Holder.Reference<T> getOrCreateHolderOrThrow(ResourceKey<T> param0) {
        return this.byKey.computeIfAbsent(param0, param0x -> {
            if (this.unregisteredIntrusiveHolders != null) {
                throw new IllegalStateException("This registry can't create new holders without value");
            } else {
                this.validateWrite(param0x);
                return Holder.Reference.createStandAlone(this.holderOwner(), param0x);
            }
        });
    }

    @Override
    public int size() {
        return this.byKey.size();
    }

    @Override
    public Lifecycle lifecycle(T param0) {
        return this.lifecycles.get(param0);
    }

    @Override
    public Lifecycle registryLifecycle() {
        return this.registryLifecycle;
    }

    @Override
    public Iterator<T> iterator() {
        return Iterators.transform(this.holdersInOrder().iterator(), Holder::value);
    }

    @Nullable
    @Override
    public T get(@Nullable ResourceLocation param0) {
        Holder.Reference<T> var0 = this.byLocation.get(param0);
        return getValueFromNullable(var0);
    }

    @Nullable
    private static <T> T getValueFromNullable(@Nullable Holder.Reference<T> param0) {
        return param0 != null ? param0.value() : null;
    }

    @Override
    public Set<ResourceLocation> keySet() {
        return Collections.unmodifiableSet(this.byLocation.keySet());
    }

    @Override
    public Set<ResourceKey<T>> registryKeySet() {
        return Collections.unmodifiableSet(this.byKey.keySet());
    }

    @Override
    public Set<Entry<ResourceKey<T>, T>> entrySet() {
        return Collections.unmodifiableSet(Maps.transformValues(this.byKey, Holder::value).entrySet());
    }

    @Override
    public Stream<Holder.Reference<T>> holders() {
        return this.holdersInOrder().stream();
    }

    @Override
    public Stream<Pair<TagKey<T>, HolderSet.Named<T>>> getTags() {
        return this.tags.entrySet().stream().map(param0 -> Pair.of((TagKey)param0.getKey(), (HolderSet.Named<T>)param0.getValue()));
    }

    @Override
    public HolderSet.Named<T> getOrCreateTag(TagKey<T> param0) {
        HolderSet.Named<T> var0 = this.tags.get(param0);
        if (var0 == null) {
            var0 = this.createTag(param0);
            Map<TagKey<T>, HolderSet.Named<T>> var1 = new IdentityHashMap<>(this.tags);
            var1.put(param0, var0);
            this.tags = var1;
        }

        return var0;
    }

    private HolderSet.Named<T> createTag(TagKey<T> param0) {
        return new HolderSet.Named<>(this.holderOwner(), param0);
    }

    @Override
    public Stream<TagKey<T>> getTagNames() {
        return this.tags.keySet().stream();
    }

    @Override
    public boolean isEmpty() {
        return this.byKey.isEmpty();
    }

    @Override
    public Optional<Holder.Reference<T>> getRandom(RandomSource param0) {
        return Util.getRandomSafe(this.holdersInOrder(), param0);
    }

    @Override
    public boolean containsKey(ResourceLocation param0) {
        return this.byLocation.containsKey(param0);
    }

    @Override
    public boolean containsKey(ResourceKey<T> param0) {
        return this.byKey.containsKey(param0);
    }

    @Override
    public Registry<T> freeze() {
        if (this.frozen) {
            return this;
        } else {
            this.frozen = true;
            this.byValue.forEach((param0, param1) -> param1.bindValue(param0));
            List<ResourceLocation> var0 = this.byKey
                .entrySet()
                .stream()
                .filter(param0 -> !param0.getValue().isBound())
                .map(param0 -> param0.getKey().location())
                .sorted()
                .toList();
            if (!var0.isEmpty()) {
                throw new IllegalStateException("Unbound values in registry " + this.key() + ": " + var0);
            } else {
                if (this.unregisteredIntrusiveHolders != null) {
                    if (!this.unregisteredIntrusiveHolders.isEmpty()) {
                        throw new IllegalStateException("Some intrusive holders were not registered: " + this.unregisteredIntrusiveHolders.values());
                    }

                    this.unregisteredIntrusiveHolders = null;
                }

                return this;
            }
        }
    }

    @Override
    public Holder.Reference<T> createIntrusiveHolder(T param0) {
        if (this.unregisteredIntrusiveHolders == null) {
            throw new IllegalStateException("This registry can't create intrusive holders");
        } else {
            this.validateWrite();
            return this.unregisteredIntrusiveHolders.computeIfAbsent(param0, param0x -> Holder.Reference.createIntrusive(this.asLookup(), param0x));
        }
    }

    @Override
    public Optional<HolderSet.Named<T>> getTag(TagKey<T> param0) {
        return Optional.ofNullable(this.tags.get(param0));
    }

    @Override
    public void bindTags(Map<TagKey<T>, List<Holder<T>>> param0) {
        Map<Holder.Reference<T>, List<TagKey<T>>> var0 = new IdentityHashMap<>();
        this.byKey.values().forEach(param1 -> var0.put(param1, new ArrayList()));
        param0.forEach((param1, param2) -> {
            for(Holder<T> var0x : param2) {
                if (!var0x.canSerializeIn(this.asLookup())) {
                    throw new IllegalStateException("Can't create named set " + param1 + " containing value " + var0x + " from outside registry " + this);
                }

                if (!(var0x instanceof Holder.Reference)) {
                    throw new IllegalStateException("Found direct holder " + var0x + " value in tag " + param1);
                }

                Holder.Reference<T> var1x = (Holder.Reference)var0x;
                var0.get(var1x).add(param1);
            }

        });
        Set<TagKey<T>> var1 = Sets.difference(this.tags.keySet(), param0.keySet());
        if (!var1.isEmpty()) {
            LOGGER.warn(
                "Not all defined tags for registry {} are present in data pack: {}",
                this.key(),
                var1.stream().map(param0x -> param0x.location().toString()).sorted().collect(Collectors.joining(", "))
            );
        }

        Map<TagKey<T>, HolderSet.Named<T>> var2 = new IdentityHashMap<>(this.tags);
        param0.forEach((param1, param2) -> var2.computeIfAbsent(param1, this::createTag).bind(param2));
        var0.forEach(Holder.Reference::bindTags);
        this.tags = var2;
    }

    @Override
    public void resetTags() {
        this.tags.values().forEach(param0 -> param0.bind(List.of()));
        this.byKey.values().forEach(param0 -> param0.bindTags(Set.of()));
    }

    @Override
    public HolderGetter<T> createRegistrationLookup() {
        this.validateWrite();
        return new HolderGetter<T>() {
            @Override
            public Optional<Holder.Reference<T>> get(ResourceKey<T> param0) {
                return Optional.of(this.getOrThrow(param0));
            }

            @Override
            public Holder.Reference<T> getOrThrow(ResourceKey<T> param0) {
                return MappedRegistry.this.getOrCreateHolderOrThrow(param0);
            }

            @Override
            public Optional<HolderSet.Named<T>> get(TagKey<T> param0) {
                return Optional.of(this.getOrThrow(param0));
            }

            @Override
            public HolderSet.Named<T> getOrThrow(TagKey<T> param0) {
                return MappedRegistry.this.getOrCreateTag(param0);
            }
        };
    }

    @Override
    public HolderOwner<T> holderOwner() {
        return this.lookup;
    }

    @Override
    public HolderLookup.RegistryLookup<T> asLookup() {
        return this.lookup;
    }
}
