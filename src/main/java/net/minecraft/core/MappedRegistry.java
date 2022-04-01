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
import java.util.OptionalInt;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;

public class MappedRegistry<T> extends WritableRegistry<T> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final ObjectList<Holder.Reference<T>> byId = new ObjectArrayList<>(256);
    private final Object2IntMap<T> toId = Util.make(new Object2IntOpenCustomHashMap<>(Util.identityStrategy()), param0x -> param0x.defaultReturnValue(-1));
    private final Map<ResourceLocation, Holder.Reference<T>> byLocation = new HashMap<>();
    private final Map<ResourceKey<T>, Holder.Reference<T>> byKey = new HashMap<>();
    private final Map<T, Holder.Reference<T>> byValue = new IdentityHashMap<>();
    private final Map<T, Lifecycle> lifecycles = new IdentityHashMap<>();
    private Lifecycle elementsLifecycle;
    private volatile Map<TagKey<T>, HolderSet.Named<T>> tags = new IdentityHashMap<>();
    private boolean frozen;
    @Nullable
    private final Function<T, Holder.Reference<T>> customHolderProvider;
    @Nullable
    private Map<T, Holder.Reference<T>> intrusiveHolderCache;
    @Nullable
    private List<Holder.Reference<T>> holdersInOrder;
    private int nextId;

    public MappedRegistry(ResourceKey<? extends Registry<T>> param0, Lifecycle param1, @Nullable Function<T, Holder.Reference<T>> param2) {
        super(param0, param1);
        this.elementsLifecycle = param1;
        this.customHolderProvider = param2;
        if (param2 != null) {
            this.intrusiveHolderCache = new IdentityHashMap<>();
        }

    }

    private List<Holder.Reference<T>> holdersInOrder() {
        if (this.holdersInOrder == null) {
            this.holdersInOrder = this.byId.stream().filter(Objects::nonNull).toList();
        }

        return this.holdersInOrder;
    }

    private void validateWrite(ResourceKey<T> param0) {
        if (this.frozen) {
            throw new IllegalStateException("Registry is already frozen (trying to add key " + param0 + ")");
        }
    }

    @Override
    public Holder<T> registerMapping(int param0, ResourceKey<T> param1, T param2, Lifecycle param3) {
        return this.registerMapping(param0, param1, param2, param3, true);
    }

    private Holder<T> registerMapping(int param0, ResourceKey<T> param1, T param2, Lifecycle param3, boolean param4) {
        this.validateWrite(param1);
        Validate.notNull(param1);
        Validate.notNull(param2);
        this.byId.size(Math.max(this.byId.size(), param0 + 1));
        this.toId.put(param2, param0);
        this.holdersInOrder = null;
        if (param4 && this.byKey.containsKey(param1)) {
            Util.logAndPauseIfInIde("Adding duplicate key '" + param1 + "' to registry");
        }

        if (this.byValue.containsKey(param2)) {
            Util.logAndPauseIfInIde("Adding duplicate value '" + param2 + "' to registry");
        }

        this.lifecycles.put(param2, param3);
        this.elementsLifecycle = this.elementsLifecycle.add(param3);
        if (this.nextId <= param0) {
            this.nextId = param0 + 1;
        }

        Holder.Reference<T> var0;
        if (this.customHolderProvider != null) {
            var0 = this.customHolderProvider.apply(param2);
            Holder.Reference<T> var1 = this.byKey.put(param1, var0);
            if (var1 != null && var1 != var0) {
                throw new IllegalStateException("Invalid holder present for key " + param1);
            }
        } else {
            var0 = this.byKey.computeIfAbsent(param1, param0x -> Holder.Reference.createStandAlone(this, param0x));
        }

        this.byLocation.put(param1.location(), var0);
        this.byValue.put(param2, var0);
        var0.bind(param1, param2);
        this.byId.set(param0, var0);
        return var0;
    }

    @Override
    public Holder<T> register(ResourceKey<T> param0, T param1, Lifecycle param2) {
        return this.registerMapping(this.nextId, param0, param1, param2);
    }

    @Override
    public Holder<T> registerOrOverride(OptionalInt param0, ResourceKey<T> param1, T param2, Lifecycle param3) {
        this.validateWrite(param1);
        Validate.notNull(param1);
        Validate.notNull(param2);
        Holder<T> var0 = this.byKey.get(param1);
        T var1 = var0 != null && var0.isBound() ? var0.value() : null;
        int var2;
        if (var1 == null) {
            var2 = param0.orElse(this.nextId);
        } else {
            var2 = this.toId.getInt(var1);
            if (param0.isPresent() && param0.getAsInt() != var2) {
                throw new IllegalStateException("ID mismatch");
            }

            this.lifecycles.remove(var1);
            this.toId.removeInt(var1);
            this.byValue.remove(var1);
        }

        return this.registerMapping(var2, param1, param2, param3, false);
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
    public Optional<Holder<T>> getHolder(int param0) {
        return param0 >= 0 && param0 < this.byId.size() ? Optional.ofNullable(this.byId.get(param0)) : Optional.empty();
    }

    @Override
    public Optional<Holder<T>> getHolder(ResourceKey<T> param0) {
        return Optional.ofNullable(this.byKey.get(param0));
    }

    @Override
    public Holder<T> getOrCreateHolder(ResourceKey<T> param0) {
        return this.byKey.computeIfAbsent(param0, param0x -> {
            if (this.customHolderProvider != null) {
                throw new IllegalStateException("This registry can't create new holders without value");
            } else {
                this.validateWrite(param0x);
                return Holder.Reference.createStandAlone(this, param0x);
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
    public Lifecycle elementsLifecycle() {
        return this.elementsLifecycle;
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
    public Set<Entry<ResourceKey<T>, T>> entrySet() {
        return Collections.unmodifiableSet(Maps.transformValues(this.byKey, Holder::value).entrySet());
    }

    @Override
    public Stream<Holder.Reference<T>> holders() {
        return this.holdersInOrder().stream();
    }

    @Override
    public boolean isKnownTagName(TagKey<T> param0) {
        return this.tags.containsKey(param0);
    }

    @Override
    public Stream<Pair<TagKey<T>, HolderSet.Named<T>>> getTags() {
        return this.tags.entrySet().stream().map(param0 -> Pair.of(param0.getKey(), param0.getValue()));
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
        return new HolderSet.Named<>(this, param0);
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
    public Optional<Holder<T>> getRandom(Random param0) {
        return Util.getRandomSafe(this.holdersInOrder(), param0).map(Holder::hackyErase);
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
        this.frozen = true;
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
            if (this.intrusiveHolderCache != null) {
                List<Holder.Reference<T>> var1 = this.intrusiveHolderCache.values().stream().filter(param0 -> !param0.isBound()).toList();
                if (!var1.isEmpty()) {
                    throw new IllegalStateException("Some intrusive holders were not added to registry: " + var1);
                }

                this.intrusiveHolderCache = null;
            }

            return this;
        }
    }

    @Override
    public Holder.Reference<T> createIntrusiveHolder(T param0) {
        if (this.customHolderProvider == null) {
            throw new IllegalStateException("This registry can't create intrusive holders");
        } else if (!this.frozen && this.intrusiveHolderCache != null) {
            return this.intrusiveHolderCache.computeIfAbsent(param0, param0x -> Holder.Reference.createIntrusive(this, param0x));
        } else {
            throw new IllegalStateException("Registry is already frozen");
        }
    }

    @Override
    public Optional<HolderSet.Named<T>> getTag(TagKey<T> param0) {
        return Optional.ofNullable(this.tags.get(param0));
    }

    @Override
    public void bindTags(Map<TagKey<T>, List<Holder<T>>> param0) {
        Map<Holder.Reference<T>, List<TagKey<T>>> var0 = new IdentityHashMap<>();
        this.byKey.values().forEach(param1 -> var0.put(param1, new ArrayList<>()));
        param0.forEach((param1, param2) -> {
            for(Holder<T> var0x : param2) {
                if (!var0x.isValidInRegistry(this)) {
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
}
