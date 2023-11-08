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
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import org.apache.commons.lang3.mutable.MutableObject;

public class RegistrySetBuilder {
    private final List<RegistrySetBuilder.RegistryStub<?>> entries = new ArrayList<>();

    static <T> HolderGetter<T> wrapContextLookup(final HolderLookup.RegistryLookup<T> param0) {
        return new RegistrySetBuilder.EmptyTagLookup<T>(param0) {
            @Override
            public Optional<Holder.Reference<T>> get(ResourceKey<T> param0x) {
                return param0.get(param0);
            }
        };
    }

    static <T> HolderLookup.RegistryLookup<T> lookupFromMap(
        final ResourceKey<? extends Registry<? extends T>> param0, final Lifecycle param1, final Map<ResourceKey<T>, Holder.Reference<T>> param2
    ) {
        return new HolderLookup.RegistryLookup<T>() {
            @Override
            public ResourceKey<? extends Registry<? extends T>> key() {
                return param0;
            }

            @Override
            public Lifecycle registryLifecycle() {
                return param1;
            }

            @Override
            public Optional<Holder.Reference<T>> get(ResourceKey<T> param0x) {
                return Optional.ofNullable(param2.get(param0));
            }

            @Override
            public Stream<Holder.Reference<T>> listElements() {
                return param2.values().stream();
            }

            @Override
            public Optional<HolderSet.Named<T>> get(TagKey<T> param0x) {
                return Optional.empty();
            }

            @Override
            public Stream<HolderSet.Named<T>> listTags() {
                return Stream.empty();
            }
        };
    }

    public <T> RegistrySetBuilder add(ResourceKey<? extends Registry<T>> param0, Lifecycle param1, RegistrySetBuilder.RegistryBootstrap<T> param2) {
        this.entries.add(new RegistrySetBuilder.RegistryStub<>(param0, param1, param2));
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

    private static HolderLookup.Provider buildProviderWithContext(RegistryAccess param0, Stream<HolderLookup.RegistryLookup<?>> param1) {
        Stream<HolderLookup.RegistryLookup<?>> var0 = param0.registries().map(param0x -> param0x.value().asLookup());
        return HolderLookup.Provider.create(Stream.concat(var0, param1));
    }

    public HolderLookup.Provider build(RegistryAccess param0) {
        RegistrySetBuilder.BuildState var0 = this.createState(param0);
        Stream<HolderLookup.RegistryLookup<?>> var1 = this.entries.stream().map(param1 -> param1.collectRegisteredValues(var0).buildAsLookup(var0.owner));
        HolderLookup.Provider var2 = buildProviderWithContext(param0, var1);
        var0.reportNotCollectedHolders();
        var0.reportUnclaimedRegisteredValues();
        var0.throwOnError();
        return var2;
    }

    private HolderLookup.Provider createLazyFullPatchedRegistries(
        RegistryAccess param0,
        HolderLookup.Provider param1,
        Cloner.Factory param2,
        Map<ResourceKey<? extends Registry<?>>, RegistrySetBuilder.RegistryContents<?>> param3,
        HolderLookup.Provider param4
    ) {
        RegistrySetBuilder.CompositeOwner var0 = new RegistrySetBuilder.CompositeOwner();
        MutableObject<HolderLookup.Provider> var1 = new MutableObject<>();
        List<HolderLookup.RegistryLookup<?>> var2 = param3.keySet()
            .stream()
            .map(param5 -> this.createLazyFullPatchedRegistries(var0, param2, param5, param4, param1, var1))
            .peek(var0::add)
            .collect(Collectors.toUnmodifiableList());
        HolderLookup.Provider var3 = buildProviderWithContext(param0, var2.stream());
        var1.setValue(var3);
        return var3;
    }

    private <T> HolderLookup.RegistryLookup<T> createLazyFullPatchedRegistries(
        HolderOwner<T> param0,
        Cloner.Factory param1,
        ResourceKey<? extends Registry<? extends T>> param2,
        HolderLookup.Provider param3,
        HolderLookup.Provider param4,
        MutableObject<HolderLookup.Provider> param5
    ) {
        Cloner<T> var0 = param1.cloner(param2);
        if (var0 == null) {
            throw new NullPointerException("No cloner for " + param2.location());
        } else {
            Map<ResourceKey<T>, Holder.Reference<T>> var1 = new HashMap<>();
            HolderLookup.RegistryLookup<T> var2 = param3.lookupOrThrow(param2);
            var2.listElements().forEach(param5x -> {
                ResourceKey<T> var0x = param5x.key();
                RegistrySetBuilder.LazyHolder<T> var1x = new RegistrySetBuilder.LazyHolder<>(param0, var0x);
                var1x.supplier = () -> var0.clone((T)param5x.value(), param3, param5.getValue());
                var1.put(var0x, var1x);
            });
            HolderLookup.RegistryLookup<T> var3 = param4.lookupOrThrow(param2);
            var3.listElements().forEach(param5x -> {
                ResourceKey<T> var0x = param5x.key();
                var1.computeIfAbsent(var0x, param6 -> {
                    RegistrySetBuilder.LazyHolder<T> var0xx = new RegistrySetBuilder.LazyHolder<>(param0, var0x);
                    var0xx.supplier = () -> var0.clone((T)param5x.value(), param4, param5.getValue());
                    return var0xx;
                });
            });
            Lifecycle var4 = var2.registryLifecycle().add(var3.registryLifecycle());
            return lookupFromMap(param2, var4, var1);
        }
    }

    public RegistrySetBuilder.PatchedRegistries buildPatch(RegistryAccess param0, HolderLookup.Provider param1, Cloner.Factory param2) {
        RegistrySetBuilder.BuildState var0 = this.createState(param0);
        Map<ResourceKey<? extends Registry<?>>, RegistrySetBuilder.RegistryContents<?>> var1 = new HashMap<>();
        this.entries.stream().map(param1x -> param1x.collectRegisteredValues(var0)).forEach(param1x -> var1.put(param1x.key, param1x));
        Set<ResourceKey<? extends Registry<?>>> var2 = param0.listRegistries().collect(Collectors.toUnmodifiableSet());
        param1.listRegistries()
            .filter(param1x -> !var2.contains(param1x))
            .forEach(param1x -> var1.putIfAbsent(param1x, new RegistrySetBuilder.RegistryContents<>(param1x, Lifecycle.stable(), Map.of())));
        Stream<HolderLookup.RegistryLookup<?>> var3 = var1.values().stream().map(param1x -> param1x.buildAsLookup(var0.owner));
        HolderLookup.Provider var4 = buildProviderWithContext(param0, var3);
        var0.reportUnclaimedRegisteredValues();
        var0.throwOnError();
        HolderLookup.Provider var5 = this.createLazyFullPatchedRegistries(param0, param1, param2, var1, var4);
        return new RegistrySetBuilder.PatchedRegistries(var5, var4);
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
                    RegistrySetBuilder.RegisteredValue<?> var0 = BuildState.this.registeredValues
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

        public void reportUnclaimedRegisteredValues() {
            this.registeredValues
                .forEach((param0, param1) -> this.errors.add(new IllegalStateException("Orpaned value " + param1.value + " for key " + param0)));
        }

        public void reportNotCollectedHolders() {
            for(ResourceKey<Object> var0 : this.lookup.holders.keySet()) {
                this.errors.add(new IllegalStateException("Unreferenced key: " + var0));
            }

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

        public <T> HolderOwner<T> cast() {
            return this;
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

    static class LazyHolder<T> extends Holder.Reference<T> {
        @Nullable
        Supplier<T> supplier;

        protected LazyHolder(HolderOwner<T> param0, @Nullable ResourceKey<T> param1) {
            super(Holder.Reference.Type.STAND_ALONE, param0, param1, (T)null);
        }

        @Override
        protected void bindValue(T param0) {
            super.bindValue(param0);
            this.supplier = null;
        }

        @Override
        public T value() {
            if (this.supplier != null) {
                this.bindValue(this.supplier.get());
            }

            return super.value();
        }
    }

    public static record PatchedRegistries(HolderLookup.Provider full, HolderLookup.Provider patches) {
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
        public HolderLookup.RegistryLookup<T> buildAsLookup(RegistrySetBuilder.CompositeOwner param0) {
            Map<ResourceKey<T>, Holder.Reference<T>> var0 = this.values.entrySet().stream().collect(Collectors.toUnmodifiableMap(Entry::getKey, param1 -> {
                RegistrySetBuilder.ValueAndHolder<T> var0x = param1.getValue();
                Holder.Reference<T> var1x = var0x.holder().orElseGet(() -> Holder.Reference.createStandAlone(param0.cast(), param1.getKey()));
                var1x.bindValue(var0x.value().value());
                return var1x;
            }));
            HolderLookup.RegistryLookup<T> var1 = RegistrySetBuilder.lookupFromMap(this.key, this.lifecycle, var0);
            param0.add(var1);
            return var1;
        }
    }

    static record RegistryStub<T>(ResourceKey<? extends Registry<T>> key, Lifecycle lifecycle, RegistrySetBuilder.RegistryBootstrap<T> bootstrap) {
        void apply(RegistrySetBuilder.BuildState param0) {
            this.bootstrap.run(param0.bootstapContext());
        }

        public RegistrySetBuilder.RegistryContents<T> collectRegisteredValues(RegistrySetBuilder.BuildState param0) {
            Map<ResourceKey<T>, RegistrySetBuilder.ValueAndHolder<T>> var0 = new HashMap<>();
            Iterator<Entry<ResourceKey<?>, RegistrySetBuilder.RegisteredValue<?>>> var1 = param0.registeredValues.entrySet().iterator();

            while(var1.hasNext()) {
                Entry<ResourceKey<?>, RegistrySetBuilder.RegisteredValue<?>> var2 = var1.next();
                ResourceKey<?> var3 = var2.getKey();
                if (var3.isFor(this.key)) {
                    RegistrySetBuilder.RegisteredValue<T> var5 = (RegistrySetBuilder.RegisteredValue)var2.getValue();
                    Holder.Reference<T> var6 = param0.lookup.holders.remove(var3);
                    var0.put(var3, new RegistrySetBuilder.ValueAndHolder<>(var5, Optional.ofNullable(var6)));
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
