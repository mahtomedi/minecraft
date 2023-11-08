package net.minecraft.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.JavaOps;

public class Cloner<T> {
    private final Codec<T> directCodec;

    Cloner(Codec<T> param0) {
        this.directCodec = param0;
    }

    public T clone(T param0, HolderLookup.Provider param1, HolderLookup.Provider param2) {
        DynamicOps<Object> var0 = RegistryOps.create(JavaOps.INSTANCE, param1);
        DynamicOps<Object> var1 = RegistryOps.create(JavaOps.INSTANCE, param2);
        Object var2 = Util.getOrThrow(this.directCodec.encodeStart(var0, param0), param0x -> new IllegalStateException("Failed to encode: " + param0x));
        return Util.getOrThrow(this.directCodec.parse(var1, var2), param0x -> new IllegalStateException("Failed to decode: " + param0x));
    }

    public static class Factory {
        private final Map<ResourceKey<? extends Registry<?>>, Cloner<?>> codecs = new HashMap<>();

        public <T> Cloner.Factory addCodec(ResourceKey<? extends Registry<? extends T>> param0, Codec<T> param1) {
            this.codecs.put(param0, new Cloner<>(param1));
            return this;
        }

        @Nullable
        public <T> Cloner<T> cloner(ResourceKey<? extends Registry<? extends T>> param0) {
            return (Cloner<T>)this.codecs.get(param0);
        }
    }
}
