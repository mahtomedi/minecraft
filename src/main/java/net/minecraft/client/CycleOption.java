package net.minecraft.client;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CycleOption<T> extends Option {
    private final CycleOption.OptionSetter<T> setter;
    private final Function<Options, T> getter;
    private final Supplier<CycleButton.Builder<T>> buttonSetup;
    private Function<Minecraft, CycleButton.TooltipSupplier<T>> tooltip = param0x -> param0xx -> ImmutableList.of();

    private CycleOption(String param0, Function<Options, T> param1, CycleOption.OptionSetter<T> param2, Supplier<CycleButton.Builder<T>> param3) {
        super(param0);
        this.getter = param1;
        this.setter = param2;
        this.buttonSetup = param3;
    }

    public static <T> CycleOption<T> create(
        String param0, List<T> param1, Function<T, Component> param2, Function<Options, T> param3, CycleOption.OptionSetter<T> param4
    ) {
        return new CycleOption<>(param0, param3, param4, () -> CycleButton.builder(param2).withValues(param1));
    }

    public static <T> CycleOption<T> create(
        String param0, Supplier<List<T>> param1, Function<T, Component> param2, Function<Options, T> param3, CycleOption.OptionSetter<T> param4
    ) {
        return new CycleOption<>(param0, param3, param4, () -> CycleButton.builder(param2).withValues(param1.get()));
    }

    public static <T> CycleOption<T> create(
        String param0,
        List<T> param1,
        List<T> param2,
        BooleanSupplier param3,
        Function<T, Component> param4,
        Function<Options, T> param5,
        CycleOption.OptionSetter<T> param6
    ) {
        return new CycleOption<>(param0, param5, param6, () -> CycleButton.builder(param4).withValues(param3, param1, param2));
    }

    public static <T> CycleOption<T> create(
        String param0, T[] param1, Function<T, Component> param2, Function<Options, T> param3, CycleOption.OptionSetter<T> param4
    ) {
        return new CycleOption<>(param0, param3, param4, () -> CycleButton.builder(param2).withValues(param1));
    }

    public static CycleOption<Boolean> createBinaryOption(
        String param0, Component param1, Component param2, Function<Options, Boolean> param3, CycleOption.OptionSetter<Boolean> param4
    ) {
        return new CycleOption<>(param0, param3, param4, () -> CycleButton.booleanBuilder(param1, param2));
    }

    public static CycleOption<Boolean> createOnOff(String param0, Function<Options, Boolean> param1, CycleOption.OptionSetter<Boolean> param2) {
        return new CycleOption<>(param0, param1, param2, CycleButton::onOffBuilder);
    }

    public static CycleOption<Boolean> createOnOff(String param0, Component param1, Function<Options, Boolean> param2, CycleOption.OptionSetter<Boolean> param3) {
        return createOnOff(param0, param2, param3).setTooltip(param1x -> {
            List<FormattedCharSequence> var0x = param1x.font.split(param1, 200);
            return param1xx -> var0x;
        });
    }

    public CycleOption<T> setTooltip(Function<Minecraft, CycleButton.TooltipSupplier<T>> param0) {
        this.tooltip = param0;
        return this;
    }

    @Override
    public AbstractWidget createButton(Options param0, int param1, int param2, int param3) {
        CycleButton.TooltipSupplier<T> var0 = this.tooltip.apply(Minecraft.getInstance());
        return this.buttonSetup
            .get()
            .withTooltip(var0)
            .withInitialValue(this.getter.apply(param0))
            .create(param1, param2, param3, 20, this.getCaption(), (param1x, param2x) -> {
                this.setter.accept(param0, this, param2x);
                param0.save();
            });
    }

    @FunctionalInterface
    @OnlyIn(Dist.CLIENT)
    public interface OptionSetter<T> {
        void accept(Options var1, Option var2, T var3);
    }
}
