package net.minecraft.resources;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;

public class HolderSetCodec<E> implements Codec<HolderSet<E>> {
    private final ResourceKey<? extends Registry<E>> registryKey;
    private final Codec<Holder<E>> elementCodec;
    private final Codec<List<Holder<E>>> homogenousListCodec;
    private final Codec<Either<TagKey<E>, List<Holder<E>>>> registryAwareCodec;

    private static <E> Codec<List<Holder<E>>> homogenousList(Codec<Holder<E>> param0, boolean param1) {
        Function<List<Holder<E>>, DataResult<List<Holder<E>>>> var0 = ExtraCodecs.ensureHomogenous(Holder::kind);
        Codec<List<Holder<E>>> var1 = param0.listOf().flatXmap(var0, var0);
        return param1
            ? var1
            : Codec.either(var1, param0)
                .xmap(
                    param0x -> param0x.map((Function<? super List<Holder<E>>, ? extends List<Holder<E>>>)(param0xx -> param0xx), List::of),
                    param0x -> param0x.size() == 1 ? Either.right(param0x.get(0)) : Either.left(param0x)
                );
    }

    public static <E> Codec<HolderSet<E>> create(ResourceKey<? extends Registry<E>> param0, Codec<Holder<E>> param1, boolean param2) {
        return new HolderSetCodec<>(param0, param1, param2);
    }

    private HolderSetCodec(ResourceKey<? extends Registry<E>> param0, Codec<Holder<E>> param1, boolean param2) {
        this.registryKey = param0;
        this.elementCodec = param1;
        this.homogenousListCodec = homogenousList(param1, param2);
        this.registryAwareCodec = Codec.either(TagKey.hashedCodec(param0), this.homogenousListCodec);
    }

    @Override
    public <T> DataResult<Pair<HolderSet<E>, T>> decode(DynamicOps<T> param0, T param1) {
        if (param0 instanceof RegistryOps var0) {
            Optional<? extends Registry<E>> var1 = var0.registry(this.registryKey);
            if (var1.isPresent()) {
                Registry<E> var2 = var1.get();
                return this.registryAwareCodec
                    .decode(param0, param1)
                    .map(param1x -> param1x.mapFirst(param1xx -> param1xx.map(var2::getOrCreateTag, HolderSet::direct)));
            }
        }

        return this.decodeWithoutRegistry(param0, param1);
    }

    public <T> DataResult<T> encode(HolderSet<E> param0, DynamicOps<T> param1, T param2) {
        return param1 instanceof RegistryOps
            ? this.registryAwareCodec.encode(param0.unwrap().mapRight(List::copyOf), param1, param2)
            : this.encodeWithoutRegistry(param0, param1, param2);
    }

    private <T> DataResult<Pair<HolderSet<E>, T>> decodeWithoutRegistry(DynamicOps<T> param0, T param1) {
        return this.elementCodec.listOf().decode(param0, param1).flatMap(param0x -> {
            List<Holder.Direct<E>> var0 = new ArrayList<>();

            for(Holder<E> var1x : param0x.getFirst()) {
                if (!(var1x instanceof Holder.Direct)) {
                    return DataResult.error("Can't decode element " + var1x + " without registry");
                }

                Holder.Direct<E> var2x = (Holder.Direct)var1x;
                var0.add(var2x);
            }

            return DataResult.success(new Pair<>(HolderSet.direct(var0), param0x.getSecond()));
        });
    }

    private <T> DataResult<T> encodeWithoutRegistry(HolderSet<E> param0, DynamicOps<T> param1, T param2) {
        return this.homogenousListCodec.encode(param0.stream().toList(), param1, param2);
    }
}
