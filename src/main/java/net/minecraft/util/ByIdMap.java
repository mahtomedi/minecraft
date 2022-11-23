package net.minecraft.util;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;

public class ByIdMap {
    private static <T> IntFunction<T> createMap(ToIntFunction<T> param0, T[] param1) {
        if (param1.length == 0) {
            throw new IllegalArgumentException("Empty value list");
        } else {
            Int2ObjectMap<T> var0 = new Int2ObjectOpenHashMap<>();

            for(T var1 : param1) {
                int var2 = param0.applyAsInt(var1);
                T var3 = var0.put(var2, var1);
                if (var3 != null) {
                    throw new IllegalArgumentException("Duplicate entry on id " + var2 + ": current=" + var1 + ", previous=" + var3);
                }
            }

            return var0;
        }
    }

    public static <T> IntFunction<T> sparse(ToIntFunction<T> param0, T[] param1, T param2) {
        IntFunction<T> var0 = createMap(param0, param1);
        return param2x -> (T)Objects.requireNonNullElse(var0.apply(param2x), param2);
    }

    private static <T> T[] createSortedArray(ToIntFunction<T> param0, T[] param1) {
        int var0 = param1.length;
        if (var0 == 0) {
            throw new IllegalArgumentException("Empty value list");
        } else {
            T[] var1 = (T[])param1.clone();
            Arrays.fill(var1, null);

            for(T var2 : param1) {
                int var3 = param0.applyAsInt(var2);
                if (var3 < 0 || var3 >= var0) {
                    throw new IllegalArgumentException("Values are not continous, found index " + var3 + " for value " + var2);
                }

                T var4 = var1[var3];
                if (var4 != null) {
                    throw new IllegalArgumentException("Duplicate entry on id " + var3 + ": current=" + var2 + ", previous=" + var4);
                }

                var1[var3] = var2;
            }

            for(int var5 = 0; var5 < var0; ++var5) {
                if (var1[var5] == null) {
                    throw new IllegalArgumentException("Missing value at index: " + var5);
                }
            }

            return var1;
        }
    }

    public static <T> IntFunction<T> continuous(ToIntFunction<T> param0, T[] param1, ByIdMap.OutOfBoundsStrategy param2) {
        T[] var0 = createSortedArray(param0, param1);
        int var1 = var0.length;

        return switch(param2) {
            case ZERO -> {
                T var2 = var0[0];
                yield param3 -> param3 >= 0 && param3 < var1 ? var0[param3] : var2;
            }
            case WRAP -> param2x -> var0[Mth.positiveModulo(param2x, var1)];
            case CLAMP -> param2x -> var0[Mth.clamp(param2x, 0, var1 - 1)];
        };
    }

    public static enum OutOfBoundsStrategy {
        ZERO,
        WRAP,
        CLAMP;
    }
}
