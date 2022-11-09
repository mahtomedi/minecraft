package net.minecraft.core;

import com.mojang.serialization.Lifecycle;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.flag.FeatureFlagSet;

public interface HolderLookup<T> extends HolderGetter<T> {
    Stream<Holder.Reference<T>> listElements();

    default Stream<ResourceKey<T>> listElementIds() {
        return this.listElements().map(Holder.Reference::key);
    }

    Stream<HolderSet.Named<T>> listTags();

    default Stream<TagKey<T>> listTagIds() {
        return this.listTags().map(HolderSet.Named::key);
    }

    default HolderLookup<T> filterElements(final Predicate<T> param0) {
        return new HolderLookup.Delegate<T>(this) {
            @Override
            public Optional<Holder.Reference<T>> get(ResourceKey<T> param0x) {
                return this.parent.get(param0).filter(param1 -> param0.test(param1.value()));
            }

            @Override
            public Stream<Holder.Reference<T>> listElements() {
                return this.parent.listElements().filter(param1 -> param0.test(param1.value()));
            }
        };
    }

    public static class Delegate<T> implements HolderLookup<T> {
        protected final HolderLookup<T> parent;

        public Delegate(HolderLookup<T> param0) {
            this.parent = param0;
        }

        @Override
        public Optional<Holder.Reference<T>> get(ResourceKey<T> param0) {
            return this.parent.get(param0);
        }

        @Override
        public Stream<Holder.Reference<T>> listElements() {
            return this.parent.listElements();
        }

        @Override
        public Optional<HolderSet.Named<T>> get(TagKey<T> param0) {
            return this.parent.get(param0);
        }

        @Override
        public Stream<HolderSet.Named<T>> listTags() {
            return this.parent.listTags();
        }
    }

    public interface Provider {
        <T> Optional<HolderLookup.RegistryLookup<T>> lookup(ResourceKey<? extends Registry<? extends T>> var1);

        default <T> HolderLookup.RegistryLookup<T> lookupOrThrow(ResourceKey<? extends Registry<? extends T>> param0) {
            return this.<T>lookup(param0).orElseThrow(() -> new IllegalStateException("Registry " + param0.location() + " not found"));
        }

        default HolderGetter.Provider asGetterLookup() {
            return new HolderGetter.Provider() {
                @Override
                public <T> Optional<HolderGetter<T>> lookup(ResourceKey<? extends Registry<? extends T>> param0) {
                    return Provider.this.lookup(param0).map(param0x -> param0x);
                }
            };
        }

        static HolderLookup.Provider create(Stream<HolderLookup.RegistryLookup<?>> param0) {
            final Map<ResourceKey<? extends Registry<?>>, HolderLookup.RegistryLookup<?>> var0 = param0.collect(
                Collectors.toUnmodifiableMap(
                    HolderLookup.RegistryLookup::key,
                    (Function<? super HolderLookup.RegistryLookup<?>, ? extends HolderLookup.RegistryLookup<?>>)(param0x -> param0x)
                )
            );
            return new HolderLookup.Provider() {
                @Override
                public <T> Optional<HolderLookup.RegistryLookup<T>> lookup(ResourceKey<? extends Registry<? extends T>> param0) {
                    return Optional.ofNullable((HolderLookup.RegistryLookup<T>)var0.get(param0));
                }
            };
        }
    }

    public interface RegistryLookup<T> extends HolderLookup<T>, HolderOwner<T> {
        ResourceKey<? extends Registry<? extends T>> key();

        Lifecycle registryLifecycle();

        default HolderLookup<T> filterFeatures(FeatureFlagSet param0) {
            return (HolderLookup<T>)(FeatureElement.FILTERED_REGISTRIES.contains(this.key())
                ? this.filterElements(param1 -> ((FeatureElement)param1).isEnabled(param0))
                : this);
        }

        public abstract static class Delegate<T> implements HolderLookup.RegistryLookup<T> {
            protected abstract HolderLookup.RegistryLookup<T> parent();

            @Override
            public ResourceKey<? extends Registry<? extends T>> key() {
                return this.parent().key();
            }

            @Override
            public Lifecycle registryLifecycle() {
                return this.parent().registryLifecycle();
            }

            @Override
            public Optional<Holder.Reference<T>> get(ResourceKey<T> param0) {
                return this.parent().get(param0);
            }

            @Override
            public Stream<Holder.Reference<T>> listElements() {
                return this.parent().listElements();
            }

            @Override
            public Optional<HolderSet.Named<T>> get(TagKey<T> param0) {
                return this.parent().get(param0);
            }

            @Override
            public Stream<HolderSet.Named<T>> listTags() {
                return this.parent().listTags();
            }
        }
    }
}
