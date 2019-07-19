package net.minecraft.realms;

import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsLabel extends RealmsGuiEventListener {
    private final RealmsLabelProxy proxy = new RealmsLabelProxy(this);
    private final String text;
    private final int x;
    private final int y;
    private final int color;

    public RealmsLabel(String param0, int param1, int param2, int param3) {
        this.text = param0;
        this.x = param1;
        this.y = param2;
        this.color = param3;
    }

    public void render(RealmsScreen param0) {
        param0.drawCenteredString(this.text, this.x, this.y, this.color);
    }

    @Override
    public GuiEventListener getProxy() {
        return this.proxy;
    }

    public String getText() {
        return this.text;
    }
}
