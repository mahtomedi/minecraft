package net.minecraft.client.gui.components;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.client.Option;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CycleButton<T> extends AbstractButton implements TooltipAccessor {
    static final BooleanSupplier DEFAULT_ALT_LIST_SELECTOR = Screen::hasAltDown;
    private static final List<Boolean> BOOLEAN_OPTIONS = ImmutableList.of(Boolean.TRUE, Boolean.FALSE);
    private final Component name;
    private int index;
    private T value;
    private final CycleButton.ValueListSupplier<T> values;
    private final Function<T, Component> valueStringifier;
    private final Function<CycleButton<T>, MutableComponent> narrationProvider;
    private final CycleButton.OnValueChange<T> onValueChange;
    private final Option.TooltipSupplier<T> tooltipSupplier;
    private final boolean displayOnlyValue;

    CycleButton(
        int param0,
        int param1,
        int param2,
        int param3,
        Component param4,
        Component param5,
        int param6,
        T param7,
        CycleButton.ValueListSupplier<T> param8,
        Function<T, Component> param9,
        Function<CycleButton<T>, MutableComponent> param10,
        CycleButton.OnValueChange<T> param11,
        Option.TooltipSupplier<T> param12,
        boolean param13
    ) {
        super(param0, param1, param2, param3, param4);
        this.name = param5;
        this.index = param6;
        this.value = param7;
        this.values = param8;
        this.valueStringifier = param9;
        this.narrationProvider = param10;
        this.onValueChange = param11;
        this.tooltipSupplier = param12;
        this.displayOnlyValue = param13;
    }

    @Override
    public void onPress() {
        if (Screen.hasShiftDown()) {
            this.cycleValue(-1);
        } else {
            this.cycleValue(1);
        }

    }

    private void cycleValue(int param0) {
        List<T> var0 = this.values.getSelectedList();
        this.index = Mth.positiveModulo(this.index + param0, var0.size());
        T var1 = var0.get(this.index);
        this.updateValue(var1);
        this.onValueChange.onValueChange(this, var1);
    }

    private T getCycledValue(int param0) {
        List<T> var0 = this.values.getSelectedList();
        return var0.get(Mth.positiveModulo(this.index + param0, var0.size()));
    }

    @Override
    public boolean mouseScrolled(double param0, double param1, double param2) {
        if (param2 > 0.0) {
            this.cycleValue(-1);
        } else if (param2 < 0.0) {
            this.cycleValue(1);
        }

        return true;
    }

    public void setValue(T param0) {
        List<T> var0 = this.values.getSelectedList();
        int var1 = var0.indexOf(param0);
        if (var1 != -1) {
            this.index = var1;
        }

        this.updateValue(param0);
    }

    private void updateValue(T param0) {
        Component var0 = this.createLabelForValue(param0);
        this.setMessage(var0);
        this.value = param0;
    }

    private Component createLabelForValue(T param0) {
        return (Component)(this.displayOnlyValue ? this.valueStringifier.apply(param0) : this.createFullName(param0));
    }

    private MutableComponent createFullName(T param0) {
        return CommonComponents.optionNameValue(this.name, this.valueStringifier.apply(param0));
    }

    public T getValue() {
        return this.value;
    }

    @Override
    protected MutableComponent createNarrationMessage() {
        return this.narrationProvider.apply(this);
    }

    @Override
    public void updateNarration(NarrationElementOutput param0) {
        param0.add(NarratedElementType.TITLE, (Component)this.createNarrationMessage());
        if (this.active) {
            T var0 = this.getCycledValue(1);
            Component var1 = this.createLabelForValue(var0);
            if (this.isFocused()) {
                param0.add(NarratedElementType.USAGE, (Component)(new TranslatableComponent("narration.cycle_button.usage.focused", var1)));
            } else {
                param0.add(NarratedElementType.USAGE, (Component)(new TranslatableComponent("narration.cycle_button.usage.hovered", var1)));
            }
        }

    }

    public MutableComponent createDefaultNarrationMessage() {
        return wrapDefaultNarrationMessage((Component)(this.displayOnlyValue ? this.createFullName(this.value) : this.getMessage()));
    }

    @Override
    public List<FormattedCharSequence> getTooltip() {
        return this.tooltipSupplier.apply(this.value);
    }

    public static <T> CycleButton.Builder<T> builder(Function<T, Component> param0) {
        return new CycleButton.Builder<>(param0);
    }

    public static CycleButton.Builder<Boolean> booleanBuilder(Component param0, Component param1) {
        return new CycleButton.Builder<>(param2 -> param2 ? param0 : param1).withValues(BOOLEAN_OPTIONS);
    }

    public static CycleButton.Builder<Boolean> onOffBuilder() {
        return new CycleButton.Builder<>(param0 -> param0 ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF).withValues(BOOLEAN_OPTIONS);
    }

    public static CycleButton.Builder<Boolean> onOffBuilder(boolean param0) {
        return onOffBuilder().withInitialValue(param0);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Builder<T> {
        private int initialIndex;
        @Nullable
        private T initialValue;
        private final Function<T, Component> valueStringifier;
        private Option.TooltipSupplier<T> tooltipSupplier = param0x -> ImmutableList.of();
        private Function<CycleButton<T>, MutableComponent> narrationProvider = CycleButton::createDefaultNarrationMessage;
        private CycleButton.ValueListSupplier<T> values = CycleButton.ValueListSupplier.create(ImmutableList.of());
        private boolean displayOnlyValue;

        public Builder(Function<T, Component> param0) {
            this.valueStringifier = param0;
        }

        public CycleButton.Builder<T> withValues(Collection<T> param0) {
            this.values = CycleButton.ValueListSupplier.create(param0);
            return this;
        }

        @SafeVarargs
        public final CycleButton.Builder<T> withValues(T... param0) {
            return this.withValues(ImmutableList.copyOf(param0));
        }

        public CycleButton.Builder<T> withValues(List<T> param0, List<T> param1) {
            this.values = CycleButton.ValueListSupplier.create(CycleButton.DEFAULT_ALT_LIST_SELECTOR, param0, param1);
            return this;
        }

        public CycleButton.Builder<T> withValues(BooleanSupplier param0, List<T> param1, List<T> param2) {
            this.values = CycleButton.ValueListSupplier.create(param0, param1, param2);
            return this;
        }

        public CycleButton.Builder<T> withTooltip(Option.TooltipSupplier<T> param0) {
            this.tooltipSupplier = param0;
            return this;
        }

        public CycleButton.Builder<T> withInitialValue(T param0) {
            this.initialValue = param0;
            int var0 = this.values.getDefaultList().indexOf(param0);
            if (var0 != -1) {
                this.initialIndex = var0;
            }

            return this;
        }

        public CycleButton.Builder<T> withCustomNarration(Function<CycleButton<T>, MutableComponent> param0) {
            this.narrationProvider = param0;
            return this;
        }

        public CycleButton.Builder<T> displayOnlyValue() {
            this.displayOnlyValue = true;
            return this;
        }

        public CycleButton<T> create(int param0, int param1, int param2, int param3, Component param4) {
            return this.create(param0, param1, param2, param3, param4, (param0x, param1x) -> {
            });
        }

        public CycleButton<T> create(int param0, int param1, int param2, int param3, Component param4, CycleButton.OnValueChange<T> param5) {
            List<T> var0 = this.values.getDefaultList();
            if (var0.isEmpty()) {
                throw new IllegalStateException("No values for cycle button");
            } else {
                T var1 = (T)(this.initialValue != null ? this.initialValue : var0.get(this.initialIndex));
                Component var2 = this.valueStringifier.apply(var1);
                Component var3 = (Component)(this.displayOnlyValue ? var2 : CommonComponents.optionNameValue(param4, var2));
                return new CycleButton<>(
                    param0,
                    param1,
                    param2,
                    param3,
                    var3,
                    param4,
                    this.initialIndex,
                    var1,
                    this.values,
                    this.valueStringifier,
                    this.narrationProvider,
                    param5,
                    this.tooltipSupplier,
                    this.displayOnlyValue
                );
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public interface OnValueChange<T> {
        void onValueChange(CycleButton<T> var1, T var2);
    }

    @OnlyIn(Dist.CLIENT)
    interface ValueListSupplier<T> {
        List<T> getSelectedList();

        List<T> getDefaultList();

        static <T> CycleButton.ValueListSupplier<T> create(Collection<T> param0) {
            final List<T> var0 = ImmutableList.copyOf(param0);
            return new CycleButton.ValueListSupplier<T>() {
                @Override
                public List<T> getSelectedList() {
                    return var0;
                }

                @Override
                public List<T> getDefaultList() {
                    return var0;
                }
            };
        }

        static <T> CycleButton.ValueListSupplier<T> create(final BooleanSupplier param0, List<T> param1, List<T> param2) {
            final List<T> var0 = ImmutableList.copyOf(param1);
            final List<T> var1 = ImmutableList.copyOf(param2);
            return new CycleButton.ValueListSupplier<T>() {
                @Override
                public List<T> getSelectedList() {
                    return param0.getAsBoolean() ? var1 : var0;
                }

                @Override
                public List<T> getDefaultList() {
                    return var0;
                }
            };
        }
    }
}
