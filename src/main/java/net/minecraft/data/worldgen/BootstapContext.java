package net.minecraft.data.worldgen;

import com.mojang.serialization.Lifecycle;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public interface BootstapContext<T> {
    Holder.Reference<T> register(ResourceKey<T> var1, T var2, Lifecycle var3);

    default Holder.Reference<T> register(ResourceKey<T> param0, T param1) {
        return this.register(param0, param1, Lifecycle.stable());
    }

    <S> HolderGetter<S> lookup(ResourceKey<? extends Registry<? extends S>> var1);
}
