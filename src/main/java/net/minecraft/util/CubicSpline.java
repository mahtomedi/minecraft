package net.minecraft.util;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.lang3.mutable.MutableObject;

public interface CubicSpline<C> extends ToFloatFunction<C> {
    @VisibleForDebug
    String parityString();

    static <C> Codec<CubicSpline<C>> codec(Codec<ToFloatFunction<C>> param0) {
        MutableObject<Codec<CubicSpline<C>>> var0 = new MutableObject<>();

        record Point<C>(float location, CubicSpline<C> value, float derivative) {
        }

        Codec<Point<C>> var1 = RecordCodecBuilder.create(
            param1 -> param1.group(
                        Codec.FLOAT.fieldOf("location").forGetter(Point::location),
                        ExtraCodecs.lazyInitializedCodec(var0::getValue).fieldOf("value").forGetter(Point::value),
                        Codec.FLOAT.fieldOf("derivative").forGetter(Point::derivative)
                    )
                    .apply(param1, (param0x, param1x, param2) -> new Point<>(param0x, param1x, param2))
        );
        Codec<CubicSpline.Multipoint<C>> var2 = RecordCodecBuilder.create(
            param2 -> param2.group(
                        param0.fieldOf("coordinate").forGetter(CubicSpline.Multipoint::coordinate),
                        var1.listOf()
                            .fieldOf("points")
                            .forGetter(
                                param0x -> IntStream.range(0, param0x.locations.length)
                                        .mapToObj(
                                            param1x -> new Point<>(
                                                    param0x.locations()[param1x], (CubicSpline<C>)param0x.values().get(param1x), param0x.derivatives()[param1x]
                                                )
                                        )
                                        .toList()
                            )
                    )
                    .apply(param2, (param0x, param1x) -> {
                        float[] var0x = new float[param1x.size()];
                        ImmutableList.Builder<CubicSpline<C>> var1x = ImmutableList.builder();
                        float[] var2x = new float[param1x.size()];
        
                        for(int var3x = 0; var3x < param1x.size(); ++var3x) {
                            Point<C> var4 = param1x.get(var3x);
                            var0x[var3x] = var4.location();
                            var1x.add(var4.value());
                            var2x[var3x] = var4.derivative();
                        }
        
                        return new CubicSpline.Multipoint<>(param0x, var0x, var1x.build(), var2x);
                    })
        );
        var0.setValue(
            Codec.either(Codec.FLOAT, var2)
                .xmap(
                    param0x -> param0x.map(CubicSpline.Constant::new, param0xx -> param0xx),
                    param0x -> param0x instanceof CubicSpline.Constant var1x ? Either.left(var1x.value()) : Either.right((CubicSpline.Multipoint<C>)param0x)
                )
        );
        return var0.getValue();
    }

    static <C> CubicSpline<C> constant(float param0) {
        return new CubicSpline.Constant<>(param0);
    }

    static <C> CubicSpline.Builder<C> builder(ToFloatFunction<C> param0) {
        return new CubicSpline.Builder<>(param0);
    }

    static <C> CubicSpline.Builder<C> builder(ToFloatFunction<C> param0, ToFloatFunction<Float> param1) {
        return new CubicSpline.Builder<>(param0, param1);
    }

    public static final class Builder<C> {
        private final ToFloatFunction<C> coordinate;
        private final ToFloatFunction<Float> valueTransformer;
        private final FloatList locations = new FloatArrayList();
        private final List<CubicSpline<C>> values = Lists.newArrayList();
        private final FloatList derivatives = new FloatArrayList();

        protected Builder(ToFloatFunction<C> param0) {
            this(param0, param0x -> param0x);
        }

        protected Builder(ToFloatFunction<C> param0, ToFloatFunction<Float> param1) {
            this.coordinate = param0;
            this.valueTransformer = param1;
        }

        public CubicSpline.Builder<C> addPoint(float param0, float param1, float param2) {
            return this.addPoint(param0, new CubicSpline.Constant<>(this.valueTransformer.apply(param1)), param2);
        }

        public CubicSpline.Builder<C> addPoint(float param0, CubicSpline<C> param1, float param2) {
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
                return new CubicSpline.Multipoint<>(
                    this.coordinate, this.locations.toFloatArray(), ImmutableList.copyOf(this.values), this.derivatives.toFloatArray()
                );
            }
        }
    }

    @VisibleForDebug
    public static record Constant<C>(float value) implements CubicSpline<C> {
        @Override
        public float apply(C param0) {
            return this.value;
        }

        @Override
        public String parityString() {
            return String.format("k=%.3f", this.value);
        }
    }

    @VisibleForDebug
    public static record Multipoint<C>(ToFloatFunction<C> coordinate, float[] locations, List<CubicSpline<C>> values, float[] derivatives)
        implements CubicSpline {
        public Multipoint(ToFloatFunction<C> param0, float[] param1, List<CubicSpline<C>> param2, float[] param3) {
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

        @VisibleForTesting
        @Override
        public String parityString() {
            return "Spline{coordinate="
                + this.coordinate
                + ", locations="
                + this.toString(this.locations)
                + ", derivatives="
                + this.toString(this.derivatives)
                + ", values="
                + (String)this.values.stream().map(CubicSpline::parityString).collect(Collectors.joining(", ", "[", "]"))
                + "}";
        }

        private String toString(float[] param0) {
            return "["
                + (String)IntStream.range(0, param0.length)
                    .mapToDouble(param1 -> (double)param0[param1])
                    .mapToObj(param0x -> String.format(Locale.ROOT, "%.3f", param0x))
                    .collect(Collectors.joining(", "))
                + "]";
        }
    }
}
