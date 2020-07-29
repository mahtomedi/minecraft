package net.minecraft.client.gui.components;

import java.util.List;
import java.util.Optional;
import net.minecraft.client.Option;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class OptionButton extends Button implements TooltipAccessor {
    private final Option option;

    public OptionButton(int param0, int param1, int param2, int param3, Option param4, Component param5, Button.OnPress param6) {
        super(param0, param1, param2, param3, param5, param6);
        this.option = param4;
    }

    public Option getOption() {
        return this.option;
    }

    @Override
    public Optional<List<FormattedCharSequence>> getTooltip() {
        return this.option.getTooltip();
    }
}
