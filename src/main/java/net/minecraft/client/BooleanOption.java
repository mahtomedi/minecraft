package net.minecraft.client;

import java.util.function.BiConsumer;
import java.util.function.Predicate;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.OptionButton;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BooleanOption extends Option {
    private final Predicate<Options> getter;
    private final BiConsumer<Options, Boolean> setter;

    public BooleanOption(String param0, Predicate<Options> param1, BiConsumer<Options, Boolean> param2) {
        super(param0);
        this.getter = param1;
        this.setter = param2;
    }

    public void set(Options param0, String param1) {
        this.set(param0, "true".equals(param1));
    }

    public void toggle(Options param0) {
        this.set(param0, !this.get(param0));
        param0.save();
    }

    private void set(Options param0, boolean param1) {
        this.setter.accept(param0, param1);
    }

    public boolean get(Options param0) {
        return this.getter.test(param0);
    }

    @Override
    public AbstractWidget createButton(Options param0, int param1, int param2, int param3) {
        return new OptionButton(param1, param2, param3, 20, this, this.getMessage(param0), param1x -> {
            this.toggle(param0);
            param1x.setMessage(this.getMessage(param0));
        });
    }

    public Component getMessage(Options param0) {
        return CommonComponents.optionStatus(this.getCaption(), this.get(param0));
    }
}
