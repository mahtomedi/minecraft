package net.minecraft.client.gui.components;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class StringWidget extends AbstractWidget {
    private int color = 16777215;
    private final Font font;
    private float alignX = 0.5F;

    public StringWidget(Component param0, Font param1) {
        this(0, 0, param1.width(param0.getVisualOrderText()), 9, param0, param1);
    }

    public StringWidget(int param0, int param1, Component param2, Font param3) {
        this(0, 0, param0, param1, param2, param3);
    }

    public StringWidget(int param0, int param1, int param2, int param3, Component param4, Font param5) {
        super(param0, param1, param2, param3, param4);
        this.font = param5;
        this.active = false;
    }

    public StringWidget color(int param0) {
        this.color = param0;
        return this;
    }

    private StringWidget horizontalAlignment(float param0) {
        this.alignX = param0;
        return this;
    }

    public StringWidget alignLeft() {
        return this.horizontalAlignment(0.0F);
    }

    public StringWidget alignCenter() {
        return this.horizontalAlignment(0.5F);
    }

    public StringWidget alignRight() {
        return this.horizontalAlignment(1.0F);
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput param0) {
    }

    @Override
    public void renderWidget(PoseStack param0, int param1, int param2, float param3) {
        Component var0 = this.getMessage();
        int var1 = this.getX() + Math.round(this.alignX * (float)(this.getWidth() - this.font.width(var0)));
        int var2 = this.getY() + (this.getHeight() - 9) / 2;
        drawString(param0, this.font, var0, var1, var2, this.color);
    }
}
