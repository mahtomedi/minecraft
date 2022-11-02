package net.minecraft.core;

import com.mojang.serialization.Lifecycle;
import net.minecraft.resources.ResourceKey;

public abstract class WritableRegistry<T> extends Registry<T> {
    public WritableRegistry(ResourceKey<? extends Registry<T>> param0, Lifecycle param1) {
        super(param0, param1);
    }

    public abstract Holder<T> registerMapping(int var1, ResourceKey<T> var2, T var3, Lifecycle var4);

    public abstract Holder.Reference<T> register(ResourceKey<T> var1, T var2, Lifecycle var3);

    public abstract boolean isEmpty();

    public abstract HolderGetter<T> createRegistrationLookup();
}
