package net.minecraft.core;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import net.minecraft.resources.HolderSetCodec;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;

public class RegistryCodecs {
    private static <T> MapCodec<RegistryCodecs.RegistryEntry<T>> withNameAndId(ResourceKey<? extends Registry<T>> param0, MapCodec<T> param1) {
        return RecordCodecBuilder.mapCodec(
            param2 -> param2.group(
                        ResourceKey.codec(param0).fieldOf("name").forGetter(RegistryCodecs.RegistryEntry::key),
                        Codec.INT.fieldOf("id").forGetter(RegistryCodecs.RegistryEntry::id),
                        param1.forGetter(RegistryCodecs.RegistryEntry::value)
                    )
                    .apply(param2, RegistryCodecs.RegistryEntry::new)
        );
    }

    public static <T> Codec<Registry<T>> networkCodec(ResourceKey<? extends Registry<T>> param0, Lifecycle param1, Codec<T> param2) {
        return withNameAndId(param0, param2.fieldOf("element")).codec().listOf().xmap(param2x -> {
            MappedRegistry<T> var0x = new MappedRegistry<>(param0, param1);

            for(RegistryCodecs.RegistryEntry<T> var1x : param2x) {
                var0x.registerMapping(var1x.id(), var1x.key(), var1x.value(), param1);
            }

            return var0x;
        }, param0x -> {
            Builder<RegistryCodecs.RegistryEntry<T>> var0x = ImmutableList.builder();

            for(T var1x : param0x) {
                var0x.add(new RegistryCodecs.RegistryEntry<>(param0x.getResourceKey((T)var1x).get(), param0x.getId((T)var1x), (T)var1x));
            }

            return var0x.build();
        });
    }

    public static <E> Codec<Registry<E>> fullCodec(ResourceKey<? extends Registry<E>> param0, Lifecycle param1, Codec<E> param2) {
        Codec<Map<ResourceKey<E>, E>> var0 = Codec.unboundedMap(ResourceKey.codec(param0), param2);
        return var0.xmap(param2x -> {
            WritableRegistry<E> var0x = new MappedRegistry<>(param0, param1);
            param2x.forEach((param2xx, param3) -> var0x.register(param2xx, param3, param1));
            return var0x.freeze();
        }, param0x -> ImmutableMap.copyOf(param0x.entrySet()));
    }

    public static <E> Codec<HolderSet<E>> homogeneousList(ResourceKey<? extends Registry<E>> param0, Codec<E> param1) {
        return homogeneousList(param0, param1, false);
    }

    public static <E> Codec<HolderSet<E>> homogeneousList(ResourceKey<? extends Registry<E>> param0, Codec<E> param1, boolean param2) {
        return HolderSetCodec.create(param0, RegistryFileCodec.create(param0, param1), param2);
    }

    public static <E> Codec<HolderSet<E>> homogeneousList(ResourceKey<? extends Registry<E>> param0) {
        return homogeneousList(param0, false);
    }

    public static <E> Codec<HolderSet<E>> homogeneousList(ResourceKey<? extends Registry<E>> param0, boolean param1) {
        return HolderSetCodec.create(param0, RegistryFixedCodec.create(param0), param1);
    }

    static record RegistryEntry<T>(ResourceKey<T> key, int id, T value) {
    }
}
