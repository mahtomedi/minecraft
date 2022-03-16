package net.minecraft.client;

import com.google.common.collect.ImmutableList;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;
import net.minecraft.client.gui.components.AbstractOptionSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.TooltipAccessor;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class OptionInstance<T> extends Option {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Function<Minecraft, Option.TooltipSupplier<T>> tooltip;
    final Function<T, Component> toString;
    private final OptionInstance.ValueSet<T> values;
    private final T initialValue;
    private final Consumer<T> onValueUpdate;
    T value;

    public static OptionInstance<Boolean> createBoolean(String param0, boolean param1, Consumer<Boolean> param2) {
        return createBoolean(param0, noTooltip(), param1, param2);
    }

    public static OptionInstance<Boolean> createBoolean(String param0, Function<Minecraft, Option.TooltipSupplier<Boolean>> param1, boolean param2) {
        return createBoolean(param0, param1, param2, param0x -> {
        });
    }

    public static OptionInstance<Boolean> createBoolean(
        String param0, Function<Minecraft, Option.TooltipSupplier<Boolean>> param1, boolean param2, Consumer<Boolean> param3
    ) {
        return new OptionInstance<>(
            param0,
            param1,
            param0x -> param0x ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF,
            new OptionInstance.Enum<>(ImmutableList.of(Boolean.TRUE, Boolean.FALSE)),
            param2,
            param3
        );
    }

    public OptionInstance(
        String param0,
        Function<Minecraft, Option.TooltipSupplier<T>> param1,
        Function<T, Component> param2,
        OptionInstance.ValueSet<T> param3,
        T param4,
        Consumer<T> param5
    ) {
        super(param0);
        this.tooltip = param1;
        this.toString = param2;
        this.values = param3;
        this.initialValue = param4;
        this.onValueUpdate = param5;
        this.value = this.initialValue;
    }

    @Override
    public AbstractWidget createButton(Options param0, int param1, int param2, int param3) {
        Option.TooltipSupplier<T> var0 = this.tooltip.apply(Minecraft.getInstance());
        return this.values.createButton(var0, param0, param1, param2, param3).apply(this);
    }

    public T get() {
        return this.value;
    }

    public void set(T param0) {
        if (!this.values.validValue(param0)) {
            LOGGER.error("Illegal option value " + param0 + " for " + this.getCaption());
            this.value = this.initialValue;
        }

        if (!Minecraft.getInstance().isRunning()) {
            this.value = param0;
        } else {
            if (!Objects.equals(this.value, param0)) {
                this.value = param0;
                this.onValueUpdate.accept(this.value);
            }

        }
    }

    public OptionInstance.ValueSet<T> values() {
        return this.values;
    }

    @OnlyIn(Dist.CLIENT)
    public static record Enum<T>(List<T> values) implements OptionInstance.ValueSet<T> {
        @Override
        public Function<OptionInstance<T>, AbstractWidget> createButton(Option.TooltipSupplier<T> param0, Options param1, int param2, int param3, int param4) {
            return param5 -> CycleButton.builder(param5.toString)
                    .withValues(this.values)
                    .withTooltip(param0)
                    .withInitialValue(param5.value)
                    .create(param2, param3, param4, 20, param5.getCaption(), (param2x, param3x) -> {
                        param5.set(param3x);
                        param1.save();
                    });
        }

        @Override
        public boolean validValue(T param0) {
            return this.values.contains(param0);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static record IntRange(int minInclusive, int maxInclusive) implements OptionInstance.SliderableValueSet<Integer> {
        @Override
        public Function<OptionInstance<Integer>, AbstractWidget> createButton(
            Option.TooltipSupplier<Integer> param0, Options param1, int param2, int param3, int param4
        ) {
            return param5 -> new OptionInstance.OptionInstanceSliderButton<>(param1, param2, param3, param4, 20, param5, this, param0);
        }

        public boolean validValue(Integer param0) {
            return param0.compareTo(this.minInclusive) >= 0 && param0.compareTo(this.maxInclusive) <= 0;
        }

        public double toSliderValue(Integer param0) {
            return (double)Mth.map((float)param0.intValue(), (float)this.minInclusive, (float)this.maxInclusive, 0.0F, 1.0F);
        }

        public Integer fromSliderValue(double param0) {
            return Mth.floor(Mth.map(param0, 0.0, 1.0, (double)this.minInclusive, (double)this.maxInclusive));
        }

        public <R> OptionInstance.SliderableValueSet<R> xmap(final IntFunction<? extends R> param0, final ToIntFunction<? super R> param1) {
            return new OptionInstance.SliderableValueSet<R>() {
                @Override
                public Function<OptionInstance<R>, AbstractWidget> createButton(
                    Option.TooltipSupplier<R> param0x, Options param1x, int param2, int param3, int param4
                ) {
                    return param5 -> new OptionInstance.OptionInstanceSliderButton<>(param1, param2, param3, param4, 20, param5, this, param0);
                }

                @Override
                public boolean validValue(R param0x) {
                    return IntRange.this.validValue(param1.applyAsInt(param0));
                }

                @Override
                public double toSliderValue(R param0x) {
                    return IntRange.this.toSliderValue(param1.applyAsInt(param0));
                }

                @Override
                public R fromSliderValue(double param0x) {
                    return param0.apply(IntRange.this.fromSliderValue(param0));
                }
            };
        }
    }

    @OnlyIn(Dist.CLIENT)
    static final class OptionInstanceSliderButton<N> extends AbstractOptionSliderButton implements TooltipAccessor {
        private final OptionInstance<N> instance;
        private final OptionInstance.SliderableValueSet<N> values;
        private final Option.TooltipSupplier<N> tooltip;

        OptionInstanceSliderButton(
            Options param0,
            int param1,
            int param2,
            int param3,
            int param4,
            OptionInstance<N> param5,
            OptionInstance.SliderableValueSet<N> param6,
            Option.TooltipSupplier<N> param7
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
    interface SliderableValueSet<T> extends OptionInstance.ValueSet<T> {
        double toSliderValue(T var1);

        T fromSliderValue(double var1);
    }

    @OnlyIn(Dist.CLIENT)
    public static enum UnitDouble implements OptionInstance.SliderableValueSet<Double> {
        INSTANCE;

        @Override
        public Function<OptionInstance<Double>, AbstractWidget> createButton(
            Option.TooltipSupplier<Double> param0, Options param1, int param2, int param3, int param4
        ) {
            return param5 -> new OptionInstance.OptionInstanceSliderButton<>(param1, param2, param3, param4, 20, param5, this, param0);
        }

        public boolean validValue(Double param0) {
            return param0 >= 0.0 && param0 <= 1.0;
        }

        public double toSliderValue(Double param0) {
            return param0;
        }

        public Double fromSliderValue(double param0) {
            return param0;
        }
    }

    @OnlyIn(Dist.CLIENT)
    interface ValueSet<T> {
        Function<OptionInstance<T>, AbstractWidget> createButton(Option.TooltipSupplier<T> var1, Options var2, int var3, int var4, int var5);

        boolean validValue(T var1);
    }
}
