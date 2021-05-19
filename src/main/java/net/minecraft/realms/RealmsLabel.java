package net.minecraft.realms;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsLabel implements Widget {
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

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        GuiComponent.drawCenteredString(param0, Minecraft.getInstance().font, this.text, this.x, this.y, this.color);
    }

    public Component getText() {
        return this.text;
    }
}
