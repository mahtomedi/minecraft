package net.minecraft.client;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.SliderButton;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ProgressOption extends Option {
    protected final float steps;
    protected final double minValue;
    protected double maxValue;
    private final Function<Options, Double> getter;
    private final BiConsumer<Options, Double> setter;
    private final BiFunction<Options, ProgressOption, Component> toString;

    public ProgressOption(
        String param0,
        double param1,
        double param2,
        float param3,
        Function<Options, Double> param4,
        BiConsumer<Options, Double> param5,
        BiFunction<Options, ProgressOption, Component> param6
    ) {
        super(param0);
        this.minValue = param1;
        this.maxValue = param2;
        this.steps = param3;
        this.getter = param4;
        this.setter = param5;
        this.toString = param6;
    }

    @Override
    public AbstractWidget createButton(Options param0, int param1, int param2, int param3) {
        return new SliderButton(param0, param1, param2, param3, 20, this);
    }

    public double toPct(double param0) {
        return Mth.clamp((this.clamp(param0) - this.minValue) / (this.maxValue - this.minValue), 0.0, 1.0);
    }

    public double toValue(double param0) {
        return this.clamp(Mth.lerp(Mth.clamp(param0, 0.0, 1.0), this.minValue, this.maxValue));
    }

    private double clamp(double param0) {
        if (this.steps > 0.0F) {
            param0 = (double)(this.steps * (float)Math.round(param0 / (double)this.steps));
        }

        return Mth.clamp(param0, this.minValue, this.maxValue);
    }

    public double getMinValue() {
        return this.minValue;
    }

    public double getMaxValue() {
        return this.maxValue;
    }

    public void setMaxValue(float param0) {
        this.maxValue = (double)param0;
    }

    public void set(Options param0, double param1) {
        this.setter.accept(param0, param1);
    }

    public double get(Options param0) {
        return this.getter.apply(param0);
    }

    public Component getMessage(Options param0) {
        return this.toString.apply(param0, this);
    }
}
