package net.minecraft.client.gui.components;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractStringWidget extends AbstractWidget {
    private final Font font;
    private int color = 16777215;

    public AbstractStringWidget(int param0, int param1, int param2, int param3, Component param4, Font param5) {
        super(param0, param1, param2, param3, param4);
        this.font = param5;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput param0) {
    }

    public AbstractStringWidget setColor(int param0) {
        this.color = param0;
        return this;
    }

    protected final Font getFont() {
        return this.font;
    }

    protected final int getColor() {
        return this.color;
    }
}
