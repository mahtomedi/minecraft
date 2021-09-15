package net.minecraft.util.profiling.jfr;

import com.google.common.math.Quantiles;
import com.google.common.math.Quantiles.ScaleAndIndexes;
import it.unimi.dsi.fastutil.ints.Int2DoubleRBTreeMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleSortedMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleSortedMaps;
import java.util.Comparator;
import java.util.Map;
import net.minecraft.Util;

public class Percentiles {
    public static final ScaleAndIndexes DEFAULT_INDEXES = Quantiles.scale(100).indexes(50, 75, 90, 99);

    private Percentiles() {
    }

    public static Map<Integer, Double> evaluate(long[] param0) {
        return param0.length == 0 ? Map.of() : sorted(DEFAULT_INDEXES.compute(param0));
    }

    public static Map<Integer, Double> evaluate(double[] param0) {
        return param0.length == 0 ? Map.of() : sorted(DEFAULT_INDEXES.compute(param0));
    }

    private static Map<Integer, Double> sorted(Map<Integer, Double> param0) {
        Int2DoubleSortedMap var0 = Util.make(new Int2DoubleRBTreeMap(Comparator.reverseOrder()), param1 -> param1.putAll(param0));
        return Int2DoubleSortedMaps.unmodifiable(var0);
    }
}
