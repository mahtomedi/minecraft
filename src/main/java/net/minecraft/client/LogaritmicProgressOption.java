package net.minecraft.client;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LogaritmicProgressOption extends ProgressOption {
    public LogaritmicProgressOption(
        String param0,
        double param1,
        double param2,
        float param3,
        Function<Options, Double> param4,
        BiConsumer<Options, Double> param5,
        BiFunction<Options, ProgressOption, String> param6
    ) {
        super(param0, param1, param2, param3, param4, param5, param6);
    }

    @Override
    public double toPct(double param0) {
        return Math.log(param0 / this.minValue) / Math.log(this.maxValue / this.minValue);
    }

    @Override
    public double toValue(double param0) {
        return this.minValue * Math.pow(Math.E, Math.log(this.maxValue / this.minValue) * param0);
    }
}
