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

public interface CubicSpline<C, I extends ToFloatFunction<C>> extends ToFloatFunction<C> {
    @VisibleForDebug
    String parityString();

    CubicSpline<C, I> mapAll(CubicSpline.CoordinateVisitor<I> var1);

    static <C, I extends ToFloatFunction<C>> Codec<CubicSpline<C, I>> codec(Codec<I> param0) {
        MutableObject<Codec<CubicSpline<C, I>>> var0 = new MutableObject<>();

        record Point<C, I extends ToFloatFunction<C>>(float location, CubicSpline<C, I> value, float derivative) {
        }

        Codec<Point<C, I>> var1 = RecordCodecBuilder.create(
            param1 -> param1.group(
                        Codec.FLOAT.fieldOf("location").forGetter(Point::location),
                        ExtraCodecs.lazyInitializedCodec(var0::getValue).fieldOf("value").forGetter(Point::value),
                        Codec.FLOAT.fieldOf("derivative").forGetter(Point::derivative)
                    )
                    .apply(param1, (param0x, param1x, param2) -> new Point<>(param0x, param1x, param2))
        );
        Codec<CubicSpline.Multipoint<C, I>> var2 = RecordCodecBuilder.create(
            param2 -> param2.group(
                        param0.fieldOf("coordinate").forGetter(CubicSpline.Multipoint::coordinate),
                        ExtraCodecs.nonEmptyList(var1.listOf())
                            .fieldOf("points")
                            .forGetter(
                                param0x -> IntStream.range(0, param0x.locations.length)
                                        .mapToObj(
                                            param1x -> new Point<>(
                                                    param0x.locations()[param1x],
                                                    (CubicSpline<C, I>)param0x.values().get(param1x),
                                                    param0x.derivatives()[param1x]
                                                )
                                        )
                                        .toList()
                            )
                    )
                    .apply(param2, (param0x, param1x) -> {
                        float[] var0x = new float[param1x.size()];
                        ImmutableList.Builder<CubicSpline<C, I>> var1x = ImmutableList.builder();
                        float[] var2x = new float[param1x.size()];
        
                        for(int var3x = 0; var3x < param1x.size(); ++var3x) {
                            Point<C, I> var4 = param1x.get(var3x);
                            var0x[var3x] = var4.location();
                            var1x.add(var4.value());
                            var2x[var3x] = var4.derivative();
                        }
        
                        return CubicSpline.Multipoint.create(param0x, var0x, var1x.build(), var2x);
                    })
        );
        var0.setValue(
            Codec.either(Codec.FLOAT, var2)
                .xmap(
                    param0x -> param0x.map(CubicSpline.Constant::new, param0xx -> param0xx),
                    param0x -> param0x instanceof CubicSpline.Constant var0x ? Either.left(var0x.value()) : Either.right((CubicSpline.Multipoint<C, I>)param0x)
                )
        );
        return var0.getValue();
    }

    static <C, I extends ToFloatFunction<C>> CubicSpline<C, I> constant(float param0) {
        return new CubicSpline.Constant<>(param0);
    }

    static <C, I extends ToFloatFunction<C>> CubicSpline.Builder<C, I> builder(I param0) {
        return new CubicSpline.Builder<>(param0);
    }

    static <C, I extends ToFloatFunction<C>> CubicSpline.Builder<C, I> builder(I param0, ToFloatFunction<Float> param1) {
        return new CubicSpline.Builder<>(param0, param1);
    }

    public static final class Builder<C, I extends ToFloatFunction<C>> {
        private final I coordinate;
        private final ToFloatFunction<Float> valueTransformer;
        private final FloatList locations = new FloatArrayList();
        private final List<CubicSpline<C, I>> values = Lists.newArrayList();
        private final FloatList derivatives = new FloatArrayList();

        protected Builder(I param0) {
            this(param0, ToFloatFunction.IDENTITY);
        }

        protected Builder(I param0, ToFloatFunction<Float> param1) {
            this.coordinate = param0;
            this.valueTransformer = param1;
        }

        public CubicSpline.Builder<C, I> addPoint(float param0, float param1) {
            return this.addPoint(param0, new CubicSpline.Constant<>(this.valueTransformer.apply(param1)), 0.0F);
        }

        public CubicSpline.Builder<C, I> addPoint(float param0, float param1, float param2) {
            return this.addPoint(param0, new CubicSpline.Constant<>(this.valueTransformer.apply(param1)), param2);
        }

        public CubicSpline.Builder<C, I> addPoint(float param0, CubicSpline<C, I> param1) {
            return this.addPoint(param0, param1, 0.0F);
        }

        private CubicSpline.Builder<C, I> addPoint(float param0, CubicSpline<C, I> param1, float param2) {
            if (!this.locations.isEmpty() && param0 <= this.locations.getFloat(this.locations.size() - 1)) {
                throw new IllegalArgumentException("Please register points in ascending order");
            } else {
                this.locations.add(param0);
                this.values.add(param1);
                this.derivatives.add(param2);
                return this;
            }
        }

        public CubicSpline<C, I> build() {
            if (this.locations.isEmpty()) {
                throw new IllegalStateException("No elements added");
            } else {
                return CubicSpline.Multipoint.create(
                    this.coordinate, this.locations.toFloatArray(), ImmutableList.copyOf(this.values), this.derivatives.toFloatArray()
                );
            }
        }
    }

    @VisibleForDebug
    public static record Constant<C, I extends ToFloatFunction<C>>(float value) implements CubicSpline<C, I> {
        @Override
        public float apply(C param0) {
            return this.value;
        }

        @Override
        public String parityString() {
            return String.format(Locale.ROOT, "k=%.3f", this.value);
        }

        @Override
        public float minValue() {
            return this.value;
        }

        @Override
        public float maxValue() {
            return this.value;
        }

        @Override
        public CubicSpline<C, I> mapAll(CubicSpline.CoordinateVisitor<I> param0) {
            return this;
        }
    }

    public interface CoordinateVisitor<I> {
        I visit(I var1);
    }

    @VisibleForDebug
    public static record Multipoint<C, I extends ToFloatFunction<C>>(
        I coordinate, float[] locations, List<CubicSpline<C, I>> values, float[] derivatives, float minValue, float maxValue
    ) implements CubicSpline<C, I> {
        public Multipoint(I param0, float[] param1, List<CubicSpline<C, I>> param2, float[] param3, float param4, float param5) {
            validateSizes(param1, param2, param3);
            this.coordinate = param0;
            this.locations = param1;
            this.values = param2;
            this.derivatives = param3;
            this.minValue = param4;
            this.maxValue = param5;
        }

        static <C, I extends ToFloatFunction<C>> CubicSpline.Multipoint<C, I> create(I param0, float[] param1, List<CubicSpline<C, I>> param2, float[] param3) {
            validateSizes(param1, param2, param3);
            int var0 = param1.length - 1;
            float var1 = Float.POSITIVE_INFINITY;
            float var2 = Float.NEGATIVE_INFINITY;
            float var3 = param0.minValue();
            float var4 = param0.maxValue();
            if (var3 < param1[0]) {
                float var5 = linearExtend(var3, param1, param2.get(0).minValue(), param3, 0);
                float var6 = linearExtend(var3, param1, param2.get(0).maxValue(), param3, 0);
                var1 = Math.min(var1, Math.min(var5, var6));
                var2 = Math.max(var2, Math.max(var5, var6));
            }

            if (var4 > param1[var0]) {
                float var7 = linearExtend(var4, param1, param2.get(var0).minValue(), param3, var0);
                float var8 = linearExtend(var4, param1, param2.get(var0).maxValue(), param3, var0);
                var1 = Math.min(var1, Math.min(var7, var8));
                var2 = Math.max(var2, Math.max(var7, var8));
            }

            for(CubicSpline<C, I> var9 : param2) {
                var1 = Math.min(var1, var9.minValue());
                var2 = Math.max(var2, var9.maxValue());
            }

            for(int var10 = 0; var10 < var0; ++var10) {
                float var11 = param1[var10];
                float var12 = param1[var10 + 1];
                float var13 = var12 - var11;
                CubicSpline<C, I> var14 = param2.get(var10);
                CubicSpline<C, I> var15 = param2.get(var10 + 1);
                float var16 = var14.minValue();
                float var17 = var14.maxValue();
                float var18 = var15.minValue();
                float var19 = var15.maxValue();
                float var20 = param3[var10];
                float var21 = param3[var10 + 1];
                if (var20 != 0.0F || var21 != 0.0F) {
                    float var22 = var20 * var13;
                    float var23 = var21 * var13;
                    float var24 = Math.min(var16, var18);
                    float var25 = Math.max(var17, var19);
                    float var26 = var22 - var19 + var16;
                    float var27 = var22 - var18 + var17;
                    float var28 = -var23 + var18 - var17;
                    float var29 = -var23 + var19 - var16;
                    float var30 = Math.min(var26, var28);
                    float var31 = Math.max(var27, var29);
                    var1 = Math.min(var1, var24 + 0.25F * var30);
                    var2 = Math.max(var2, var25 + 0.25F * var31);
                }
            }

            return new CubicSpline.Multipoint<>(param0, param1, param2, param3, var1, var2);
        }

        private static float linearExtend(float param0, float[] param1, float param2, float[] param3, int param4) {
            float var0 = param3[param4];
            return var0 == 0.0F ? param2 : param2 + var0 * (param0 - param1[param4]);
        }

        private static <C, I extends ToFloatFunction<C>> void validateSizes(float[] param0, List<CubicSpline<C, I>> param1, float[] param2) {
            if (param0.length != param1.size() || param0.length != param2.length) {
                throw new IllegalArgumentException("All lengths must be equal, got: " + param0.length + " " + param1.size() + " " + param2.length);
            } else if (param0.length == 0) {
                throw new IllegalArgumentException("Cannot create a multipoint spline with no points");
            }
        }

        @Override
        public float apply(C param0) {
            float var0 = this.coordinate.apply(param0);
            int var1 = findIntervalStart(this.locations, var0);
            int var2 = this.locations.length - 1;
            if (var1 < 0) {
                return linearExtend(var0, this.locations, this.values.get(0).apply(param0), this.derivatives, 0);
            } else if (var1 == var2) {
                return linearExtend(var0, this.locations, this.values.get(var2).apply(param0), this.derivatives, var2);
            } else {
                float var3 = this.locations[var1];
                float var4 = this.locations[var1 + 1];
                float var5 = (var0 - var3) / (var4 - var3);
                ToFloatFunction<C> var6 = (ToFloatFunction)this.values.get(var1);
                ToFloatFunction<C> var7 = (ToFloatFunction)this.values.get(var1 + 1);
                float var8 = this.derivatives[var1];
                float var9 = this.derivatives[var1 + 1];
                float var10 = var6.apply(param0);
                float var11 = var7.apply(param0);
                float var12 = var8 * (var4 - var3) - (var11 - var10);
                float var13 = -var9 * (var4 - var3) + (var11 - var10);
                return Mth.lerp(var5, var10, var11) + var5 * (1.0F - var5) * Mth.lerp(var5, var12, var13);
            }
        }

        private static int findIntervalStart(float[] param0, float param1) {
            return Mth.binarySearch(0, param0.length, param2 -> param1 < param0[param2]) - 1;
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

        @Override
        public CubicSpline<C, I> mapAll(CubicSpline.CoordinateVisitor<I> param0) {
            return create(param0.visit(this.coordinate), this.locations, this.values().stream().map(param1 -> param1.mapAll(param0)).toList(), this.derivatives);
        }
    }
}
