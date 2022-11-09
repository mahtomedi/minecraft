package net.minecraft.client.gui.components;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CenteredStringWidget extends AbstractWidget {
    private int color = 16777215;
    private final Font font;

    public CenteredStringWidget(Component param0, Font param1) {
        this(0, 0, param1.width(param0.getVisualOrderText()), 9, param0, param1);
    }

    public CenteredStringWidget(int param0, int param1, Component param2, Font param3) {
        this(0, 0, param0, param1, param2, param3);
    }

    public CenteredStringWidget(int param0, int param1, int param2, int param3, Component param4, Font param5) {
        super(param0, param1, param2, param3, param4);
        this.font = param5;
    }

    public CenteredStringWidget color(int param0) {
        this.color = param0;
        return this;
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput param0) {
    }

    @Override
    public boolean changeFocus(boolean param0) {
        return false;
    }

    @Override
    public void renderButton(PoseStack param0, int param1, int param2, float param3) {
        drawCenteredString(param0, this.font, this.getMessage(), this.getX() + this.getWidth() / 2, this.getY() + (this.getHeight() - 9) / 2, this.color);
    }
}
