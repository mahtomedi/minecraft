package net.minecraft.core;

import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.flag.FeatureFlagSet;

public interface HolderLookup<T> {
    Optional<Holder.Reference<T>> get(ResourceKey<T> var1);

    Stream<ResourceKey<T>> listElements();

    Optional<HolderSet.Named<T>> get(TagKey<T> var1);

    Stream<TagKey<T>> listTags();

    static <T> HolderLookup.RegistryLookup<T> forRegistry(Registry<T> param0) {
        return new HolderLookup.RegistryLookup<>(param0);
    }

    public static class RegistryLookup<T> implements HolderLookup<T> {
        protected final Registry<T> registry;

        public RegistryLookup(Registry<T> param0) {
            this.registry = param0;
        }

        @Override
        public Optional<Holder.Reference<T>> get(ResourceKey<T> param0) {
            return this.registry.getHolder(param0);
        }

        @Override
        public Stream<ResourceKey<T>> listElements() {
            return this.registry.entrySet().stream().map(Entry::getKey);
        }

        @Override
        public Optional<HolderSet.Named<T>> get(TagKey<T> param0) {
            return this.registry.getTag(param0);
        }

        @Override
        public Stream<TagKey<T>> listTags() {
            return this.registry.getTagNames();
        }

        public HolderLookup<T> filterElements(final Predicate<T> param0) {
            return new HolderLookup<T>() {
                @Override
                public Optional<Holder.Reference<T>> get(ResourceKey<T> param0x) {
                    return RegistryLookup.this.registry.getHolder(param0).filter(param1 -> param0.test(param1.value()));
                }

                @Override
                public Stream<ResourceKey<T>> listElements() {
                    return RegistryLookup.this.registry.entrySet().stream().filter(param1 -> param0.test(param1.getValue())).map(Entry::getKey);
                }

                @Override
                public Optional<HolderSet.Named<T>> get(TagKey<T> param0x) {
                    return RegistryLookup.this.get(param0);
                }

                @Override
                public Stream<TagKey<T>> listTags() {
                    return RegistryLookup.this.listTags();
                }
            };
        }

        public HolderLookup<T> filterFeatures(FeatureFlagSet param0) {
            return (HolderLookup<T>)(FeatureElement.FILTERED_REGISTRIES.contains(this.registry.key())
                ? this.filterElements(param1 -> ((FeatureElement)param1).isEnabled(param0))
                : this);
        }
    }
}
