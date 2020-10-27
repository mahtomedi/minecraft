package net.minecraft.client;

import java.util.function.BiConsumer;
import java.util.function.Predicate;
import javax.annotation.Nullable;
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
    @Nullable
    private final Component tooltipText;

    public BooleanOption(String param0, Predicate<Options> param1, BiConsumer<Options, Boolean> param2) {
        this(param0, null, param1, param2);
    }

    public BooleanOption(String param0, @Nullable Component param1, Predicate<Options> param2, BiConsumer<Options, Boolean> param3) {
        super(param0);
        this.getter = param2;
        this.setter = param3;
        this.tooltipText = param1;
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
        if (this.tooltipText != null) {
            this.setTooltip(Minecraft.getInstance().font.split(this.tooltipText, 200));
        }

        return new OptionButton(param1, param2, param3, 20, this, this.getMessage(param0), param1x -> {
            this.toggle(param0);
            param1x.setMessage(this.getMessage(param0));
        });
    }

    public Component getMessage(Options param0) {
        return CommonComponents.optionStatus(this.getCaption(), this.get(param0));
    }
}
