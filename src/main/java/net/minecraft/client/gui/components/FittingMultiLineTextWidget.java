package net.minecraft.client.gui.components;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FittingMultiLineTextWidget extends AbstractScrollWidget {
    private final Font font;
    private final MultiLineTextWidget multilineWidget;

    public FittingMultiLineTextWidget(int param0, int param1, int param2, int param3, Component param4, Font param5) {
        super(param0, param1, param2, param3, param4);
        this.font = param5;
        this.multilineWidget = new MultiLineTextWidget(0, 0, param4, param5).setMaxWidth(this.getWidth() - this.totalInnerPadding());
    }

    public FittingMultiLineTextWidget setColor(int param0) {
        this.multilineWidget.setColor(param0);
        return this;
    }

    @Override
    public void setWidth(int param0) {
        super.setWidth(param0);
        this.multilineWidget.setMaxWidth(this.getWidth() - this.totalInnerPadding());
    }

    @Override
    protected int getInnerHeight() {
        return this.multilineWidget.getHeight();
    }

    @Override
    protected double scrollRate() {
        return 9.0;
    }

    @Override
    protected void renderBackground(GuiGraphics param0) {
        if (this.scrollbarVisible()) {
            super.renderBackground(param0);
        } else if (this.isFocused()) {
            this.renderBorder(
                param0,
                this.getX() - this.innerPadding(),
                this.getY() - this.innerPadding(),
                this.getWidth() + this.totalInnerPadding(),
                this.getHeight() + this.totalInnerPadding()
            );
        }

    }

    @Override
    public void renderWidget(GuiGraphics param0, int param1, int param2, float param3) {
        if (this.visible) {
            if (!this.scrollbarVisible()) {
                this.renderBackground(param0);
                param0.pose().pushPose();
                param0.pose().translate((float)this.getX(), (float)this.getY(), 0.0F);
                this.multilineWidget.render(param0, param1, param2, param3);
                param0.pose().popPose();
            } else {
                super.renderWidget(param0, param1, param2, param3);
            }

        }
    }

    @Override
    protected void renderContents(GuiGraphics param0, int param1, int param2, float param3) {
        param0.pose().pushPose();
        param0.pose().translate((float)(this.getX() + this.innerPadding()), (float)(this.getY() + this.innerPadding()), 0.0F);
        this.multilineWidget.render(param0, param1, param2, param3);
        param0.pose().popPose();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput param0) {
        param0.add(NarratedElementType.TITLE, this.getMessage());
    }
}
