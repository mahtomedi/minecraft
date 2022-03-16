package net.minecraft.world.level.levelgen;

import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.CubicSpline;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.ToFloatFunction;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.synth.BlendedNoise;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import org.slf4j.Logger;

public final class DensityFunctions {
    private static final Codec<DensityFunction> CODEC = Registry.DENSITY_FUNCTION_TYPES.byNameCodec().dispatch(DensityFunction::codec, Function.identity());
    protected static final double MAX_REASONABLE_NOISE_VALUE = 1000000.0;
    static final Codec<Double> NOISE_VALUE_CODEC = Codec.doubleRange(-1000000.0, 1000000.0);
    public static final Codec<DensityFunction> DIRECT_CODEC = Codec.either(NOISE_VALUE_CODEC, CODEC)
        .xmap(
            param0 -> param0.map(DensityFunctions::constant, Function.identity()),
            param0 -> param0 instanceof DensityFunctions.Constant var0 ? Either.left(var0.value()) : Either.right(param0)
        );

    public static Codec<? extends DensityFunction> bootstrap(Registry<Codec<? extends DensityFunction>> param0) {
        register(param0, "blend_alpha", DensityFunctions.BlendAlpha.CODEC);
        register(param0, "blend_offset", DensityFunctions.BlendOffset.CODEC);
        register(param0, "beardifier", DensityFunctions.BeardifierMarker.CODEC);
        register(param0, "old_blended_noise", BlendedNoise.CODEC);

        for(DensityFunctions.Marker.Type var0 : DensityFunctions.Marker.Type.values()) {
            register(param0, var0.getSerializedName(), var0.codec);
        }

        register(param0, "noise", DensityFunctions.Noise.CODEC);
        register(param0, "end_islands", DensityFunctions.EndIslandDensityFunction.CODEC);
        register(param0, "weird_scaled_sampler", DensityFunctions.WeirdScaledSampler.CODEC);
        register(param0, "shifted_noise", DensityFunctions.ShiftedNoise.CODEC);
        register(param0, "range_choice", DensityFunctions.RangeChoice.CODEC);
        register(param0, "shift_a", DensityFunctions.ShiftA.CODEC);
        register(param0, "shift_b", DensityFunctions.ShiftB.CODEC);
        register(param0, "shift", DensityFunctions.Shift.CODEC);
        register(param0, "blend_density", DensityFunctions.BlendDensity.CODEC);
        register(param0, "clamp", DensityFunctions.Clamp.CODEC);

        for(DensityFunctions.Mapped.Type var1 : DensityFunctions.Mapped.Type.values()) {
            register(param0, var1.getSerializedName(), var1.codec);
        }

        register(param0, "slide", DensityFunctions.Slide.CODEC);

        for(DensityFunctions.TwoArgumentSimpleFunction.Type var2 : DensityFunctions.TwoArgumentSimpleFunction.Type.values()) {
            register(param0, var2.getSerializedName(), var2.codec);
        }

        register(param0, "spline", DensityFunctions.Spline.CODEC);
        register(param0, "constant", DensityFunctions.Constant.CODEC);
        return register(param0, "y_clamped_gradient", DensityFunctions.YClampedGradient.CODEC);
    }

    private static Codec<? extends DensityFunction> register(
        Registry<Codec<? extends DensityFunction>> param0, String param1, Codec<? extends DensityFunction> param2
    ) {
        return Registry.register(param0, param1, param2);
    }

    static <A, O> Codec<O> singleArgumentCodec(Codec<A> param0, Function<A, O> param1, Function<O, A> param2) {
        return param0.fieldOf("argument").xmap(param1, param2).codec();
    }

    static <O> Codec<O> singleFunctionArgumentCodec(Function<DensityFunction, O> param0, Function<O, DensityFunction> param1) {
        return singleArgumentCodec(DensityFunction.HOLDER_HELPER_CODEC, param0, param1);
    }

    static <O> Codec<O> doubleFunctionArgumentCodec(
        BiFunction<DensityFunction, DensityFunction, O> param0, Function<O, DensityFunction> param1, Function<O, DensityFunction> param2
    ) {
        return RecordCodecBuilder.create(
            param3 -> param3.group(
                        DensityFunction.HOLDER_HELPER_CODEC.fieldOf("argument1").forGetter(param1),
                        DensityFunction.HOLDER_HELPER_CODEC.fieldOf("argument2").forGetter(param2)
                    )
                    .apply(param3, param0)
        );
    }

    static <O> Codec<O> makeCodec(MapCodec<O> param0) {
        return param0.codec();
    }

    private DensityFunctions() {
    }

    public static DensityFunction interpolated(DensityFunction param0) {
        return new DensityFunctions.Marker(DensityFunctions.Marker.Type.Interpolated, param0);
    }

    public static DensityFunction flatCache(DensityFunction param0) {
        return new DensityFunctions.Marker(DensityFunctions.Marker.Type.FlatCache, param0);
    }

    public static DensityFunction cache2d(DensityFunction param0) {
        return new DensityFunctions.Marker(DensityFunctions.Marker.Type.Cache2D, param0);
    }

    public static DensityFunction cacheOnce(DensityFunction param0) {
        return new DensityFunctions.Marker(DensityFunctions.Marker.Type.CacheOnce, param0);
    }

    public static DensityFunction cacheAllInCell(DensityFunction param0) {
        return new DensityFunctions.Marker(DensityFunctions.Marker.Type.CacheAllInCell, param0);
    }

    public static DensityFunction mappedNoise(
        Holder<NormalNoise.NoiseParameters> param0, @Deprecated double param1, double param2, double param3, double param4
    ) {
        return mapFromUnitTo(new DensityFunctions.Noise(param0, null, param1, param2), param3, param4);
    }

    public static DensityFunction mappedNoise(Holder<NormalNoise.NoiseParameters> param0, double param1, double param2, double param3) {
        return mappedNoise(param0, 1.0, param1, param2, param3);
    }

    public static DensityFunction mappedNoise(Holder<NormalNoise.NoiseParameters> param0, double param1, double param2) {
        return mappedNoise(param0, 1.0, 1.0, param1, param2);
    }

    public static DensityFunction shiftedNoise2d(DensityFunction param0, DensityFunction param1, double param2, Holder<NormalNoise.NoiseParameters> param3) {
        return new DensityFunctions.ShiftedNoise(param0, zero(), param1, param2, 0.0, param3, null);
    }

    public static DensityFunction noise(Holder<NormalNoise.NoiseParameters> param0) {
        return noise(param0, 1.0, 1.0);
    }

    public static DensityFunction noise(Holder<NormalNoise.NoiseParameters> param0, double param1, double param2) {
        return new DensityFunctions.Noise(param0, null, param1, param2);
    }

    public static DensityFunction noise(Holder<NormalNoise.NoiseParameters> param0, double param1) {
        return noise(param0, 1.0, param1);
    }

    public static DensityFunction rangeChoice(DensityFunction param0, double param1, double param2, DensityFunction param3, DensityFunction param4) {
        return new DensityFunctions.RangeChoice(param0, param1, param2, param3, param4);
    }

    public static DensityFunction shiftA(Holder<NormalNoise.NoiseParameters> param0) {
        return new DensityFunctions.ShiftA(param0, null);
    }

    public static DensityFunction shiftB(Holder<NormalNoise.NoiseParameters> param0) {
        return new DensityFunctions.ShiftB(param0, null);
    }

    public static DensityFunction shift(Holder<NormalNoise.NoiseParameters> param0) {
        return new DensityFunctions.Shift(param0, null);
    }

    public static DensityFunction blendDensity(DensityFunction param0) {
        return new DensityFunctions.BlendDensity(param0);
    }

    public static DensityFunction endIslands(long param0) {
        return new DensityFunctions.EndIslandDensityFunction(param0);
    }

    public static DensityFunction weirdScaledSampler(
        DensityFunction param0, Holder<NormalNoise.NoiseParameters> param1, DensityFunctions.WeirdScaledSampler.RarityValueMapper param2
    ) {
        return new DensityFunctions.WeirdScaledSampler(param0, param1, null, param2);
    }

    public static DensityFunction slide(NoiseSettings param0, DensityFunction param1) {
        return new DensityFunctions.Slide(param0, param1);
    }

    public static DensityFunction add(DensityFunction param0, DensityFunction param1) {
        return DensityFunctions.TwoArgumentSimpleFunction.create(DensityFunctions.TwoArgumentSimpleFunction.Type.ADD, param0, param1);
    }

    public static DensityFunction mul(DensityFunction param0, DensityFunction param1) {
        return DensityFunctions.TwoArgumentSimpleFunction.create(DensityFunctions.TwoArgumentSimpleFunction.Type.MUL, param0, param1);
    }

    public static DensityFunction min(DensityFunction param0, DensityFunction param1) {
        return DensityFunctions.TwoArgumentSimpleFunction.create(DensityFunctions.TwoArgumentSimpleFunction.Type.MIN, param0, param1);
    }

    public static DensityFunction max(DensityFunction param0, DensityFunction param1) {
        return DensityFunctions.TwoArgumentSimpleFunction.create(DensityFunctions.TwoArgumentSimpleFunction.Type.MAX, param0, param1);
    }

    public static DensityFunction spline(CubicSpline<DensityFunctions.Spline.Point, DensityFunctions.Spline.Coordinate> param0) {
        return new DensityFunctions.Spline(param0);
    }

    public static DensityFunction zero() {
        return DensityFunctions.Constant.ZERO;
    }

    public static DensityFunction constant(double param0) {
        return new DensityFunctions.Constant(param0);
    }

    public static DensityFunction yClampedGradient(int param0, int param1, double param2, double param3) {
        return new DensityFunctions.YClampedGradient(param0, param1, param2, param3);
    }

    public static DensityFunction map(DensityFunction param0, DensityFunctions.Mapped.Type param1) {
        return DensityFunctions.Mapped.create(param1, param0);
    }

    private static DensityFunction mapFromUnitTo(DensityFunction param0, double param1, double param2) {
        double var0 = (param1 + param2) * 0.5;
        double var1 = (param2 - param1) * 0.5;
        return add(constant(var0), mul(constant(var1), param0));
    }

    public static DensityFunction blendAlpha() {
        return DensityFunctions.BlendAlpha.INSTANCE;
    }

    public static DensityFunction blendOffset() {
        return DensityFunctions.BlendOffset.INSTANCE;
    }

    public static DensityFunction lerp(DensityFunction param0, DensityFunction param1, DensityFunction param2) {
        DensityFunction var0 = cacheOnce(param0);
        DensityFunction var1 = add(mul(var0, constant(-1.0)), constant(1.0));
        return add(mul(param1, var1), mul(param2, var0));
    }

    static record Ap2(
        DensityFunctions.TwoArgumentSimpleFunction.Type type, DensityFunction argument1, DensityFunction argument2, double minValue, double maxValue
    ) implements DensityFunctions.TwoArgumentSimpleFunction {
        @Override
        public double compute(DensityFunction.FunctionContext param0) {
            double var0 = this.argument1.compute(param0);

            return switch(this.type) {
                case ADD -> var0 + this.argument2.compute(param0);
                case MAX -> var0 > this.argument2.maxValue() ? var0 : Math.max(var0, this.argument2.compute(param0));
                case MIN -> var0 < this.argument2.minValue() ? var0 : Math.min(var0, this.argument2.compute(param0));
                case MUL -> var0 == 0.0 ? 0.0 : var0 * this.argument2.compute(param0);
            };
        }

        @Override
        public void fillArray(double[] param0, DensityFunction.ContextProvider param1) {
            this.argument1.fillArray(param0, param1);
            switch(this.type) {
                case ADD:
                    double[] var0 = new double[param0.length];
                    this.argument2.fillArray(var0, param1);

                    for(int var1 = 0; var1 < param0.length; ++var1) {
                        param0[var1] += var0[var1];
                    }
                    break;
                case MAX:
                    double var7 = this.argument2.maxValue();

                    for(int var8 = 0; var8 < param0.length; ++var8) {
                        double var9 = param0[var8];
                        param0[var8] = var9 > var7 ? var9 : Math.max(var9, this.argument2.compute(param1.forIndex(var8)));
                    }
                    break;
                case MIN:
                    double var4 = this.argument2.minValue();

                    for(int var5 = 0; var5 < param0.length; ++var5) {
                        double var6 = param0[var5];
                        param0[var5] = var6 < var4 ? var6 : Math.min(var6, this.argument2.compute(param1.forIndex(var5)));
                    }
                    break;
                case MUL:
                    for(int var2 = 0; var2 < param0.length; ++var2) {
                        double var3 = param0[var2];
                        param0[var2] = var3 == 0.0 ? 0.0 : var3 * this.argument2.compute(param1.forIndex(var2));
                    }
            }

        }

        @Override
        public DensityFunction mapAll(DensityFunction.Visitor param0) {
            return param0.apply(DensityFunctions.TwoArgumentSimpleFunction.create(this.type, this.argument1.mapAll(param0), this.argument2.mapAll(param0)));
        }
    }

    protected static enum BeardifierMarker implements DensityFunctions.BeardifierOrMarker {
        INSTANCE;

        @Override
        public double compute(DensityFunction.FunctionContext param0) {
            return 0.0;
        }

        @Override
        public void fillArray(double[] param0, DensityFunction.ContextProvider param1) {
            Arrays.fill(param0, 0.0);
        }

        @Override
        public double minValue() {
            return 0.0;
        }

        @Override
        public double maxValue() {
            return 0.0;
        }
    }

    public interface BeardifierOrMarker extends DensityFunction.SimpleFunction {
        Codec<DensityFunction> CODEC = Codec.unit(DensityFunctions.BeardifierMarker.INSTANCE);

        @Override
        default Codec<? extends DensityFunction> codec() {
            return CODEC;
        }
    }

    protected static enum BlendAlpha implements DensityFunction.SimpleFunction {
        INSTANCE;

        public static final Codec<DensityFunction> CODEC = Codec.unit(INSTANCE);

        @Override
        public double compute(DensityFunction.FunctionContext param0) {
            return 1.0;
        }

        @Override
        public void fillArray(double[] param0, DensityFunction.ContextProvider param1) {
            Arrays.fill(param0, 1.0);
        }

        @Override
        public double minValue() {
            return 1.0;
        }

        @Override
        public double maxValue() {
            return 1.0;
        }

        @Override
        public Codec<? extends DensityFunction> codec() {
            return CODEC;
        }
    }

    static record BlendDensity(DensityFunction input) implements DensityFunctions.TransformerWithContext {
        static final Codec<DensityFunctions.BlendDensity> CODEC = DensityFunctions.singleFunctionArgumentCodec(
            DensityFunctions.BlendDensity::new, DensityFunctions.BlendDensity::input
        );

        @Override
        public double transform(DensityFunction.FunctionContext param0, double param1) {
            return param0.getBlender().blendDensity(param0, param1);
        }

        @Override
        public DensityFunction mapAll(DensityFunction.Visitor param0) {
            return param0.apply(new DensityFunctions.BlendDensity(this.input.mapAll(param0)));
        }

        @Override
        public double minValue() {
            return Double.NEGATIVE_INFINITY;
        }

        @Override
        public double maxValue() {
            return Double.POSITIVE_INFINITY;
        }

        @Override
        public Codec<? extends DensityFunction> codec() {
            return CODEC;
        }
    }

    protected static enum BlendOffset implements DensityFunction.SimpleFunction {
        INSTANCE;

        public static final Codec<DensityFunction> CODEC = Codec.unit(INSTANCE);

        @Override
        public double compute(DensityFunction.FunctionContext param0) {
            return 0.0;
        }

        @Override
        public void fillArray(double[] param0, DensityFunction.ContextProvider param1) {
            Arrays.fill(param0, 0.0);
        }

        @Override
        public double minValue() {
            return 0.0;
        }

        @Override
        public double maxValue() {
            return 0.0;
        }

        @Override
        public Codec<? extends DensityFunction> codec() {
            return CODEC;
        }
    }

    protected static record Clamp(DensityFunction input, double minValue, double maxValue) implements DensityFunctions.PureTransformer {
        private static final MapCodec<DensityFunctions.Clamp> DATA_CODEC = RecordCodecBuilder.mapCodec(
            param0 -> param0.group(
                        DensityFunction.DIRECT_CODEC.fieldOf("input").forGetter(DensityFunctions.Clamp::input),
                        DensityFunctions.NOISE_VALUE_CODEC.fieldOf("min").forGetter(DensityFunctions.Clamp::minValue),
                        DensityFunctions.NOISE_VALUE_CODEC.fieldOf("max").forGetter(DensityFunctions.Clamp::maxValue)
                    )
                    .apply(param0, DensityFunctions.Clamp::new)
        );
        public static final Codec<DensityFunctions.Clamp> CODEC = DensityFunctions.makeCodec(DATA_CODEC);

        @Override
        public double transform(double param0) {
            return Mth.clamp(param0, this.minValue, this.maxValue);
        }

        @Override
        public DensityFunction mapAll(DensityFunction.Visitor param0) {
            return new DensityFunctions.Clamp(this.input.mapAll(param0), this.minValue, this.maxValue);
        }

        @Override
        public Codec<? extends DensityFunction> codec() {
            return CODEC;
        }
    }

    static record Constant(double value) implements DensityFunction.SimpleFunction {
        static final Codec<DensityFunctions.Constant> CODEC = DensityFunctions.singleArgumentCodec(
            DensityFunctions.NOISE_VALUE_CODEC, DensityFunctions.Constant::new, DensityFunctions.Constant::value
        );
        static final DensityFunctions.Constant ZERO = new DensityFunctions.Constant(0.0);

        @Override
        public double compute(DensityFunction.FunctionContext param0) {
            return this.value;
        }

        @Override
        public void fillArray(double[] param0, DensityFunction.ContextProvider param1) {
            Arrays.fill(param0, this.value);
        }

        @Override
        public double minValue() {
            return this.value;
        }

        @Override
        public double maxValue() {
            return this.value;
        }

        @Override
        public Codec<? extends DensityFunction> codec() {
            return CODEC;
        }
    }

    protected static final class EndIslandDensityFunction implements DensityFunction.SimpleFunction {
        public static final Codec<DensityFunctions.EndIslandDensityFunction> CODEC = Codec.unit(new DensityFunctions.EndIslandDensityFunction(0L));
        private static final float ISLAND_THRESHOLD = -0.9F;
        private final SimplexNoise islandNoise;

        public EndIslandDensityFunction(long param0) {
            RandomSource var0 = new LegacyRandomSource(param0);
            var0.consumeCount(17292);
            this.islandNoise = new SimplexNoise(var0);
        }

        private static float getHeightValue(SimplexNoise param0, int param1, int param2) {
            int var0 = param1 / 2;
            int var1 = param2 / 2;
            int var2 = param1 % 2;
            int var3 = param2 % 2;
            float var4 = 100.0F - Mth.sqrt((float)(param1 * param1 + param2 * param2)) * 8.0F;
            var4 = Mth.clamp(var4, -100.0F, 80.0F);

            for(int var5 = -12; var5 <= 12; ++var5) {
                for(int var6 = -12; var6 <= 12; ++var6) {
                    long var7 = (long)(var0 + var5);
                    long var8 = (long)(var1 + var6);
                    if (var7 * var7 + var8 * var8 > 4096L && param0.getValue((double)var7, (double)var8) < -0.9F) {
                        float var9 = (Mth.abs((float)var7) * 3439.0F + Mth.abs((float)var8) * 147.0F) % 13.0F + 9.0F;
                        float var10 = (float)(var2 - var5 * 2);
                        float var11 = (float)(var3 - var6 * 2);
                        float var12 = 100.0F - Mth.sqrt(var10 * var10 + var11 * var11) * var9;
                        var12 = Mth.clamp(var12, -100.0F, 80.0F);
                        var4 = Math.max(var4, var12);
                    }
                }
            }

            return var4;
        }

        @Override
        public double compute(DensityFunction.FunctionContext param0) {
            return ((double)getHeightValue(this.islandNoise, param0.blockX() / 8, param0.blockZ() / 8) - 8.0) / 128.0;
        }

        @Override
        public double minValue() {
            return -0.84375;
        }

        @Override
        public double maxValue() {
            return 0.5625;
        }

        @Override
        public Codec<? extends DensityFunction> codec() {
            return CODEC;
        }
    }

    @VisibleForDebug
    public static record HolderHolder(Holder<DensityFunction> function) implements DensityFunction {
        @Override
        public double compute(DensityFunction.FunctionContext param0) {
            return this.function.value().compute(param0);
        }

        @Override
        public void fillArray(double[] param0, DensityFunction.ContextProvider param1) {
            this.function.value().fillArray(param0, param1);
        }

        @Override
        public DensityFunction mapAll(DensityFunction.Visitor param0) {
            return param0.apply(new DensityFunctions.HolderHolder(new Holder.Direct<>(this.function.value().mapAll(param0))));
        }

        @Override
        public double minValue() {
            return this.function.value().minValue();
        }

        @Override
        public double maxValue() {
            return this.function.value().maxValue();
        }

        @Override
        public Codec<? extends DensityFunction> codec() {
            throw new UnsupportedOperationException("Calling .codec() on HolderHolder");
        }
    }

    protected static record Mapped(DensityFunctions.Mapped.Type type, DensityFunction input, double minValue, double maxValue)
        implements DensityFunctions.PureTransformer {
        public static DensityFunctions.Mapped create(DensityFunctions.Mapped.Type param0, DensityFunction param1) {
            double var0 = param1.minValue();
            double var1 = transform(param0, var0);
            double var2 = transform(param0, param1.maxValue());
            return param0 != DensityFunctions.Mapped.Type.ABS && param0 != DensityFunctions.Mapped.Type.SQUARE
                ? new DensityFunctions.Mapped(param0, param1, var1, var2)
                : new DensityFunctions.Mapped(param0, param1, Math.max(0.0, var0), Math.max(var1, var2));
        }

        private static double transform(DensityFunctions.Mapped.Type param0, double param1) {
            return switch(param0) {
                case ABS -> Math.abs(param1);
                case SQUARE -> param1 * param1;
                case CUBE -> param1 * param1 * param1;
                case HALF_NEGATIVE -> param1 > 0.0 ? param1 : param1 * 0.5;
                case QUARTER_NEGATIVE -> param1 > 0.0 ? param1 : param1 * 0.25;
                case SQUEEZE -> {
                    double var0 = Mth.clamp(param1, -1.0, 1.0);
                    yield var0 / 2.0 - var0 * var0 * var0 / 24.0;
                }
            };
        }

        @Override
        public double transform(double param0) {
            return transform(this.type, param0);
        }

        public DensityFunctions.Mapped mapAll(DensityFunction.Visitor param0) {
            return create(this.type, this.input.mapAll(param0));
        }

        @Override
        public Codec<? extends DensityFunction> codec() {
            return this.type.codec;
        }

        static enum Type implements StringRepresentable {
            ABS("abs"),
            SQUARE("square"),
            CUBE("cube"),
            HALF_NEGATIVE("half_negative"),
            QUARTER_NEGATIVE("quarter_negative"),
            SQUEEZE("squeeze");

            private final String name;
            final Codec<DensityFunctions.Mapped> codec = DensityFunctions.singleFunctionArgumentCodec(
                param0x -> DensityFunctions.Mapped.create(this, param0x), DensityFunctions.Mapped::input
            );

            private Type(String param0) {
                this.name = param0;
            }

            @Override
            public String getSerializedName() {
                return this.name;
            }
        }
    }

    protected static record Marker(DensityFunctions.Marker.Type type, DensityFunction wrapped) implements DensityFunctions.MarkerOrMarked {
        @Override
        public double compute(DensityFunction.FunctionContext param0) {
            return this.wrapped.compute(param0);
        }

        @Override
        public void fillArray(double[] param0, DensityFunction.ContextProvider param1) {
            this.wrapped.fillArray(param0, param1);
        }

        @Override
        public DensityFunction mapAll(DensityFunction.Visitor param0) {
            return param0.apply(new DensityFunctions.Marker(this.type, this.wrapped.mapAll(param0)));
        }

        @Override
        public double minValue() {
            return this.wrapped.minValue();
        }

        @Override
        public double maxValue() {
            return this.wrapped.maxValue();
        }

        static enum Type implements StringRepresentable {
            Interpolated("interpolated"),
            FlatCache("flat_cache"),
            Cache2D("cache_2d"),
            CacheOnce("cache_once"),
            CacheAllInCell("cache_all_in_cell");

            private final String name;
            final Codec<DensityFunctions.MarkerOrMarked> codec = DensityFunctions.singleFunctionArgumentCodec(
                param0x -> new DensityFunctions.Marker(this, param0x), DensityFunctions.MarkerOrMarked::wrapped
            );

            private Type(String param0) {
                this.name = param0;
            }

            @Override
            public String getSerializedName() {
                return this.name;
            }
        }
    }

    public interface MarkerOrMarked extends DensityFunction {
        DensityFunctions.Marker.Type type();

        DensityFunction wrapped();

        @Override
        default Codec<? extends DensityFunction> codec() {
            return this.type().codec;
        }
    }

    static record MulOrAdd(DensityFunctions.MulOrAdd.Type specificType, DensityFunction input, double minValue, double maxValue, double argument)
        implements DensityFunctions.PureTransformer,
        DensityFunctions.TwoArgumentSimpleFunction {
        @Override
        public DensityFunctions.TwoArgumentSimpleFunction.Type type() {
            return this.specificType == DensityFunctions.MulOrAdd.Type.MUL
                ? DensityFunctions.TwoArgumentSimpleFunction.Type.MUL
                : DensityFunctions.TwoArgumentSimpleFunction.Type.ADD;
        }

        @Override
        public DensityFunction argument1() {
            return DensityFunctions.constant(this.argument);
        }

        @Override
        public DensityFunction argument2() {
            return this.input;
        }

        @Override
        public double transform(double param0) {
            return switch(this.specificType) {
                case MUL -> param0 * this.argument;
                case ADD -> param0 + this.argument;
            };
        }

        @Override
        public DensityFunction mapAll(DensityFunction.Visitor param0) {
            DensityFunction var0 = this.input.mapAll(param0);
            double var1 = var0.minValue();
            double var2 = var0.maxValue();
            double var3;
            double var4;
            if (this.specificType == DensityFunctions.MulOrAdd.Type.ADD) {
                var3 = var1 + this.argument;
                var4 = var2 + this.argument;
            } else if (this.argument >= 0.0) {
                var3 = var1 * this.argument;
                var4 = var2 * this.argument;
            } else {
                var3 = var2 * this.argument;
                var4 = var1 * this.argument;
            }

            return new DensityFunctions.MulOrAdd(this.specificType, var0, var3, var4, this.argument);
        }

        static enum Type {
            MUL,
            ADD;
        }
    }

    protected static record Noise(Holder<NormalNoise.NoiseParameters> noiseData, @Nullable NormalNoise noise, @Deprecated double xzScale, double yScale)
        implements DensityFunction.SimpleFunction {
        public static final MapCodec<DensityFunctions.Noise> DATA_CODEC = RecordCodecBuilder.mapCodec(
            param0 -> param0.group(
                        NormalNoise.NoiseParameters.CODEC.fieldOf("noise").forGetter(DensityFunctions.Noise::noiseData),
                        Codec.DOUBLE.fieldOf("xz_scale").forGetter(DensityFunctions.Noise::xzScale),
                        Codec.DOUBLE.fieldOf("y_scale").forGetter(DensityFunctions.Noise::yScale)
                    )
                    .apply(param0, DensityFunctions.Noise::createUnseeded)
        );
        public static final Codec<DensityFunctions.Noise> CODEC = DensityFunctions.makeCodec(DATA_CODEC);

        public static DensityFunctions.Noise createUnseeded(Holder<NormalNoise.NoiseParameters> param0, @Deprecated double param1, double param2) {
            return new DensityFunctions.Noise(param0, null, param1, param2);
        }

        @Override
        public double compute(DensityFunction.FunctionContext param0) {
            return this.noise == null
                ? 0.0
                : this.noise.getValue((double)param0.blockX() * this.xzScale, (double)param0.blockY() * this.yScale, (double)param0.blockZ() * this.xzScale);
        }

        @Override
        public double minValue() {
            return -this.maxValue();
        }

        @Override
        public double maxValue() {
            return this.noise == null ? 2.0 : this.noise.maxValue();
        }

        @Override
        public Codec<? extends DensityFunction> codec() {
            return CODEC;
        }
    }

    interface PureTransformer extends DensityFunction {
        DensityFunction input();

        @Override
        default double compute(DensityFunction.FunctionContext param0) {
            return this.transform(this.input().compute(param0));
        }

        @Override
        default void fillArray(double[] param0, DensityFunction.ContextProvider param1) {
            this.input().fillArray(param0, param1);

            for(int var0 = 0; var0 < param0.length; ++var0) {
                param0[var0] = this.transform(param0[var0]);
            }

        }

        double transform(double var1);
    }

    static record RangeChoice(DensityFunction input, double minInclusive, double maxExclusive, DensityFunction whenInRange, DensityFunction whenOutOfRange)
        implements DensityFunction {
        public static final MapCodec<DensityFunctions.RangeChoice> DATA_CODEC = RecordCodecBuilder.mapCodec(
            param0 -> param0.group(
                        DensityFunction.HOLDER_HELPER_CODEC.fieldOf("input").forGetter(DensityFunctions.RangeChoice::input),
                        DensityFunctions.NOISE_VALUE_CODEC.fieldOf("min_inclusive").forGetter(DensityFunctions.RangeChoice::minInclusive),
                        DensityFunctions.NOISE_VALUE_CODEC.fieldOf("max_exclusive").forGetter(DensityFunctions.RangeChoice::maxExclusive),
                        DensityFunction.HOLDER_HELPER_CODEC.fieldOf("when_in_range").forGetter(DensityFunctions.RangeChoice::whenInRange),
                        DensityFunction.HOLDER_HELPER_CODEC.fieldOf("when_out_of_range").forGetter(DensityFunctions.RangeChoice::whenOutOfRange)
                    )
                    .apply(param0, DensityFunctions.RangeChoice::new)
        );
        public static final Codec<DensityFunctions.RangeChoice> CODEC = DensityFunctions.makeCodec(DATA_CODEC);

        @Override
        public double compute(DensityFunction.FunctionContext param0) {
            double var0 = this.input.compute(param0);
            return var0 >= this.minInclusive && var0 < this.maxExclusive ? this.whenInRange.compute(param0) : this.whenOutOfRange.compute(param0);
        }

        @Override
        public void fillArray(double[] param0, DensityFunction.ContextProvider param1) {
            this.input.fillArray(param0, param1);

            for(int var0 = 0; var0 < param0.length; ++var0) {
                double var1 = param0[var0];
                if (var1 >= this.minInclusive && var1 < this.maxExclusive) {
                    param0[var0] = this.whenInRange.compute(param1.forIndex(var0));
                } else {
                    param0[var0] = this.whenOutOfRange.compute(param1.forIndex(var0));
                }
            }

        }

        @Override
        public DensityFunction mapAll(DensityFunction.Visitor param0) {
            return param0.apply(
                new DensityFunctions.RangeChoice(
                    this.input.mapAll(param0), this.minInclusive, this.maxExclusive, this.whenInRange.mapAll(param0), this.whenOutOfRange.mapAll(param0)
                )
            );
        }

        @Override
        public double minValue() {
            return Math.min(this.whenInRange.minValue(), this.whenOutOfRange.minValue());
        }

        @Override
        public double maxValue() {
            return Math.max(this.whenInRange.maxValue(), this.whenOutOfRange.maxValue());
        }

        @Override
        public Codec<? extends DensityFunction> codec() {
            return CODEC;
        }
    }

    static record Shift(Holder<NormalNoise.NoiseParameters> noiseData, @Nullable NormalNoise offsetNoise) implements DensityFunctions.ShiftNoise {
        static final Codec<DensityFunctions.Shift> CODEC = DensityFunctions.singleArgumentCodec(
            NormalNoise.NoiseParameters.CODEC, param0 -> new DensityFunctions.Shift(param0, null), DensityFunctions.Shift::noiseData
        );

        @Override
        public double compute(DensityFunction.FunctionContext param0) {
            return this.compute((double)param0.blockX(), (double)param0.blockY(), (double)param0.blockZ());
        }

        @Override
        public DensityFunctions.ShiftNoise withNewNoise(NormalNoise param0) {
            return new DensityFunctions.Shift(this.noiseData, param0);
        }

        @Override
        public Codec<? extends DensityFunction> codec() {
            return CODEC;
        }
    }

    protected static record ShiftA(Holder<NormalNoise.NoiseParameters> noiseData, @Nullable NormalNoise offsetNoise) implements DensityFunctions.ShiftNoise {
        static final Codec<DensityFunctions.ShiftA> CODEC = DensityFunctions.singleArgumentCodec(
            NormalNoise.NoiseParameters.CODEC, param0 -> new DensityFunctions.ShiftA(param0, null), DensityFunctions.ShiftA::noiseData
        );

        @Override
        public double compute(DensityFunction.FunctionContext param0) {
            return this.compute((double)param0.blockX(), 0.0, (double)param0.blockZ());
        }

        @Override
        public DensityFunctions.ShiftNoise withNewNoise(NormalNoise param0) {
            return new DensityFunctions.ShiftA(this.noiseData, param0);
        }

        @Override
        public Codec<? extends DensityFunction> codec() {
            return CODEC;
        }
    }

    protected static record ShiftB(Holder<NormalNoise.NoiseParameters> noiseData, @Nullable NormalNoise offsetNoise) implements DensityFunctions.ShiftNoise {
        static final Codec<DensityFunctions.ShiftB> CODEC = DensityFunctions.singleArgumentCodec(
            NormalNoise.NoiseParameters.CODEC, param0 -> new DensityFunctions.ShiftB(param0, null), DensityFunctions.ShiftB::noiseData
        );

        @Override
        public double compute(DensityFunction.FunctionContext param0) {
            return this.compute((double)param0.blockZ(), (double)param0.blockX(), 0.0);
        }

        @Override
        public DensityFunctions.ShiftNoise withNewNoise(NormalNoise param0) {
            return new DensityFunctions.ShiftB(this.noiseData, param0);
        }

        @Override
        public Codec<? extends DensityFunction> codec() {
            return CODEC;
        }
    }

    interface ShiftNoise extends DensityFunction.SimpleFunction {
        Holder<NormalNoise.NoiseParameters> noiseData();

        @Nullable
        NormalNoise offsetNoise();

        @Override
        default double minValue() {
            return -this.maxValue();
        }

        @Override
        default double maxValue() {
            NormalNoise var0 = this.offsetNoise();
            return (var0 == null ? 2.0 : var0.maxValue()) * 4.0;
        }

        default double compute(double param0, double param1, double param2) {
            NormalNoise var0 = this.offsetNoise();
            return var0 == null ? 0.0 : var0.getValue(param0 * 0.25, param1 * 0.25, param2 * 0.25) * 4.0;
        }

        DensityFunctions.ShiftNoise withNewNoise(NormalNoise var1);
    }

    protected static record ShiftedNoise(
        DensityFunction shiftX,
        DensityFunction shiftY,
        DensityFunction shiftZ,
        double xzScale,
        double yScale,
        Holder<NormalNoise.NoiseParameters> noiseData,
        @Nullable NormalNoise noise
    ) implements DensityFunction {
        private static final MapCodec<DensityFunctions.ShiftedNoise> DATA_CODEC = RecordCodecBuilder.mapCodec(
            param0 -> param0.group(
                        DensityFunction.HOLDER_HELPER_CODEC.fieldOf("shift_x").forGetter(DensityFunctions.ShiftedNoise::shiftX),
                        DensityFunction.HOLDER_HELPER_CODEC.fieldOf("shift_y").forGetter(DensityFunctions.ShiftedNoise::shiftY),
                        DensityFunction.HOLDER_HELPER_CODEC.fieldOf("shift_z").forGetter(DensityFunctions.ShiftedNoise::shiftZ),
                        Codec.DOUBLE.fieldOf("xz_scale").forGetter(DensityFunctions.ShiftedNoise::xzScale),
                        Codec.DOUBLE.fieldOf("y_scale").forGetter(DensityFunctions.ShiftedNoise::yScale),
                        NormalNoise.NoiseParameters.CODEC.fieldOf("noise").forGetter(DensityFunctions.ShiftedNoise::noiseData)
                    )
                    .apply(param0, DensityFunctions.ShiftedNoise::createUnseeded)
        );
        public static final Codec<DensityFunctions.ShiftedNoise> CODEC = DensityFunctions.makeCodec(DATA_CODEC);

        public static DensityFunctions.ShiftedNoise createUnseeded(
            DensityFunction param0, DensityFunction param1, DensityFunction param2, double param3, double param4, Holder<NormalNoise.NoiseParameters> param5
        ) {
            return new DensityFunctions.ShiftedNoise(param0, param1, param2, param3, param4, param5, null);
        }

        @Override
        public double compute(DensityFunction.FunctionContext param0) {
            if (this.noise == null) {
                return 0.0;
            } else {
                double var0 = (double)param0.blockX() * this.xzScale + this.shiftX.compute(param0);
                double var1 = (double)param0.blockY() * this.yScale + this.shiftY.compute(param0);
                double var2 = (double)param0.blockZ() * this.xzScale + this.shiftZ.compute(param0);
                return this.noise.getValue(var0, var1, var2);
            }
        }

        @Override
        public void fillArray(double[] param0, DensityFunction.ContextProvider param1) {
            param1.fillAllDirectly(param0, this);
        }

        @Override
        public DensityFunction mapAll(DensityFunction.Visitor param0) {
            return param0.apply(
                new DensityFunctions.ShiftedNoise(
                    this.shiftX.mapAll(param0), this.shiftY.mapAll(param0), this.shiftZ.mapAll(param0), this.xzScale, this.yScale, this.noiseData, this.noise
                )
            );
        }

        @Override
        public double minValue() {
            return -this.maxValue();
        }

        @Override
        public double maxValue() {
            return this.noise == null ? 2.0 : this.noise.maxValue();
        }

        @Override
        public Codec<? extends DensityFunction> codec() {
            return CODEC;
        }
    }

    protected static record Slide(@Nullable NoiseSettings settings, DensityFunction input) implements DensityFunctions.TransformerWithContext {
        public static final Codec<DensityFunctions.Slide> CODEC = DensityFunctions.singleFunctionArgumentCodec(
            param0 -> new DensityFunctions.Slide(null, param0), DensityFunctions.Slide::input
        );

        @Override
        public double transform(DensityFunction.FunctionContext param0, double param1) {
            return this.settings == null ? param1 : NoiseRouterData.applySlide(this.settings, param1, (double)param0.blockY());
        }

        @Override
        public DensityFunction mapAll(DensityFunction.Visitor param0) {
            return param0.apply(new DensityFunctions.Slide(this.settings, this.input.mapAll(param0)));
        }

        @Override
        public double minValue() {
            return this.settings == null
                ? this.input.minValue()
                : Math.min(this.input.minValue(), Math.min(this.settings.bottomSlideSettings().target(), this.settings.topSlideSettings().target()));
        }

        @Override
        public double maxValue() {
            return this.settings == null
                ? this.input.maxValue()
                : Math.max(this.input.maxValue(), Math.max(this.settings.bottomSlideSettings().target(), this.settings.topSlideSettings().target()));
        }

        @Override
        public Codec<? extends DensityFunction> codec() {
            return CODEC;
        }
    }

    public static record Spline(CubicSpline<DensityFunctions.Spline.Point, DensityFunctions.Spline.Coordinate> spline) implements DensityFunction {
        private static final Codec<CubicSpline<DensityFunctions.Spline.Point, DensityFunctions.Spline.Coordinate>> SPLINE_CODEC = CubicSpline.codec(
            DensityFunctions.Spline.Coordinate.CODEC
        );
        private static final MapCodec<DensityFunctions.Spline> DATA_CODEC = SPLINE_CODEC.fieldOf("spline")
            .xmap(DensityFunctions.Spline::new, DensityFunctions.Spline::spline);
        public static final Codec<DensityFunctions.Spline> CODEC = DensityFunctions.makeCodec(DATA_CODEC);

        @Override
        public double compute(DensityFunction.FunctionContext param0) {
            return (double)this.spline.apply(new DensityFunctions.Spline.Point(param0));
        }

        @Override
        public double minValue() {
            return (double)this.spline.minValue();
        }

        @Override
        public double maxValue() {
            return (double)this.spline.maxValue();
        }

        @Override
        public void fillArray(double[] param0, DensityFunction.ContextProvider param1) {
            param1.fillAllDirectly(param0, this);
        }

        @Override
        public DensityFunction mapAll(DensityFunction.Visitor param0) {
            return param0.apply(new DensityFunctions.Spline(this.spline.mapAll(param1 -> param1.mapAll(param0))));
        }

        @Override
        public Codec<? extends DensityFunction> codec() {
            return CODEC;
        }

        public static record Coordinate(Holder<DensityFunction> function) implements ToFloatFunction<DensityFunctions.Spline.Point> {
            public static final Codec<DensityFunctions.Spline.Coordinate> CODEC = DensityFunction.CODEC
                .xmap(DensityFunctions.Spline.Coordinate::new, DensityFunctions.Spline.Coordinate::function);

            @Override
            public String toString() {
                Optional<ResourceKey<DensityFunction>> var0 = this.function.unwrapKey();
                if (var0.isPresent()) {
                    ResourceKey<DensityFunction> var1 = var0.get();
                    if (var1 == NoiseRouterData.CONTINENTS) {
                        return "continents";
                    }

                    if (var1 == NoiseRouterData.EROSION) {
                        return "erosion";
                    }

                    if (var1 == NoiseRouterData.RIDGES) {
                        return "weirdness";
                    }

                    if (var1 == NoiseRouterData.RIDGES_FOLDED) {
                        return "ridges";
                    }
                }

                return "Coordinate[" + this.function + "]";
            }

            public float apply(DensityFunctions.Spline.Point param0) {
                return (float)this.function.value().compute(param0.context());
            }

            @Override
            public float minValue() {
                return (float)this.function.value().minValue();
            }

            @Override
            public float maxValue() {
                return (float)this.function.value().maxValue();
            }

            public DensityFunctions.Spline.Coordinate mapAll(DensityFunction.Visitor param0) {
                return new DensityFunctions.Spline.Coordinate(new Holder.Direct<>(this.function.value().mapAll(param0)));
            }
        }

        public static record Point(DensityFunction.FunctionContext context) {
        }
    }

    interface TransformerWithContext extends DensityFunction {
        DensityFunction input();

        @Override
        default double compute(DensityFunction.FunctionContext param0) {
            return this.transform(param0, this.input().compute(param0));
        }

        @Override
        default void fillArray(double[] param0, DensityFunction.ContextProvider param1) {
            this.input().fillArray(param0, param1);

            for(int var0 = 0; var0 < param0.length; ++var0) {
                param0[var0] = this.transform(param1.forIndex(var0), param0[var0]);
            }

        }

        double transform(DensityFunction.FunctionContext var1, double var2);
    }

    interface TwoArgumentSimpleFunction extends DensityFunction {
        Logger LOGGER = LogUtils.getLogger();

        static DensityFunctions.TwoArgumentSimpleFunction create(
            DensityFunctions.TwoArgumentSimpleFunction.Type param0, DensityFunction param1, DensityFunction param2
        ) {
            double var0 = param1.minValue();
            double var1 = param2.minValue();
            double var2 = param1.maxValue();
            double var3 = param2.maxValue();
            if (param0 == DensityFunctions.TwoArgumentSimpleFunction.Type.MIN || param0 == DensityFunctions.TwoArgumentSimpleFunction.Type.MAX) {
                boolean var4 = var0 >= var3;
                boolean var5 = var1 >= var2;
                if (var4 || var5) {
                    LOGGER.warn("Creating a " + param0 + " function between two non-overlapping inputs: " + param1 + " and " + param2);
                }
            }

            double var6 = switch(param0) {
                case ADD -> var0 + var1;
                case MAX -> Math.max(var0, var1);
                case MIN -> Math.min(var0, var1);
                case MUL -> var0 > 0.0 && var1 > 0.0 ? var0 * var1 : (var2 < 0.0 && var3 < 0.0 ? var2 * var3 : Math.min(var0 * var3, var2 * var1));
            };

            double var7 = switch(param0) {
                case ADD -> var2 + var3;
                case MAX -> Math.max(var2, var3);
                case MIN -> Math.min(var2, var3);
                case MUL -> var0 > 0.0 && var1 > 0.0 ? var2 * var3 : (var2 < 0.0 && var3 < 0.0 ? var0 * var1 : Math.max(var0 * var1, var2 * var3));
            };
            if (param0 == DensityFunctions.TwoArgumentSimpleFunction.Type.MUL || param0 == DensityFunctions.TwoArgumentSimpleFunction.Type.ADD) {
                if (param1 instanceof DensityFunctions.Constant var8) {
                    return new DensityFunctions.MulOrAdd(
                        param0 == DensityFunctions.TwoArgumentSimpleFunction.Type.ADD ? DensityFunctions.MulOrAdd.Type.ADD : DensityFunctions.MulOrAdd.Type.MUL,
                        param2,
                        var6,
                        var7,
                        var8.value
                    );
                }

                if (param2 instanceof DensityFunctions.Constant var9) {
                    return new DensityFunctions.MulOrAdd(
                        param0 == DensityFunctions.TwoArgumentSimpleFunction.Type.ADD ? DensityFunctions.MulOrAdd.Type.ADD : DensityFunctions.MulOrAdd.Type.MUL,
                        param1,
                        var6,
                        var7,
                        var9.value
                    );
                }
            }

            return new DensityFunctions.Ap2(param0, param1, param2, var6, var7);
        }

        DensityFunctions.TwoArgumentSimpleFunction.Type type();

        DensityFunction argument1();

        DensityFunction argument2();

        @Override
        default Codec<? extends DensityFunction> codec() {
            return this.type().codec;
        }

        public static enum Type implements StringRepresentable {
            ADD("add"),
            MUL("mul"),
            MIN("min"),
            MAX("max");

            final Codec<DensityFunctions.TwoArgumentSimpleFunction> codec = DensityFunctions.doubleFunctionArgumentCodec(
                (param0x, param1) -> DensityFunctions.TwoArgumentSimpleFunction.create(this, param0x, param1),
                DensityFunctions.TwoArgumentSimpleFunction::argument1,
                DensityFunctions.TwoArgumentSimpleFunction::argument2
            );
            private final String name;

            private Type(String param0) {
                this.name = param0;
            }

            @Override
            public String getSerializedName() {
                return this.name;
            }
        }
    }

    protected static record WeirdScaledSampler(
        DensityFunction input,
        Holder<NormalNoise.NoiseParameters> noiseData,
        @Nullable NormalNoise noise,
        DensityFunctions.WeirdScaledSampler.RarityValueMapper rarityValueMapper
    ) implements DensityFunctions.TransformerWithContext {
        private static final MapCodec<DensityFunctions.WeirdScaledSampler> DATA_CODEC = RecordCodecBuilder.mapCodec(
            param0 -> param0.group(
                        DensityFunction.HOLDER_HELPER_CODEC.fieldOf("input").forGetter(DensityFunctions.WeirdScaledSampler::input),
                        NormalNoise.NoiseParameters.CODEC.fieldOf("noise").forGetter(DensityFunctions.WeirdScaledSampler::noiseData),
                        DensityFunctions.WeirdScaledSampler.RarityValueMapper.CODEC
                            .fieldOf("rarity_value_mapper")
                            .forGetter(DensityFunctions.WeirdScaledSampler::rarityValueMapper)
                    )
                    .apply(param0, DensityFunctions.WeirdScaledSampler::createUnseeded)
        );
        public static final Codec<DensityFunctions.WeirdScaledSampler> CODEC = DensityFunctions.makeCodec(DATA_CODEC);

        public static DensityFunctions.WeirdScaledSampler createUnseeded(
            DensityFunction param0, Holder<NormalNoise.NoiseParameters> param1, DensityFunctions.WeirdScaledSampler.RarityValueMapper param2
        ) {
            return new DensityFunctions.WeirdScaledSampler(param0, param1, null, param2);
        }

        @Override
        public double transform(DensityFunction.FunctionContext param0, double param1) {
            if (this.noise == null) {
                return 0.0;
            } else {
                double var0 = this.rarityValueMapper.mapper.get(param1);
                return var0 * Math.abs(this.noise.getValue((double)param0.blockX() / var0, (double)param0.blockY() / var0, (double)param0.blockZ() / var0));
            }
        }

        @Override
        public DensityFunction mapAll(DensityFunction.Visitor param0) {
            this.input.mapAll(param0);
            return param0.apply(new DensityFunctions.WeirdScaledSampler(this.input.mapAll(param0), this.noiseData, this.noise, this.rarityValueMapper));
        }

        @Override
        public double minValue() {
            return 0.0;
        }

        @Override
        public double maxValue() {
            return this.rarityValueMapper.maxRarity * (this.noise == null ? 2.0 : this.noise.maxValue());
        }

        @Override
        public Codec<? extends DensityFunction> codec() {
            return CODEC;
        }

        public static enum RarityValueMapper implements StringRepresentable {
            TYPE1("type_1", NoiseRouterData.QuantizedSpaghettiRarity::getSpaghettiRarity3D, 2.0),
            TYPE2("type_2", NoiseRouterData.QuantizedSpaghettiRarity::getSphaghettiRarity2D, 3.0);

            private static final Map<String, DensityFunctions.WeirdScaledSampler.RarityValueMapper> BY_NAME = Arrays.stream(values())
                .collect(Collectors.toMap(DensityFunctions.WeirdScaledSampler.RarityValueMapper::getSerializedName, param0 -> param0));
            public static final Codec<DensityFunctions.WeirdScaledSampler.RarityValueMapper> CODEC = StringRepresentable.fromEnum(
                DensityFunctions.WeirdScaledSampler.RarityValueMapper::values, BY_NAME::get
            );
            private final String name;
            final Double2DoubleFunction mapper;
            final double maxRarity;

            private RarityValueMapper(String param0, Double2DoubleFunction param1, double param2) {
                this.name = param0;
                this.mapper = param1;
                this.maxRarity = param2;
            }

            @Override
            public String getSerializedName() {
                return this.name;
            }
        }
    }

    static record YClampedGradient(int fromY, int toY, double fromValue, double toValue) implements DensityFunction.SimpleFunction {
        private static final MapCodec<DensityFunctions.YClampedGradient> DATA_CODEC = RecordCodecBuilder.mapCodec(
            param0 -> param0.group(
                        Codec.intRange(DimensionType.MIN_Y * 2, DimensionType.MAX_Y * 2).fieldOf("from_y").forGetter(DensityFunctions.YClampedGradient::fromY),
                        Codec.intRange(DimensionType.MIN_Y * 2, DimensionType.MAX_Y * 2).fieldOf("to_y").forGetter(DensityFunctions.YClampedGradient::toY),
                        DensityFunctions.NOISE_VALUE_CODEC.fieldOf("from_value").forGetter(DensityFunctions.YClampedGradient::fromValue),
                        DensityFunctions.NOISE_VALUE_CODEC.fieldOf("to_value").forGetter(DensityFunctions.YClampedGradient::toValue)
                    )
                    .apply(param0, DensityFunctions.YClampedGradient::new)
        );
        public static final Codec<DensityFunctions.YClampedGradient> CODEC = DensityFunctions.makeCodec(DATA_CODEC);

        @Override
        public double compute(DensityFunction.FunctionContext param0) {
            return Mth.clampedMap((double)param0.blockY(), (double)this.fromY, (double)this.toY, this.fromValue, this.toValue);
        }

        @Override
        public double minValue() {
            return Math.min(this.fromValue, this.toValue);
        }

        @Override
        public double maxValue() {
            return Math.max(this.fromValue, this.toValue);
        }

        @Override
        public Codec<? extends DensityFunction> codec() {
            return CODEC;
        }
    }
}
