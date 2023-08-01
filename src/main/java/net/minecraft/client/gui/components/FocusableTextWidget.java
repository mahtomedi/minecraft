package net.minecraft.client.gui.components;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FocusableTextWidget extends MultiLineTextWidget {
    private static final int BACKGROUND_COLOR = 1426063360;
    private static final int PADDING = 4;
    private final boolean alwaysShowBorder;

    public FocusableTextWidget(int param0, Component param1, Font param2) {
        this(param0, param1, param2, true);
    }

    public FocusableTextWidget(int param0, Component param1, Font param2, boolean param3) {
        super(param1, param2);
        this.setMaxWidth(param0);
        this.setCentered(true);
        this.active = true;
        this.alwaysShowBorder = param3;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput param0) {
        param0.add(NarratedElementType.TITLE, this.getMessage());
    }

    @Override
    public void renderWidget(GuiGraphics param0, int param1, int param2, float param3) {
        if (this.isFocused() || this.alwaysShowBorder) {
            int var0 = this.getX() - 4;
            int var1 = this.getY() - 4;
            int var2 = this.getWidth() + 8;
            int var3 = this.getHeight() + 8;
            int var4 = this.alwaysShowBorder ? (this.isFocused() ? -1 : -6250336) : -1;
            param0.fill(var0 + 1, var1, var0 + var2, var1 + var3, 1426063360);
            param0.renderOutline(var0, var1, var2, var3, var4);
        }

        super.renderWidget(param0, param1, param2, param3);
    }

    @Override
    public void playDownSound(SoundManager param0) {
    }
}
