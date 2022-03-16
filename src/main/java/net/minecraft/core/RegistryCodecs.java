package net.minecraft.core;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.resources.HolderSetCodec;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.RegistryLoader;
import net.minecraft.resources.RegistryOps;
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
            WritableRegistry<T> var0x = new MappedRegistry<>(param0, param1, null);

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

    public static <E> Codec<Registry<E>> dataPackAwareCodec(ResourceKey<? extends Registry<E>> param0, Lifecycle param1, Codec<E> param2) {
        Codec<Map<ResourceKey<E>, E>> var0 = directCodec(param0, param2);
        Encoder<Registry<E>> var1 = var0.comap(param0x -> ImmutableMap.copyOf(param0x.entrySet()));
        return Codec.of(var1, dataPackAwareDecoder(param0, param2, var0, param1), "DataPackRegistryCodec for " + param0);
    }

    private static <E> Decoder<Registry<E>> dataPackAwareDecoder(
        final ResourceKey<? extends Registry<E>> param0, final Codec<E> param1, Decoder<Map<ResourceKey<E>, E>> param2, Lifecycle param3
    ) {
        final Decoder<WritableRegistry<E>> var0 = param2.map(param2x -> {
            WritableRegistry<E> var0x = new MappedRegistry<>(param0, param3, null);
            param2x.forEach((param2xx, param3x) -> var0x.register(param2xx, param3x, param3));
            return var0x;
        });
        return new Decoder<Registry<E>>() {
            @Override
            public <T> DataResult<Pair<Registry<E>, T>> decode(DynamicOps<T> param0x, T param1x) {
                DataResult<Pair<WritableRegistry<E>, T>> var0 = var0.decode(param0, param1);
                return param0 instanceof RegistryOps var1
                    ? var1.registryLoader()
                        .map(param2 -> this.overrideFromResources(var0, var1, param2.loader()))
                        .orElseGet(() -> DataResult.error("Can't load registry with this ops"))
                    : var0.map(param0xx -> param0xx.mapFirst((Function<? super WritableRegistry<E>, ? extends WritableRegistry<E>>)(param0xxxx -> param0xxxx)));
            }

            private <T> DataResult<Pair<Registry<E>, T>> overrideFromResources(
                DataResult<Pair<WritableRegistry<E>, T>> param0x, RegistryOps<?> param1x, RegistryLoader param2
            ) {
                return param0.flatMap(
                    param4 -> param2.overrideRegistryFromResources(param4.getFirst(), param0, param1, param1.getAsJson())
                            .map(param1xxxxx -> Pair.of(param1xxxxx, (T)param4.getSecond()))
                );
            }
        };
    }

    private static <T> Codec<Map<ResourceKey<T>, T>> directCodec(ResourceKey<? extends Registry<T>> param0, Codec<T> param1) {
        return Codec.unboundedMap(ResourceKey.codec(param0), param1);
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
