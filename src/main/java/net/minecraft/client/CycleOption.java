package net.minecraft.client;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.OptionButton;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CycleOption extends Option {
    private final BiConsumer<Options, Integer> setter;
    private final BiFunction<Options, CycleOption, String> toString;

    public CycleOption(String param0, BiConsumer<Options, Integer> param1, BiFunction<Options, CycleOption, String> param2) {
        super(param0);
        this.setter = param1;
        this.toString = param2;
    }

    public void toggle(Options param0, int param1) {
        this.setter.accept(param0, param1);
        param0.save();
    }

    @Override
    public AbstractWidget createButton(Options param0, int param1, int param2, int param3) {
        return new OptionButton(param1, param2, param3, 20, this, this.getMessage(param0), param1x -> {
            this.toggle(param0, 1);
            param1x.setMessage(this.getMessage(param0));
        });
    }

    public String getMessage(Options param0) {
        return this.toString.apply(param0, this);
    }
}
