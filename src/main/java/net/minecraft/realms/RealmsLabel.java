package net.minecraft.realms;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsLabel implements GuiEventListener {
    private final Component text;
    private final int x;
    private final int y;
    private final int color;

    public RealmsLabel(Component param0, int param1, int param2, int param3) {
        this.text = param0;
        this.x = param1;
        this.y = param2;
        this.color = param3;
    }

    public void render(Screen param0, PoseStack param1) {
        Screen.drawCenteredString(param1, Minecraft.getInstance().font, this.text, this.x, this.y, this.color);
    }

    public String getText() {
        return this.text.getString();
    }
}
