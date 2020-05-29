package net.minecraft.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.function.Function;

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
}
