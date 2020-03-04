package net.minecraft.realms;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsLabel implements GuiEventListener {
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

    public void render(Screen param0) {
        param0.drawCenteredString(Minecraft.getInstance().font, this.text, this.x, this.y, this.color);
    }

    public String getText() {
        return this.text;
    }
}
