package net.minecraft.core;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.serialization.Lifecycle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

public class RegistrySetBuilder {
    private final List<RegistrySetBuilder.RegistryStub<?>> entries = new ArrayList();

    static <T> HolderGetter<T> wrapContextLookup(final HolderLookup.RegistryLookup<T> param0) {
        return new RegistrySetBuilder.EmptyTagLookup<T>(param0) {
            @Override
            public Optional<Holder.Reference<T>> get(ResourceKey<T> param0x) {
                return param0.get(param0);
            }
        };
    }

    public <T> RegistrySetBuilder add(ResourceKey<? extends Registry<T>> param0, Lifecycle param1, RegistrySetBuilder.RegistryBootstrap<T> param2) {
        this.entries.add(new RegistrySetBuilder.RegistryStub<T>(param0, param1, param2));
        return this;
    }

    public <T> RegistrySetBuilder add(ResourceKey<? extends Registry<T>> param0, RegistrySetBuilder.RegistryBootstrap<T> param1) {
        return this.add(param0, Lifecycle.stable(), param1);
    }

    private RegistrySetBuilder.BuildState createState(RegistryAccess param0) {
        RegistrySetBuilder.BuildState var0 = RegistrySetBuilder.BuildState.create(param0, this.entries.stream().map(RegistrySetBuilder.RegistryStub::key));
        this.entries.forEach(param1 -> param1.apply(var0));
        return var0;
    }

    public HolderLookup.Provider build(RegistryAccess param0) {
        RegistrySetBuilder.BuildState var0 = this.createState(param0);
        Stream<HolderLookup.RegistryLookup<?>> var1 = param0.registries().map(param0x -> param0x.value().asLookup());
        Stream<HolderLookup.RegistryLookup<?>> var2 = this.entries.stream().map(param1 -> param1.collectChanges(var0).buildAsLookup());
        HolderLookup.Provider var3 = HolderLookup.Provider.create(Stream.concat(var1, var2.peek(var0::addOwner)));
        var0.reportRemainingUnreferencedValues();
        var0.throwOnError();
        return var3;
    }

    public HolderLookup.Provider buildPatch(RegistryAccess param0, HolderLookup.Provider param1) {
        RegistrySetBuilder.BuildState var0 = this.createState(param0);
        Map<ResourceKey<? extends Registry<?>>, RegistrySetBuilder.RegistryContents<?>> var1 = new HashMap<>();
        var0.collectReferencedRegistries().forEach(param1x -> var1.put(param1x.key, param1x));
        this.entries.stream().map(param1x -> param1x.collectChanges(var0)).forEach(param1x -> var1.put(param1x.key, param1x));
        Stream<HolderLookup.RegistryLookup<?>> var2 = param0.registries().map(param0x -> param0x.value().asLookup());
        HolderLookup.Provider var3 = HolderLookup.Provider.create(
            Stream.concat(var2, var1.values().stream().map(RegistrySetBuilder.RegistryContents::buildAsLookup).peek(var0::addOwner))
        );
        var0.fillMissingHolders(param1);
        var0.reportRemainingUnreferencedValues();
        var0.throwOnError();
        return var3;
    }

    static record BuildState<T>(
        RegistrySetBuilder.CompositeOwner owner,
        RegistrySetBuilder.UniversalLookup lookup,
        Map<ResourceLocation, HolderGetter<?>> registries,
        Map<ResourceKey<?>, RegistrySetBuilder.RegisteredValue<?>> registeredValues,
        List<RuntimeException> errors
    ) {
        public static RegistrySetBuilder.BuildState create(RegistryAccess param0, Stream<ResourceKey<? extends Registry<?>>> param1) {
            RegistrySetBuilder.CompositeOwner var0 = new RegistrySetBuilder.CompositeOwner();
            List<RuntimeException> var1 = new ArrayList<>();
            RegistrySetBuilder.UniversalLookup var2 = new RegistrySetBuilder.UniversalLookup(var0);
            Builder<ResourceLocation, HolderGetter<?>> var3 = ImmutableMap.builder();
            param0.registries().forEach(param1x -> var3.put(param1x.key().location(), RegistrySetBuilder.wrapContextLookup(param1x.value().asLookup())));
            param1.forEach(param2 -> var3.put(param2.location(), var2));
            return new RegistrySetBuilder.BuildState(var0, var2, var3.build(), new HashMap<>(), var1);
        }

        public <T> BootstapContext<T> bootstapContext() {
            return new BootstapContext<T>() {
                @Override
                public Holder.Reference<T> register(ResourceKey<T> param0, T param1, Lifecycle param2) {
                    RegistrySetBuilder.RegisteredValue<?> var0 = (RegistrySetBuilder.RegisteredValue)BuildState.this.registeredValues
                        .put(param0, new RegistrySetBuilder.RegisteredValue(param1, param2));
                    if (var0 != null) {
                        BuildState.this.errors
                            .add(new IllegalStateException("Duplicate registration for " + param0 + ", new=" + param1 + ", old=" + var0.value));
                    }

                    return BuildState.this.lookup.getOrCreate(param0);
                }

                @Override
                public <S> HolderGetter<S> lookup(ResourceKey<? extends Registry<? extends S>> param0) {
                    return (HolderGetter<S>)BuildState.this.registries.getOrDefault(param0.location(), BuildState.this.lookup);
                }
            };
        }

        public void reportRemainingUnreferencedValues() {
            for(ResourceKey<Object> var0 : this.lookup.holders.keySet()) {
                this.errors.add(new IllegalStateException("Unreferenced key: " + var0));
            }

            this.registeredValues
                .forEach((param0, param1) -> this.errors.add(new IllegalStateException("Orpaned value " + param1.value + " for key " + param0)));
        }

        public void throwOnError() {
            if (!this.errors.isEmpty()) {
                IllegalStateException var0 = new IllegalStateException("Errors during registry creation");

                for(RuntimeException var1 : this.errors) {
                    var0.addSuppressed(var1);
                }

                throw var0;
            }
        }

        public void addOwner(HolderOwner<?> param0) {
            this.owner.add(param0);
        }

        public void fillMissingHolders(HolderLookup.Provider param0) {
            Map<ResourceLocation, Optional<? extends HolderLookup<Object>>> var0 = new HashMap<>();
            Iterator<Entry<ResourceKey<Object>, Holder.Reference<Object>>> var1 = this.lookup.holders.entrySet().iterator();

            while(var1.hasNext()) {
                Entry<ResourceKey<Object>, Holder.Reference<Object>> var2 = var1.next();
                ResourceKey<Object> var3 = var2.getKey();
                Holder.Reference<Object> var4 = var2.getValue();
                var0.computeIfAbsent(var3.registry(), param1 -> param0.lookup(ResourceKey.createRegistryKey(param1)))
                    .flatMap(param1 -> param1.get(var3))
                    .ifPresent(param2 -> {
                        var4.bindValue(param2.value());
                        var1.remove();
                    });
            }

        }

        public Stream<RegistrySetBuilder.RegistryContents<?>> collectReferencedRegistries() {
            return this.lookup
                .holders
                .keySet()
                .stream()
                .map(ResourceKey::registry)
                .distinct()
                .map(param0 -> new RegistrySetBuilder.RegistryContents(ResourceKey.createRegistryKey(param0), Lifecycle.stable(), Map.of()));
        }
    }

    static class CompositeOwner implements HolderOwner<Object> {
        private final Set<HolderOwner<?>> owners = Sets.newIdentityHashSet();

        @Override
        public boolean canSerializeIn(HolderOwner<Object> param0) {
            return this.owners.contains(param0);
        }

        public void add(HolderOwner<?> param0) {
            this.owners.add(param0);
        }
    }

    abstract static class EmptyTagLookup<T> implements HolderGetter<T> {
        protected final HolderOwner<T> owner;

        protected EmptyTagLookup(HolderOwner<T> param0) {
            this.owner = param0;
        }

        @Override
        public Optional<HolderSet.Named<T>> get(TagKey<T> param0) {
            return Optional.of(HolderSet.emptyNamed(this.owner, param0));
        }
    }

    static record RegisteredValue<T>(T value, Lifecycle lifecycle) {
    }

    @FunctionalInterface
    public interface RegistryBootstrap<T> {
        void run(BootstapContext<T> var1);
    }

    static record RegistryContents<T>(
        ResourceKey<? extends Registry<? extends T>> key, Lifecycle lifecycle, Map<ResourceKey<T>, RegistrySetBuilder.ValueAndHolder<T>> values
    ) {
        public HolderLookup.RegistryLookup<T> buildAsLookup() {
            return new HolderLookup.RegistryLookup<T>() {
                private final Map<ResourceKey<T>, Holder.Reference<T>> entries = RegistryContents.this.values
                    .entrySet()
                    .stream()
                    .collect(Collectors.toUnmodifiableMap(Entry::getKey, param0x -> {
                        RegistrySetBuilder.ValueAndHolder<T> var0 = (RegistrySetBuilder.ValueAndHolder)param0x.getValue();
                        Holder.Reference<T> var1x = var0.holder().orElseGet(() -> Holder.Reference.createStandAlone(this, (ResourceKey<T>)param0x.getKey()));
                        var1x.bindValue(var0.value().value());
                        return var1x;
                    }));

                @Override
                public ResourceKey<? extends Registry<? extends T>> key() {
                    return RegistryContents.this.key;
                }

                @Override
                public Lifecycle registryLifecycle() {
                    return RegistryContents.this.lifecycle;
                }

                @Override
                public Optional<Holder.Reference<T>> get(ResourceKey<T> param0) {
                    return Optional.ofNullable(this.entries.get(param0));
                }

                @Override
                public Stream<Holder.Reference<T>> listElements() {
                    return this.entries.values().stream();
                }

                @Override
                public Optional<HolderSet.Named<T>> get(TagKey<T> param0) {
                    return Optional.empty();
                }

                @Override
                public Stream<HolderSet.Named<T>> listTags() {
                    return Stream.empty();
                }
            };
        }
    }

    static record RegistryStub<T>(ResourceKey<? extends Registry<T>> key, Lifecycle lifecycle, RegistrySetBuilder.RegistryBootstrap<T> bootstrap) {
        void apply(RegistrySetBuilder.BuildState param0) {
            this.bootstrap.run(param0.bootstapContext());
        }

        public RegistrySetBuilder.RegistryContents<T> collectChanges(RegistrySetBuilder.BuildState param0) {
            Map<ResourceKey<T>, RegistrySetBuilder.ValueAndHolder<T>> var0 = new HashMap<>();
            Iterator<Entry<ResourceKey<?>, RegistrySetBuilder.RegisteredValue<?>>> var1 = param0.registeredValues.entrySet().iterator();

            while(var1.hasNext()) {
                Entry<ResourceKey<?>, RegistrySetBuilder.RegisteredValue<?>> var2 = var1.next();
                ResourceKey<?> var3 = var2.getKey();
                if (var3.isFor(this.key)) {
                    RegistrySetBuilder.RegisteredValue<T> var5 = (RegistrySetBuilder.RegisteredValue)var2.getValue();
                    Holder.Reference<T> var6 = param0.lookup.holders.remove(var3);
                    var0.put(var3, new RegistrySetBuilder.ValueAndHolder<T>(var5, Optional.ofNullable(var6)));
                    var1.remove();
                }
            }

            return new RegistrySetBuilder.RegistryContents<>(this.key, this.lifecycle, var0);
        }
    }

    static class UniversalLookup extends RegistrySetBuilder.EmptyTagLookup<Object> {
        final Map<ResourceKey<Object>, Holder.Reference<Object>> holders = new HashMap<>();

        public UniversalLookup(HolderOwner<Object> param0) {
            super(param0);
        }

        @Override
        public Optional<Holder.Reference<Object>> get(ResourceKey<Object> param0) {
            return Optional.of(this.getOrCreate(param0));
        }

        <T> Holder.Reference<T> getOrCreate(ResourceKey<T> param0) {
            return this.holders.computeIfAbsent(param0, param0x -> Holder.Reference.createStandAlone(this.owner, param0x));
        }
    }

    static record ValueAndHolder<T>(RegistrySetBuilder.RegisteredValue<T> value, Optional<Holder.Reference<T>> holder) {
    }
}
