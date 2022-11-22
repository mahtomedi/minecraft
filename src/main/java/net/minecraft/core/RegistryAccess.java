package net.minecraft.core;

import com.google.common.collect.ImmutableMap;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Lifecycle;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceKey;
import org.slf4j.Logger;

public interface RegistryAccess extends HolderLookup.Provider {
    Logger LOGGER = LogUtils.getLogger();
    RegistryAccess.Frozen EMPTY = new RegistryAccess.ImmutableRegistryAccess(Map.of()).freeze();

    <E> Optional<Registry<E>> registry(ResourceKey<? extends Registry<? extends E>> var1);

    @Override
    default <T> Optional<HolderLookup.RegistryLookup<T>> lookup(ResourceKey<? extends Registry<? extends T>> param0) {
        return this.registry(param0).map(Registry::asLookup);
    }

    default <E> Registry<E> registryOrThrow(ResourceKey<? extends Registry<? extends E>> param0) {
        return this.registry(param0).orElseThrow(() -> new IllegalStateException("Missing registry: " + param0));
    }

    Stream<RegistryAccess.RegistryEntry<?>> registries();

    static RegistryAccess.Frozen fromRegistryOfRegistries(final Registry<? extends Registry<?>> param0) {
        return new RegistryAccess.Frozen() {
            @Override
            public <T> Optional<Registry<T>> registry(ResourceKey<? extends Registry<? extends T>> param0x) {
                Registry<Registry<T>> var0 = param0;
                return var0.getOptional(param0);
            }

            @Override
            public Stream<RegistryAccess.RegistryEntry<?>> registries() {
                return param0.entrySet().stream().map(RegistryAccess.RegistryEntry::fromMapEntry);
            }

            @Override
            public RegistryAccess.Frozen freeze() {
                return this;
            }
        };
    }

    default RegistryAccess.Frozen freeze() {
        class FrozenAccess extends RegistryAccess.ImmutableRegistryAccess implements RegistryAccess.Frozen {
            protected FrozenAccess(Stream<RegistryAccess.RegistryEntry<?>> param1) {
                super(param1);
            }
        }

        return new FrozenAccess(this.registries().map(RegistryAccess.RegistryEntry::freeze));
    }

    default Lifecycle allRegistriesLifecycle() {
        return this.registries().map(param0 -> param0.value.registryLifecycle()).reduce(Lifecycle.stable(), Lifecycle::add);
    }

    public interface Frozen extends RegistryAccess {
    }

    public static class ImmutableRegistryAccess implements RegistryAccess {
        private final Map<? extends ResourceKey<? extends Registry<?>>, ? extends Registry<?>> registries;

        public ImmutableRegistryAccess(List<? extends Registry<?>> param0) {
            this.registries = param0.stream().collect(Collectors.toUnmodifiableMap(Registry::key, param0x -> param0x));
        }

        public ImmutableRegistryAccess(Map<? extends ResourceKey<? extends Registry<?>>, ? extends Registry<?>> param0) {
            this.registries = Map.copyOf(param0);
        }

        public ImmutableRegistryAccess(Stream<RegistryAccess.RegistryEntry<?>> param0) {
            this.registries = param0.collect(ImmutableMap.toImmutableMap(RegistryAccess.RegistryEntry::key, RegistryAccess.RegistryEntry::value));
        }

        @Override
        public <E> Optional<Registry<E>> registry(ResourceKey<? extends Registry<? extends E>> param0) {
            return Optional.ofNullable(this.registries.get(param0)).map((Function<? super Registry<?>, ? extends Registry<E>>)(param0x -> param0x));
        }

        @Override
        public Stream<RegistryAccess.RegistryEntry<?>> registries() {
            return this.registries.entrySet().stream().map(RegistryAccess.RegistryEntry::fromMapEntry);
        }
    }

    public static record RegistryEntry<T>(ResourceKey<? extends Registry<T>> key, Registry<T> value) {
        private static <T, R extends Registry<? extends T>> RegistryAccess.RegistryEntry<T> fromMapEntry(
            Entry<? extends ResourceKey<? extends Registry<?>>, R> param0
        ) {
            return fromUntyped(param0.getKey(), param0.getValue());
        }

        private static <T> RegistryAccess.RegistryEntry<T> fromUntyped(ResourceKey<? extends Registry<?>> param0, Registry<?> param1) {
            return new RegistryAccess.RegistryEntry<>(param0, param1);
        }

        private RegistryAccess.RegistryEntry<T> freeze() {
            return new RegistryAccess.RegistryEntry<>(this.key, this.value.freeze());
        }
    }
}
