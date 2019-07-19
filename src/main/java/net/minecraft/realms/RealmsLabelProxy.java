package net.minecraft.realms;

import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsLabelProxy implements GuiEventListener {
    private final RealmsLabel label;

    public RealmsLabelProxy(RealmsLabel param0) {
        this.label = param0;
    }

    public RealmsLabel getLabel() {
        return this.label;
    }
}
