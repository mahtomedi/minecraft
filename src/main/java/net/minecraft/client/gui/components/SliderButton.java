package net.minecraft.client.gui.components;

import net.minecraft.client.Options;
import net.minecraft.client.ProgressOption;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SliderButton extends AbstractOptionSliderButton {
    private final ProgressOption option;

    public SliderButton(Options param0, int param1, int param2, int param3, int param4, ProgressOption param5) {
        super(param0, param1, param2, param3, param4, (double)((float)param5.toPct(param5.get(param0))));
        this.option = param5;
        this.updateMessage();
    }

    @Override
    protected void applyValue() {
        this.option.set(this.options, this.option.toValue(this.value));
        this.options.save();
    }

    @Override
    protected void updateMessage() {
        this.setMessage(this.option.getMessage(this.options));
    }
}
