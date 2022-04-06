package net.minecraft.util;

import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import java.util.function.Function;

public interface ToFloatFunction<C> {
    ToFloatFunction<Float> IDENTITY = createUnlimited(param0 -> param0);

    float apply(C var1);

    float minValue();

    float maxValue();

    static ToFloatFunction<Float> createUnlimited(final Float2FloatFunction param0) {
        return new ToFloatFunction<Float>() {
            public float apply(Float param0x) {
                return param0.apply(param0);
            }

            @Override
            public float minValue() {
                return Float.NEGATIVE_INFINITY;
            }

            @Override
            public float maxValue() {
                return Float.POSITIVE_INFINITY;
            }
        };
    }

    default <C2> ToFloatFunction<C2> comap(final Function<C2, C> param0) {
        final ToFloatFunction<C> var0 = this;
        return new ToFloatFunction<C2>() {
            @Override
            public float apply(C2 param0x) {
                return var0.apply(param0.apply(param0));
            }

            @Override
            public float minValue() {
                return var0.minValue();
            }

            @Override
            public float maxValue() {
                return var0.maxValue();
            }
        };
    }
}
