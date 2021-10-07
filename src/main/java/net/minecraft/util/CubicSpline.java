package net.minecraft.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.primitives.Floats;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class CubicSpline<C> implements ToFloatFunction<C> {
    private final ToFloatFunction<C> coordinate;
    private final float[] locations;
    private final List<ToFloatFunction<C>> values;
    private final float[] derivatives;

    CubicSpline(ToFloatFunction<C> param0, float[] param1, List<ToFloatFunction<C>> param2, float[] param3) {
        if (param1.length == param2.size() && param1.length == param3.length) {
            this.coordinate = param0;
            this.locations = param1;
            this.values = param2;
            this.derivatives = param3;
        } else {
            throw new IllegalArgumentException("All lengths must be equal, got: " + param1.length + " " + param2.size() + " " + param3.length);
        }
    }

    @Override
    public float apply(C param0) {
        float var0 = this.coordinate.apply(param0);
        int var1 = Mth.binarySearch(0, this.locations.length, param1 -> var0 < this.locations[param1]) - 1;
        int var2 = this.locations.length - 1;
        if (var1 < 0) {
            return this.values.get(0).apply(param0) + this.derivatives[0] * (var0 - this.locations[0]);
        } else if (var1 == var2) {
            return this.values.get(var2).apply(param0) + this.derivatives[var2] * (var0 - this.locations[var2]);
        } else {
            float var3 = this.locations[var1];
            float var4 = this.locations[var1 + 1];
            float var5 = (var0 - var3) / (var4 - var3);
            ToFloatFunction<C> var6 = this.values.get(var1);
            ToFloatFunction<C> var7 = this.values.get(var1 + 1);
            float var8 = this.derivatives[var1];
            float var9 = this.derivatives[var1 + 1];
            float var10 = var6.apply(param0);
            float var11 = var7.apply(param0);
            float var12 = var8 * (var4 - var3) - (var11 - var10);
            float var13 = -var9 * (var4 - var3) + (var11 - var10);
            return Mth.lerp(var5, var10, var11) + var5 * (1.0F - var5) * Mth.lerp(var5, var12, var13);
        }
    }

    public static <C> CubicSpline.Builder<C> builder(ToFloatFunction<C> param0) {
        return new CubicSpline.Builder<>(param0);
    }

    private String toString(float[] param0) {
        return "["
            + (String)IntStream.range(0, param0.length)
                .mapToDouble(param1 -> (double)param0[param1])
                .mapToObj(param0x -> String.format(Locale.ROOT, "%.3f", param0x))
                .collect(Collectors.joining(", "))
            + "]";
    }

    @VisibleForDebug
    protected ToFloatFunction<C> coordinate() {
        return this.coordinate;
    }

    @VisibleForDebug
    public List<Float> debugLocations() {
        return Collections.unmodifiableList(Floats.asList(this.locations));
    }

    @VisibleForDebug
    public ToFloatFunction<C> debugValue(int param0) {
        return this.values.get(param0);
    }

    @VisibleForDebug
    public float debugDerivative(int param0) {
        return this.derivatives[param0];
    }

    @Override
    public String toString() {
        return "Spline{coordinate="
            + this.coordinate
            + ", locations="
            + this.toString(this.locations)
            + ", derivatives="
            + this.toString(this.derivatives)
            + ", values="
            + this.values
            + "}";
    }

    public static final class Builder<C> {
        private final ToFloatFunction<C> coordinate;
        private final FloatList locations = new FloatArrayList();
        private final List<ToFloatFunction<C>> values = Lists.newArrayList();
        private final FloatList derivatives = new FloatArrayList();

        protected Builder(ToFloatFunction<C> param0) {
            this.coordinate = param0;
        }

        public CubicSpline.Builder<C> addPoint(float param0, float param1, float param2) {
            return this.add(param0, new CubicSpline.Constant<>(param1), param2);
        }

        public CubicSpline.Builder<C> addPoint(float param0, ToFloatFunction<C> param1, float param2) {
            return this.add(param0, param1, param2);
        }

        public CubicSpline.Builder<C> addPoint(float param0, CubicSpline<C> param1, float param2) {
            return this.add(param0, param1, param2);
        }

        private CubicSpline.Builder<C> add(float param0, ToFloatFunction<C> param1, float param2) {
            if (!this.locations.isEmpty() && param0 <= this.locations.getFloat(this.locations.size() - 1)) {
                throw new IllegalArgumentException("Please register points in ascending order");
            } else {
                this.locations.add(param0);
                this.values.add(param1);
                this.derivatives.add(param2);
                return this;
            }
        }

        public CubicSpline<C> build() {
            if (this.locations.isEmpty()) {
                throw new IllegalStateException("No elements added");
            } else {
                return new CubicSpline<>(this.coordinate, this.locations.toFloatArray(), ImmutableList.copyOf(this.values), this.derivatives.toFloatArray());
            }
        }
    }

    static class Constant<C> implements ToFloatFunction<C> {
        private final float value;

        public Constant(float param0) {
            this.value = param0;
        }

        @Override
        public float apply(C param0) {
            return this.value;
        }

        @Override
        public String toString() {
            return String.format(Locale.ROOT, "k=%.3f", this.value);
        }
    }
}
