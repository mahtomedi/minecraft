package net.minecraft.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.PrimitiveCodec;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

public class ExtraCodecs {
    public static final Codec<DoubleStream> DOUBLE_STREAM = new PrimitiveCodec<DoubleStream>() {
        @Override
        public <T> DataResult<DoubleStream> read(DynamicOps<T> param0, T param1) {
            return ExtraCodecs.asDoubleStream(param0, param1);
        }

        public <T> T write(DynamicOps<T> param0, DoubleStream param1) {
            return ExtraCodecs.createDoubleList(param0, param1);
        }

        @Override
        public String toString() {
            return "DoubleStream";
        }
    };

    public static <T> DataResult<DoubleStream> asDoubleStream(DynamicOps<T> param0, T param1) {
        return param0.getStream(param1)
            .flatMap(
                param2 -> {
                    List<T> var0x = param2.collect(Collectors.toList());
                    return var0x.stream().allMatch(param1x -> param0.getNumberValue((T)param1x).result().isPresent())
                        ? DataResult.success(var0x.stream().mapToDouble(param1x -> param0.getNumberValue((T)param1x).result().get().doubleValue()))
                        : DataResult.error("Some elements are not doubles: " + param1);
                }
            );
    }

    public static <T> T createDoubleList(DynamicOps<T> param0, DoubleStream param1) {
        return param0.createList(param1.mapToObj(param0::createDouble));
    }
}
