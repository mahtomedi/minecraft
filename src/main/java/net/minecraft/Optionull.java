package net.minecraft;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nullable;

public class Optionull {
    @Nullable
    public static <T, R> R map(@Nullable T param0, Function<T, R> param1) {
        return param0 == null ? null : param1.apply(param0);
    }

    public static <T, R> R mapOrDefault(@Nullable T param0, Function<T, R> param1, R param2) {
        return (R)(param0 == null ? param2 : param1.apply(param0));
    }

    public static <T, R> R mapOrElse(@Nullable T param0, Function<T, R> param1, Supplier<R> param2) {
        return (R)(param0 == null ? param2.get() : param1.apply(param0));
    }

    @Nullable
    public static <T> T first(Collection<T> param0) {
        Iterator<T> var0 = param0.iterator();
        return var0.hasNext() ? var0.next() : null;
    }

    public static <T> T firstOrDefault(Collection<T> param0, T param1) {
        Iterator<T> var0 = param0.iterator();
        return (T)(var0.hasNext() ? var0.next() : param1);
    }

    public static <T> T firstOrElse(Collection<T> param0, Supplier<T> param1) {
        Iterator<T> var0 = param0.iterator();
        return (T)(var0.hasNext() ? var0.next() : param1.get());
    }

    public static <T> boolean isNullOrEmpty(@Nullable T[] param0) {
        return param0 == null || param0.length == 0;
    }

    public static boolean isNullOrEmpty(@Nullable boolean[] param0) {
        return param0 == null || param0.length == 0;
    }

    public static boolean isNullOrEmpty(@Nullable byte[] param0) {
        return param0 == null || param0.length == 0;
    }

    public static boolean isNullOrEmpty(@Nullable char[] param0) {
        return param0 == null || param0.length == 0;
    }

    public static boolean isNullOrEmpty(@Nullable short[] param0) {
        return param0 == null || param0.length == 0;
    }

    public static boolean isNullOrEmpty(@Nullable int[] param0) {
        return param0 == null || param0.length == 0;
    }

    public static boolean isNullOrEmpty(@Nullable long[] param0) {
        return param0 == null || param0.length == 0;
    }

    public static boolean isNullOrEmpty(@Nullable float[] param0) {
        return param0 == null || param0.length == 0;
    }

    public static boolean isNullOrEmpty(@Nullable double[] param0) {
        return param0 == null || param0.length == 0;
    }
}
