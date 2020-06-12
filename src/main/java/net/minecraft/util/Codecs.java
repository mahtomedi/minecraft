package net.minecraft.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class Codecs {
    private static Function<Integer, DataResult<Integer>> checkRange(int param0, int param1) {
        return param2 -> param2 >= param0 && param2 <= param1
                ? DataResult.success(param2)
                : DataResult.error("Value " + param2 + " outside of range [" + param0 + ":" + param1 + "]", param2);
    }

    public static Codec<Integer> intRange(int param0, int param1) {
        Function<Integer, DataResult<Integer>> var0 = checkRange(param0, param1);
        return Codec.INT.flatXmap(var0, var0);
    }

    private static Function<Double, DataResult<Double>> checkRange(double param0, double param1) {
        return param2 -> param2 >= param0 && param2 <= param1
                ? DataResult.success(param2)
                : DataResult.error("Value " + param2 + " outside of range [" + param0 + ":" + param1 + "]", param2);
    }

    public static Codec<Double> doubleRange(double param0, double param1) {
        Function<Double, DataResult<Double>> var0 = checkRange(param0, param1);
        return Codec.DOUBLE.flatXmap(var0, var0);
    }

    public static <T> MapCodec<Pair<ResourceKey<T>, T>> withName(ResourceKey<Registry<T>> param0, MapCodec<T> param1) {
        return Codec.mapPair(ResourceLocation.CODEC.xmap(ResourceKey.elementKey(param0), ResourceKey::location).fieldOf("name"), param1);
    }

    private static <A> MapCodec<A> mapResult(final MapCodec<A> param0, final Codecs.ResultFunction<A> param1) {
        return new MapCodec<A>() {
            @Override
            public <T> Stream<T> keys(DynamicOps<T> param0x) {
                return param0.keys(param0);
            }

            @Override
            public <T> RecordBuilder<T> encode(A param0x, DynamicOps<T> param1x, RecordBuilder<T> param2) {
                return param1.coApply(param1, param0, param0.encode(param0, param1, param2));
            }

            @Override
            public <T> DataResult<A> decode(DynamicOps<T> param0x, MapLike<T> param1x) {
                return param1.apply(param0, param1, param0.decode(param0, param1));
            }

            @Override
            public String toString() {
                return param0 + "[mapResult " + param1 + "]";
            }
        };
    }

    public static <A> MapCodec<A> withDefault(MapCodec<A> param0, final Consumer<String> param1, final Supplier<? extends A> param2) {
        return mapResult(param0, new Codecs.ResultFunction<A>() {
            @Override
            public <T> DataResult<A> apply(DynamicOps<T> param0, MapLike<T> param1x, DataResult<A> param2x) {
                return DataResult.success(param2.resultOrPartial(param1).orElseGet(param2));
            }

            @Override
            public <T> RecordBuilder<T> coApply(DynamicOps<T> param0, A param1x, RecordBuilder<T> param2x) {
                return param2;
            }

            @Override
            public String toString() {
                return "WithDefault[" + param2.get() + "]";
            }
        });
    }

    public static <A> MapCodec<A> setPartial(MapCodec<A> param0, final Supplier<A> param1) {
        return mapResult(param0, new Codecs.ResultFunction<A>() {
            @Override
            public <T> DataResult<A> apply(DynamicOps<T> param0, MapLike<T> param1x, DataResult<A> param2) {
                return param2.setPartial(param1);
            }

            @Override
            public <T> RecordBuilder<T> coApply(DynamicOps<T> param0, A param1x, RecordBuilder<T> param2) {
                return param2;
            }

            @Override
            public String toString() {
                return "SetPartial[" + param1 + "]";
            }
        });
    }

    interface ResultFunction<A> {
        <T> DataResult<A> apply(DynamicOps<T> var1, MapLike<T> var2, DataResult<A> var3);

        <T> RecordBuilder<T> coApply(DynamicOps<T> var1, A var2, RecordBuilder<T> var3);
    }
}
