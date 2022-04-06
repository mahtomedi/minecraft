package net.minecraft.client;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.stream.IntStream;
import net.minecraft.client.gui.components.AbstractOptionSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.TooltipAccessor;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.OptionEnum;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public final class OptionInstance<T> {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final OptionInstance.Enum<Boolean> BOOLEAN_VALUES = new OptionInstance.Enum<>(ImmutableList.of(Boolean.TRUE, Boolean.FALSE), Codec.BOOL);
    private static final int TOOLTIP_WIDTH = 200;
    private final OptionInstance.TooltipSupplierFactory<T> tooltip;
    final Function<T, Component> toString;
    private final OptionInstance.ValueSet<T> values;
    private final Codec<T> codec;
    private final T initialValue;
    private final Consumer<T> onValueUpdate;
    final Component caption;
    T value;

    public static OptionInstance<Boolean> createBoolean(String param0, boolean param1, Consumer<Boolean> param2) {
        return createBoolean(param0, noTooltip(), param1, param2);
    }

    public static OptionInstance<Boolean> createBoolean(String param0, boolean param1) {
        return createBoolean(param0, noTooltip(), param1, param0x -> {
        });
    }

    public static OptionInstance<Boolean> createBoolean(String param0, OptionInstance.TooltipSupplierFactory<Boolean> param1, boolean param2) {
        return createBoolean(param0, param1, param2, param0x -> {
        });
    }

    public static OptionInstance<Boolean> createBoolean(
        String param0, OptionInstance.TooltipSupplierFactory<Boolean> param1, boolean param2, Consumer<Boolean> param3
    ) {
        return new OptionInstance<>(
            param0, param1, (param0x, param1x) -> param1x ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF, BOOLEAN_VALUES, param2, param3
        );
    }

    public OptionInstance(
        String param0,
        OptionInstance.TooltipSupplierFactory<T> param1,
        OptionInstance.CaptionBasedToString<T> param2,
        OptionInstance.ValueSet<T> param3,
        T param4,
        Consumer<T> param5
    ) {
        this(param0, param1, param2, param3, param3.codec(), param4, param5);
    }

    public OptionInstance(
        String param0,
        OptionInstance.TooltipSupplierFactory<T> param1,
        OptionInstance.CaptionBasedToString<T> param2,
        OptionInstance.ValueSet<T> param3,
        Codec<T> param4,
        T param5,
        Consumer<T> param6
    ) {
        this.caption = new TranslatableComponent(param0);
        this.tooltip = param1;
        this.toString = param1x -> param2.toString(this.caption, param1x);
        this.values = param3;
        this.codec = param4;
        this.initialValue = param5;
        this.onValueUpdate = param6;
        this.value = this.initialValue;
    }

    public static <T> OptionInstance.TooltipSupplierFactory<T> noTooltip() {
        return param0 -> param0x -> ImmutableList.of();
    }

    public static <T> OptionInstance.TooltipSupplierFactory<T> cachedConstantTooltip(Component param0) {
        return param1 -> {
            List<FormattedCharSequence> var0x = splitTooltip(param1, param0);
            return param1x -> var0x;
        };
    }

    public static <T extends OptionEnum> OptionInstance.CaptionBasedToString<T> forOptionEnum() {
        return (param0, param1) -> param1.getCaption();
    }

    protected static List<FormattedCharSequence> splitTooltip(Minecraft param0, Component param1) {
        return param0.font.split(param1, 200);
    }

    public AbstractWidget createButton(Options param0, int param1, int param2, int param3) {
        OptionInstance.TooltipSupplier<T> var0 = this.tooltip.apply((T)Minecraft.getInstance());
        return this.values.createButton(var0, param0, param1, param2, param3).apply(this);
    }

    public T get() {
        return this.value;
    }

    public Codec<T> codec() {
        return this.codec;
    }

    @Override
    public String toString() {
        return this.caption.getString();
    }

    public void set(T param0) {
        T var0 = this.values.validateValue(param0).orElseGet(() -> {
            LOGGER.error("Illegal option value " + param0 + " for " + this.caption);
            return this.initialValue;
        });
        if (!Minecraft.getInstance().isRunning()) {
            this.value = var0;
        } else {
            if (!Objects.equals(this.value, var0)) {
                this.value = var0;
                this.onValueUpdate.accept(this.value);
            }

        }
    }

    public OptionInstance.ValueSet<T> values() {
        return this.values;
    }

    @OnlyIn(Dist.CLIENT)
    public static record AltEnum<T>(
        List<T> values, List<T> altValues, BooleanSupplier altCondition, OptionInstance.CycleableValueSet.ValueSetter<T> valueSetter, Codec<T> codec
    ) implements OptionInstance.CycleableValueSet<T> {
        @Override
        public CycleButton.ValueListSupplier<T> valueListSupplier() {
            return CycleButton.ValueListSupplier.create(this.altCondition, this.values, this.altValues);
        }

        @Override
        public Optional<T> validateValue(T param0) {
            return (this.altCondition.getAsBoolean() ? this.altValues : this.values).contains(param0) ? Optional.of(param0) : Optional.empty();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public interface CaptionBasedToString<T> {
        Component toString(Component var1, T var2);
    }

    @OnlyIn(Dist.CLIENT)
    public static record ClampingLazyMaxIntRange(int minInclusive, IntSupplier maxSupplier)
        implements OptionInstance.IntRangeBase,
        OptionInstance.SliderableOrCyclableValueSet<Integer> {
        public Optional<Integer> validateValue(Integer param0) {
            return Optional.of(Mth.clamp(param0, this.minInclusive(), this.maxInclusive()));
        }

        @Override
        public int maxInclusive() {
            return this.maxSupplier.getAsInt();
        }

        @Override
        public Codec<Integer> codec() {
            Function<Integer, DataResult<Integer>> var0 = param0 -> {
                int var0x = this.maxSupplier.getAsInt() + 1;
                return param0.compareTo(this.minInclusive) >= 0 && param0.compareTo(var0x) <= 0
                    ? DataResult.success(param0)
                    : DataResult.error("Value " + param0 + " outside of range [" + this.minInclusive + ":" + var0x + "]", param0);
            };
            return Codec.INT.flatXmap(var0, var0);
        }

        @Override
        public boolean createCycleButton() {
            return true;
        }

        @Override
        public CycleButton.ValueListSupplier<Integer> valueListSupplier() {
            return CycleButton.ValueListSupplier.create(IntStream.range(this.minInclusive, this.maxInclusive() + 1).boxed().toList());
        }
    }

    @OnlyIn(Dist.CLIENT)
    interface CycleableValueSet<T> extends OptionInstance.ValueSet<T> {
        CycleButton.ValueListSupplier<T> valueListSupplier();

        default OptionInstance.CycleableValueSet.ValueSetter<T> valueSetter() {
            return OptionInstance::set;
        }

        @Override
        default Function<OptionInstance<T>, AbstractWidget> createButton(
            OptionInstance.TooltipSupplier<T> param0, Options param1, int param2, int param3, int param4
        ) {
            return param5 -> CycleButton.builder(param5.toString)
                    .withValues(this.valueListSupplier())
                    .withTooltip(param0)
                    .withInitialValue(param5.value)
                    .create(param2, param3, param4, 20, param5.caption, (param2x, param3x) -> {
                        this.valueSetter().set(param5, param3x);
                        param1.save();
                    });
        }

        @OnlyIn(Dist.CLIENT)
        public interface ValueSetter<T> {
            void set(OptionInstance<T> var1, T var2);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static record Enum<T>(List<T> values, Codec<T> codec) implements OptionInstance.CycleableValueSet<T> {
        @Override
        public Optional<T> validateValue(T param0) {
            return this.values.contains(param0) ? Optional.of(param0) : Optional.empty();
        }

        @Override
        public CycleButton.ValueListSupplier<T> valueListSupplier() {
            return CycleButton.ValueListSupplier.create(this.values);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static record IntRange(int minInclusive, int maxInclusive) implements OptionInstance.IntRangeBase {
        public Optional<Integer> validateValue(Integer param0) {
            return param0.compareTo(this.minInclusive()) >= 0 && param0.compareTo(this.maxInclusive()) <= 0 ? Optional.of(param0) : Optional.empty();
        }

        @Override
        public Codec<Integer> codec() {
            return Codec.intRange(this.minInclusive, this.maxInclusive + 1);
        }
    }

    @OnlyIn(Dist.CLIENT)
    interface IntRangeBase extends OptionInstance.SliderableValueSet<Integer> {
        int minInclusive();

        int maxInclusive();

        default double toSliderValue(Integer param0) {
            return (double)Mth.map((float)param0.intValue(), (float)this.minInclusive(), (float)this.maxInclusive(), 0.0F, 1.0F);
        }

        default Integer fromSliderValue(double param0) {
            return Mth.floor(Mth.map(param0, 0.0, 1.0, (double)this.minInclusive(), (double)this.maxInclusive()));
        }

        default <R> OptionInstance.SliderableValueSet<R> xmap(final IntFunction<? extends R> param0, final ToIntFunction<? super R> param1) {
            return new OptionInstance.SliderableValueSet<R>() {
                @Override
                public Optional<R> validateValue(R param0x) {
                    return IntRangeBase.this.validateValue((T)Integer.valueOf(param1.applyAsInt(param0))).map(param0::apply);
                }

                @Override
                public double toSliderValue(R param0x) {
                    return IntRangeBase.this.toSliderValue(param1.applyAsInt(param0));
                }

                @Override
                public R fromSliderValue(double param0x) {
                    return param0.apply(IntRangeBase.this.fromSliderValue(param0));
                }

                @Override
                public Codec<R> codec() {
                    return IntRangeBase.this.codec().xmap(param0::apply, param1::applyAsInt);
                }
            };
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static record LazyEnum<T>(Supplier<List<T>> values, Function<T, Optional<T>> validateValue, Codec<T> codec)
        implements OptionInstance.CycleableValueSet<T> {
        @Override
        public Optional<T> validateValue(T param0) {
            return this.validateValue.apply(param0);
        }

        @Override
        public CycleButton.ValueListSupplier<T> valueListSupplier() {
            return CycleButton.ValueListSupplier.create(this.values.get());
        }
    }

    @OnlyIn(Dist.CLIENT)
    static final class OptionInstanceSliderButton<N> extends AbstractOptionSliderButton implements TooltipAccessor {
        private final OptionInstance<N> instance;
        private final OptionInstance.SliderableValueSet<N> values;
        private final OptionInstance.TooltipSupplier<N> tooltip;

        OptionInstanceSliderButton(
            Options param0,
            int param1,
            int param2,
            int param3,
            int param4,
            OptionInstance<N> param5,
            OptionInstance.SliderableValueSet<N> param6,
            OptionInstance.TooltipSupplier<N> param7
        ) {
            super(param0, param1, param2, param3, param4, param6.toSliderValue(param5.get()));
            this.instance = param5;
            this.values = param6;
            this.tooltip = param7;
            this.updateMessage();
        }

        @Override
        protected void updateMessage() {
            this.setMessage(this.instance.toString.apply(this.instance.get()));
        }

        @Override
        protected void applyValue() {
            this.instance.set(this.values.fromSliderValue(this.value));
            this.options.save();
        }

        @Override
        public List<FormattedCharSequence> getTooltip() {
            return this.tooltip.apply(this.values.fromSliderValue(this.value));
        }
    }

    @OnlyIn(Dist.CLIENT)
    interface SliderableOrCyclableValueSet<T> extends OptionInstance.CycleableValueSet<T>, OptionInstance.SliderableValueSet<T> {
        boolean createCycleButton();

        @Override
        default Function<OptionInstance<T>, AbstractWidget> createButton(
            OptionInstance.TooltipSupplier<T> param0, Options param1, int param2, int param3, int param4
        ) {
            return this.createCycleButton()
                ? OptionInstance.CycleableValueSet.super.createButton(param0, param1, param2, param3, param4)
                : OptionInstance.SliderableValueSet.super.createButton(param0, param1, param2, param3, param4);
        }
    }

    @OnlyIn(Dist.CLIENT)
    interface SliderableValueSet<T> extends OptionInstance.ValueSet<T> {
        double toSliderValue(T var1);

        T fromSliderValue(double var1);

        @Override
        default Function<OptionInstance<T>, AbstractWidget> createButton(
            OptionInstance.TooltipSupplier<T> param0, Options param1, int param2, int param3, int param4
        ) {
            return param5 -> new OptionInstance.OptionInstanceSliderButton<>(param1, param2, param3, param4, 20, param5, this, param0);
        }
    }

    @FunctionalInterface
    @OnlyIn(Dist.CLIENT)
    public interface TooltipSupplier<T> extends Function<T, List<FormattedCharSequence>> {
    }

    @OnlyIn(Dist.CLIENT)
    public interface TooltipSupplierFactory<T> extends Function<Minecraft, OptionInstance.TooltipSupplier<T>> {
    }

    @OnlyIn(Dist.CLIENT)
    public static enum UnitDouble implements OptionInstance.SliderableValueSet<Double> {
        INSTANCE;

        public Optional<Double> validateValue(Double param0) {
            return param0 >= 0.0 && param0 <= 1.0 ? Optional.of(param0) : Optional.empty();
        }

        public double toSliderValue(Double param0) {
            return param0;
        }

        public Double fromSliderValue(double param0) {
            return param0;
        }

        public <R> OptionInstance.SliderableValueSet<R> xmap(final DoubleFunction<? extends R> param0, final ToDoubleFunction<? super R> param1) {
            return new OptionInstance.SliderableValueSet<R>() {
                @Override
                public Optional<R> validateValue(R param0x) {
                    return UnitDouble.this.validateValue(param1.applyAsDouble(param0)).map(param0::apply);
                }

                @Override
                public double toSliderValue(R param0x) {
                    return UnitDouble.this.toSliderValue(param1.applyAsDouble(param0));
                }

                @Override
                public R fromSliderValue(double param0x) {
                    return param0.apply(UnitDouble.this.fromSliderValue(param0));
                }

                @Override
                public Codec<R> codec() {
                    return UnitDouble.this.codec().xmap(param0::apply, param1::applyAsDouble);
                }
            };
        }

        @Override
        public Codec<Double> codec() {
            return Codec.either(Codec.doubleRange(0.0, 1.0), Codec.BOOL)
                .xmap(param0 -> param0.map(param0x -> param0x, param0x -> param0x ? 1.0 : 0.0), Either::left);
        }
    }

    @OnlyIn(Dist.CLIENT)
    interface ValueSet<T> {
        Function<OptionInstance<T>, AbstractWidget> createButton(OptionInstance.TooltipSupplier<T> var1, Options var2, int var3, int var4, int var5);

        Optional<T> validateValue(T var1);

        Codec<T> codec();
    }
}
