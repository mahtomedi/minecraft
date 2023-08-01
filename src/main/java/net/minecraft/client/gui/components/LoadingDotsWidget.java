package net.minecraft.client.gui.components;

import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.LoadingDotsText;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LoadingDotsWidget extends AbstractWidget {
    private final Font font;

    public LoadingDotsWidget(Font param0, Component param1) {
        super(0, 0, param0.width(param1), 9 * 2, param1);
        this.font = param0;
    }

    @Override
    protected void renderWidget(GuiGraphics param0, int param1, int param2, float param3) {
        int var0 = this.getX() + this.getWidth() / 2;
        int var1 = this.getY() + this.getHeight() / 2;
        Component var2 = this.getMessage();
        param0.drawString(this.font, var2, var0 - this.font.width(var2) / 2, var1 - 9, -1, false);
        String var3 = LoadingDotsText.get(Util.getMillis());
        param0.drawString(this.font, var3, var0 - this.font.width(var3) / 2, var1, -8355712, false);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput param0) {
        param0.add(NarratedElementType.TITLE, this.getMessage());
    }
}
