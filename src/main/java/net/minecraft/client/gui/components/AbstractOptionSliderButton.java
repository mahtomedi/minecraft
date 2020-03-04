package net.minecraft.client.gui.components;

import net.minecraft.client.Options;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractOptionSliderButton extends AbstractSliderButton {
    protected final Options options;

    protected AbstractOptionSliderButton(Options param0, int param1, int param2, int param3, int param4, double param5) {
        super(param1, param2, param3, param4, "", param5);
        this.options = param0;
    }
}
