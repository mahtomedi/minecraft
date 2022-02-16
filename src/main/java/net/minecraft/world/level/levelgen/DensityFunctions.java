package net.minecraft.world.level.levelgen;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import java.util.Arrays;
import net.minecraft.util.Mth;
import net.minecraft.util.ToFloatFunction;
import net.minecraft.world.level.biome.TerrainShaper;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import org.slf4j.Logger;

public final class DensityFunctions {
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

    public static DensityFunction mappedNoise(NormalNoise param0, @Deprecated double param1, double param2, double param3, double param4) {
        return mapFromUnitTo(new DensityFunctions.Noise(param0, param1, param2), param3, param4);
    }

    public static DensityFunction mappedNoise(NormalNoise param0, double param1, double param2, double param3) {
        return mapFromUnitTo(new DensityFunctions.Noise(param0, 1.0, param1), param2, param3);
    }

    public static DensityFunction mappedNoise(NormalNoise param0, double param1, double param2) {
        return mapFromUnitTo(new DensityFunctions.Noise(param0, 1.0, 1.0), param1, param2);
    }

    public static DensityFunction shiftedNoise2d(DensityFunction param0, DensityFunction param1, double param2, NormalNoise param3) {
        return new DensityFunctions.ShiftedNoise(param0, zero(), param1, param2, 0.0, param3);
    }

    public static DensityFunction noise(NormalNoise param0) {
        return new DensityFunctions.Noise(param0);
    }

    public static DensityFunction noise(NormalNoise param0, double param1, double param2) {
        return new DensityFunctions.Noise(param0, param1, param2);
    }

    public static DensityFunction noise(NormalNoise param0, double param1) {
        return new DensityFunctions.Noise(param0, param1);
    }

    public static DensityFunction rangeChoice(DensityFunction param0, double param1, double param2, DensityFunction param3, DensityFunction param4) {
        return new DensityFunctions.RangeChoice(param0, param1, param2, param3, param4);
    }

    public static DensityFunction shiftA(NormalNoise param0) {
        return new DensityFunctions.ShiftA(param0);
    }

    public static DensityFunction shiftB(NormalNoise param0) {
        return new DensityFunctions.ShiftB(param0);
    }

    public static DensityFunction shift0(NormalNoise param0) {
        return new DensityFunctions.Shift0(param0);
    }

    public static DensityFunction shift1(NormalNoise param0) {
        return new DensityFunctions.Shift1(param0);
    }

    public static DensityFunction shift2(NormalNoise param0) {
        return new DensityFunctions.Shift2(param0);
    }

    public static DensityFunction blendDensity(DensityFunction param0) {
        return new DensityFunctions.BlendDensity(param0);
    }

    public static DensityFunction endIslands(long param0) {
        return new DensityFunctions.EndIslandDensityFunction(param0);
    }

    public static DensityFunction weirdScaledSampler(DensityFunction param0, NormalNoise param1, Double2DoubleFunction param2, double param3) {
        return new DensityFunctions.WeirdScaledSampler(param0, param1, param2, param3);
    }

    public static DensityFunction slide(NoiseSettings param0, DensityFunction param1) {
        return new DensityFunctions.Slide(param0, param1);
    }

    public static DensityFunction add(DensityFunction param0, DensityFunction param1) {
        return DensityFunctions.Ap2.create(DensityFunctions.Ap2.Type.ADD, param0, param1);
    }

    public static DensityFunction mul(DensityFunction param0, DensityFunction param1) {
        return DensityFunctions.Ap2.create(DensityFunctions.Ap2.Type.MUL, param0, param1);
    }

    public static DensityFunction min(DensityFunction param0, DensityFunction param1) {
        return DensityFunctions.Ap2.create(DensityFunctions.Ap2.Type.MIN, param0, param1);
    }

    public static DensityFunction max(DensityFunction param0, DensityFunction param1) {
        return DensityFunctions.Ap2.create(DensityFunctions.Ap2.Type.MAX, param0, param1);
    }

    public static DensityFunction terrainShaperSpline(
        DensityFunction param0, DensityFunction param1, DensityFunction param2, ToFloatFunction<TerrainShaper.Point> param3, double param4, double param5
    ) {
        return new DensityFunctions.TerrainShaperSpline(param0, param1, param2, param3, param4, param5);
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

    protected static DensityFunction map(DensityFunction param0, DensityFunctions.Mapped.Type param1) {
        return new DensityFunctions.Mapped(param1, param0, 0.0, 0.0).mapAll(param0x -> param0x);
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

    static record Ap2(DensityFunctions.Ap2.Type type, DensityFunction f1, DensityFunction f2, double minValue, double maxValue) implements DensityFunction {
        private static final Logger LOGGER = LogUtils.getLogger();

        public static DensityFunction create(DensityFunctions.Ap2.Type param0, DensityFunction param1, DensityFunction param2) {
            double var0 = param1.minValue();
            double var1 = param2.minValue();
            double var2 = param1.maxValue();
            double var3 = param2.maxValue();
            if (param0 == DensityFunctions.Ap2.Type.MIN || param0 == DensityFunctions.Ap2.Type.MAX) {
                boolean var4 = var0 >= var3;
                boolean var5 = var1 >= var2;
                if (var4 || var5) {
                    LOGGER.warn("Creating a " + param0 + " function between two non-overlapping inputs: " + param1 + " and " + param2);
                    if (param0 == DensityFunctions.Ap2.Type.MIN) {
                        return var5 ? param1 : param2;
                    } else {
                        return var5 ? param2 : param1;
                    }
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
            if (param0 == DensityFunctions.Ap2.Type.MUL || param0 == DensityFunctions.Ap2.Type.ADD) {
                if (param1 instanceof DensityFunctions.Constant var8) {
                    return new DensityFunctions.MulOrAdd(
                        param0 == DensityFunctions.Ap2.Type.ADD ? DensityFunctions.MulOrAdd.Type.ADD : DensityFunctions.MulOrAdd.Type.MUL,
                        param2,
                        var6,
                        var7,
                        var8.value
                    );
                }

                if (param2 instanceof DensityFunctions.Constant var9) {
                    return new DensityFunctions.MulOrAdd(
                        param0 == DensityFunctions.Ap2.Type.ADD ? DensityFunctions.MulOrAdd.Type.ADD : DensityFunctions.MulOrAdd.Type.MUL,
                        param1,
                        var6,
                        var7,
                        var9.value
                    );
                }
            }

            return new DensityFunctions.Ap2(param0, param1, param2, var6, var7);
        }

        @Override
        public double compute(DensityFunction.FunctionContext param0) {
            double var0 = this.f1.compute(param0);

            return switch(this.type) {
                case ADD -> var0 + this.f2.compute(param0);
                case MAX -> var0 > this.f2.maxValue() ? var0 : Math.max(var0, this.f2.compute(param0));
                case MIN -> var0 < this.f2.minValue() ? var0 : Math.min(var0, this.f2.compute(param0));
                case MUL -> var0 == 0.0 ? 0.0 : var0 * this.f2.compute(param0);
            };
        }

        @Override
        public void fillArray(double[] param0, DensityFunction.ContextProvider param1) {
            this.f1.fillArray(param0, param1);
            switch(this.type) {
                case ADD:
                    double[] var0 = new double[param0.length];
                    this.f2.fillArray(var0, param1);

                    for(int var1 = 0; var1 < param0.length; ++var1) {
                        param0[var1] += var0[var1];
                    }
                    break;
                case MAX:
                    double var7 = this.f2.maxValue();

                    for(int var8 = 0; var8 < param0.length; ++var8) {
                        double var9 = param0[var8];
                        param0[var8] = var9 > var7 ? var9 : Math.max(var9, this.f2.compute(param1.forIndex(var8)));
                    }
                    break;
                case MIN:
                    double var4 = this.f2.minValue();

                    for(int var5 = 0; var5 < param0.length; ++var5) {
                        double var6 = param0[var5];
                        param0[var5] = var6 < var4 ? var6 : Math.min(var6, this.f2.compute(param1.forIndex(var5)));
                    }
                    break;
                case MUL:
                    for(int var2 = 0; var2 < param0.length; ++var2) {
                        double var3 = param0[var2];
                        param0[var2] = var3 == 0.0 ? 0.0 : var3 * this.f2.compute(param1.forIndex(var2));
                    }
            }

        }

        @Override
        public DensityFunction mapAll(DensityFunction.Visitor param0) {
            return param0.apply(create(this.type, this.f1.mapAll(param0), this.f2.mapAll(param0)));
        }

        static enum Type {
            ADD,
            MUL,
            MIN,
            MAX;
        }
    }

    protected static enum BlendAlpha implements DensityFunction.SimpleFunction {
        INSTANCE;

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
    }

    static record BlendDensity(DensityFunction input) implements DensityFunctions.TransformerWithContext {
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
    }

    protected static enum BlendOffset implements DensityFunction.SimpleFunction {
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

    protected static record Clamp(DensityFunction input, double minValue, double maxValue) implements DensityFunctions.PureTransformer {
        @Override
        public double transform(double param0) {
            return Mth.clamp(param0, this.minValue, this.maxValue);
        }

        @Override
        public DensityFunction mapAll(DensityFunction.Visitor param0) {
            return new DensityFunctions.Clamp(this.input.mapAll(param0), this.minValue, this.maxValue);
        }
    }

    static record Constant(double value) implements DensityFunction.SimpleFunction {
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
    }

    static final class EndIslandDensityFunction implements DensityFunction.SimpleFunction {
        final SimplexNoise islandNoise;

        public EndIslandDensityFunction(long param0) {
            RandomSource var0 = new LegacyRandomSource(param0);
            var0.consumeCount(17292);
            this.islandNoise = new SimplexNoise(var0);
        }

        @Override
        public double compute(DensityFunction.FunctionContext param0) {
            return ((double)TheEndBiomeSource.getHeightValue(this.islandNoise, param0.blockX() / 8, param0.blockZ() / 8) - 8.0) / 128.0;
        }

        @Override
        public double minValue() {
            return -0.84375;
        }

        @Override
        public double maxValue() {
            return 0.5625;
        }
    }

    protected static record Mapped(DensityFunctions.Mapped.Type type, DensityFunction input, double minValue, double maxValue)
        implements DensityFunctions.PureTransformer {
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

        @Override
        public DensityFunction mapAll(DensityFunction.Visitor param0) {
            DensityFunction var0 = this.input.mapAll(param0);
            double var1 = var0.minValue();
            double var2 = transform(this.type, var1);
            double var3 = transform(this.type, var0.maxValue());
            return this.type != DensityFunctions.Mapped.Type.ABS && this.type != DensityFunctions.Mapped.Type.SQUARE
                ? new DensityFunctions.Mapped(this.type, var0, var2, var3)
                : new DensityFunctions.Mapped(this.type, var0, Math.max(0.0, var1), Math.max(var2, var3));
        }

        static enum Type {
            ABS,
            SQUARE,
            CUBE,
            HALF_NEGATIVE,
            QUARTER_NEGATIVE,
            SQUEEZE;
        }
    }

    protected static record Marker(DensityFunctions.Marker.Type type, DensityFunction function) implements DensityFunction {
        @Override
        public double compute(DensityFunction.FunctionContext param0) {
            return this.function.compute(param0);
        }

        @Override
        public void fillArray(double[] param0, DensityFunction.ContextProvider param1) {
            this.function.fillArray(param0, param1);
        }

        @Override
        public DensityFunction mapAll(DensityFunction.Visitor param0) {
            return param0.apply(new DensityFunctions.Marker(this.type, this.function.mapAll(param0)));
        }

        @Override
        public double minValue() {
            return this.function.minValue();
        }

        @Override
        public double maxValue() {
            return this.function.maxValue();
        }

        static enum Type {
            Interpolated,
            FlatCache,
            Cache2D,
            CacheOnce,
            CacheAllInCell;
        }
    }

    static record MulOrAdd(DensityFunctions.MulOrAdd.Type type, DensityFunction input, double minValue, double maxValue, double argument)
        implements DensityFunctions.PureTransformer {
        @Override
        public double transform(double param0) {
            return switch(this.type) {
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
            if (this.type == DensityFunctions.MulOrAdd.Type.ADD) {
                var3 = var1 + this.argument;
                var4 = var2 + this.argument;
            } else if (this.argument >= 0.0) {
                var3 = var1 * this.argument;
                var4 = var2 * this.argument;
            } else {
                var3 = var2 * this.argument;
                var4 = var1 * this.argument;
            }

            return new DensityFunctions.MulOrAdd(this.type, var0, var3, var4, this.argument);
        }

        static enum Type {
            MUL,
            ADD;
        }
    }

    static record Noise(NormalNoise noise, @Deprecated double xzScale, double yScale) implements DensityFunction.SimpleFunction {
        public Noise(NormalNoise param0, double param1) {
            this(param0, 1.0, param1);
        }

        public Noise(NormalNoise param0) {
            this(param0, 1.0, 1.0);
        }

        @Override
        public double compute(DensityFunction.FunctionContext param0) {
            return this.noise.getValue((double)param0.blockX() * this.xzScale, (double)param0.blockY() * this.yScale, (double)param0.blockZ() * this.xzScale);
        }

        @Override
        public double minValue() {
            return -this.maxValue();
        }

        @Override
        public double maxValue() {
            return this.noise.maxValue();
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
    }

    static record Shift0(NormalNoise offsetNoise) implements DensityFunctions.ShiftNoise {
        @Override
        public double compute(DensityFunction.FunctionContext param0) {
            return this.compute((double)param0.blockX(), (double)param0.blockY(), (double)param0.blockZ());
        }
    }

    static record Shift1(NormalNoise offsetNoise) implements DensityFunctions.ShiftNoise {
        @Override
        public double compute(DensityFunction.FunctionContext param0) {
            return this.compute((double)param0.blockY(), (double)param0.blockZ(), (double)param0.blockX());
        }
    }

    static record Shift2(NormalNoise offsetNoise) implements DensityFunctions.ShiftNoise {
        @Override
        public double compute(DensityFunction.FunctionContext param0) {
            return this.compute((double)param0.blockZ(), (double)param0.blockX(), (double)param0.blockY());
        }
    }

    static record ShiftA(NormalNoise offsetNoise) implements DensityFunctions.ShiftNoise {
        @Override
        public double compute(DensityFunction.FunctionContext param0) {
            return this.compute((double)param0.blockX(), 0.0, (double)param0.blockZ());
        }
    }

    static record ShiftB(NormalNoise offsetNoise) implements DensityFunctions.ShiftNoise {
        @Override
        public double compute(DensityFunction.FunctionContext param0) {
            return this.compute((double)param0.blockZ(), (double)param0.blockX(), 0.0);
        }
    }

    interface ShiftNoise extends DensityFunction.SimpleFunction {
        NormalNoise offsetNoise();

        @Override
        default double minValue() {
            return -this.maxValue();
        }

        @Override
        default double maxValue() {
            return this.offsetNoise().maxValue() * 4.0;
        }

        default double compute(double param0, double param1, double param2) {
            return this.offsetNoise().getValue(param0 * 0.25, param1 * 0.25, param2 * 0.25) * 4.0;
        }
    }

    static record ShiftedNoise(DensityFunction shiftX, DensityFunction shiftY, DensityFunction shiftZ, double xzScale, double yScale, NormalNoise noise)
        implements DensityFunction {
        @Override
        public double compute(DensityFunction.FunctionContext param0) {
            double var0 = (double)param0.blockX() * this.xzScale + this.shiftX.compute(param0);
            double var1 = (double)param0.blockY() * this.yScale + this.shiftY.compute(param0);
            double var2 = (double)param0.blockZ() * this.xzScale + this.shiftZ.compute(param0);
            return this.noise.getValue(var0, var1, var2);
        }

        @Override
        public void fillArray(double[] param0, DensityFunction.ContextProvider param1) {
            param1.fillAllDirectly(param0, this);
        }

        @Override
        public DensityFunction mapAll(DensityFunction.Visitor param0) {
            return param0.apply(
                new DensityFunctions.ShiftedNoise(
                    this.shiftX.mapAll(param0), this.shiftY.mapAll(param0), this.shiftZ.mapAll(param0), this.xzScale, this.yScale, this.noise
                )
            );
        }

        @Override
        public double minValue() {
            return -this.maxValue();
        }

        @Override
        public double maxValue() {
            return this.noise.maxValue();
        }
    }

    static record Slide(NoiseSettings settings, DensityFunction input) implements DensityFunctions.TransformerWithContext {
        @Override
        public double transform(DensityFunction.FunctionContext param0, double param1) {
            return NoiseRouterData.applySlide(this.settings, param1, (double)param0.blockY());
        }

        @Override
        public DensityFunction mapAll(DensityFunction.Visitor param0) {
            return param0.apply(new DensityFunctions.Slide(this.settings, this.input.mapAll(param0)));
        }

        @Override
        public double minValue() {
            return Math.min(this.input.minValue(), Math.min(this.settings.bottomSlideSettings().target(), this.settings.topSlideSettings().target()));
        }

        @Override
        public double maxValue() {
            return Math.max(this.input.maxValue(), Math.max(this.settings.bottomSlideSettings().target(), this.settings.topSlideSettings().target()));
        }
    }

    static record TerrainShaperSpline(
        DensityFunction continentalness,
        DensityFunction erosion,
        DensityFunction weirdness,
        ToFloatFunction<TerrainShaper.Point> spline,
        double minValue,
        double maxValue
    ) implements DensityFunction {
        @Override
        public double compute(DensityFunction.FunctionContext param0) {
            return Mth.clamp(
                (double)this.spline
                    .apply(
                        TerrainShaper.makePoint(
                            (float)this.continentalness.compute(param0), (float)this.erosion.compute(param0), (float)this.weirdness.compute(param0)
                        )
                    ),
                this.minValue,
                this.maxValue
            );
        }

        @Override
        public void fillArray(double[] param0, DensityFunction.ContextProvider param1) {
            for(int var0 = 0; var0 < param0.length; ++var0) {
                param0[var0] = this.compute(param1.forIndex(var0));
            }

        }

        @Override
        public DensityFunction mapAll(DensityFunction.Visitor param0) {
            return param0.apply(
                new DensityFunctions.TerrainShaperSpline(
                    this.continentalness.mapAll(param0), this.erosion.mapAll(param0), this.weirdness.mapAll(param0), this.spline, this.minValue, this.maxValue
                )
            );
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

    static record WeirdScaledSampler(DensityFunction input, NormalNoise noise, Double2DoubleFunction rarityValueMapper, double maxRarity)
        implements DensityFunctions.TransformerWithContext {
        @Override
        public double transform(DensityFunction.FunctionContext param0, double param1) {
            double var0 = this.rarityValueMapper.get(param1);
            return var0 * Math.abs(this.noise.getValue((double)param0.blockX() / var0, (double)param0.blockY() / var0, (double)param0.blockZ() / var0));
        }

        @Override
        public DensityFunction mapAll(DensityFunction.Visitor param0) {
            this.input.mapAll(param0);
            return param0.apply(new DensityFunctions.WeirdScaledSampler(this.input.mapAll(param0), this.noise, this.rarityValueMapper, this.maxRarity));
        }

        @Override
        public double minValue() {
            return 0.0;
        }

        @Override
        public double maxValue() {
            return this.maxRarity * this.noise.maxValue();
        }
    }

    static record YClampedGradient(int fromY, int toY, double fromValue, double toValue) implements DensityFunction.SimpleFunction {
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
    }
}
