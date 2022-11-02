package net.minecraft.core;

import java.util.Optional;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

public interface HolderGetter<T> {
    Optional<Holder.Reference<T>> get(ResourceKey<T> var1);

    default Holder.Reference<T> getOrThrow(ResourceKey<T> param0) {
        return this.get(param0).orElseThrow(() -> new IllegalStateException("Missing element " + param0));
    }

    Optional<HolderSet.Named<T>> get(TagKey<T> var1);

    default HolderSet.Named<T> getOrThrow(TagKey<T> param0) {
        return this.get(param0).orElseThrow(() -> new IllegalStateException("Missing tag " + param0));
    }

    public interface Provider {
        <T> Optional<HolderGetter<T>> lookup(ResourceKey<? extends Registry<? extends T>> var1);

        default <T> HolderGetter<T> lookupOrThrow(ResourceKey<? extends Registry<? extends T>> param0) {
            return this.<T>lookup(param0).orElseThrow(() -> new IllegalStateException("Registry " + param0.location() + " not found"));
        }
    }
}
