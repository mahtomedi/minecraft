package net.minecraft.client.gui.components;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class StringWidget extends AbstractStringWidget {
    private float alignX = 0.5F;

    public StringWidget(Component param0, Font param1) {
        this(0, 0, param1.width(param0.getVisualOrderText()), 9, param0, param1);
    }

    public StringWidget(int param0, int param1, Component param2, Font param3) {
        this(0, 0, param0, param1, param2, param3);
    }

    public StringWidget(int param0, int param1, int param2, int param3, Component param4, Font param5) {
        super(param0, param1, param2, param3, param4, param5);
        this.active = false;
    }

    public StringWidget setColor(int param0) {
        super.setColor(param0);
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
    public void renderWidget(PoseStack param0, int param1, int param2, float param3) {
        Component var0 = this.getMessage();
        Font var1 = this.getFont();
        int var2 = this.getX() + Math.round(this.alignX * (float)(this.getWidth() - var1.width(var0)));
        int var3 = this.getY() + (this.getHeight() - 9) / 2;
        drawString(param0, var1, var0, var2, var3, this.getColor());
    }
}
