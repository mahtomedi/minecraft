package net.minecraft.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractScrollWidget extends AbstractWidget implements Renderable, GuiEventListener {
    private static final int BORDER_COLOR_FOCUSED = -1;
    private static final int BORDER_COLOR = -6250336;
    private static final int BACKGROUND_COLOR = -16777216;
    private static final int INNER_PADDING = 4;
    private double scrollAmount;
    private boolean scrolling;

    public AbstractScrollWidget(int param0, int param1, int param2, int param3, Component param4) {
        super(param0, param1, param2, param3, param4);
    }

    @Override
    public boolean mouseClicked(double param0, double param1, int param2) {
        if (!this.visible) {
            return false;
        } else {
            boolean var0 = this.withinContentAreaPoint(param0, param1);
            boolean var1 = this.scrollbarVisible()
                && param0 >= (double)(this.getX() + this.width)
                && param0 <= (double)(this.getX() + this.width + 8)
                && param1 >= (double)this.getY()
                && param1 < (double)(this.getY() + this.height);
            this.setFocused(var0 || var1);
            if (var1 && param2 == 0) {
                this.scrolling = true;
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public boolean mouseReleased(double param0, double param1, int param2) {
        if (param2 == 0) {
            this.scrolling = false;
        }

        return super.mouseReleased(param0, param1, param2);
    }

    @Override
    public boolean mouseDragged(double param0, double param1, int param2, double param3, double param4) {
        if (this.visible && this.isFocused() && this.scrolling) {
            if (param1 < (double)this.getY()) {
                this.setScrollAmount(0.0);
            } else if (param1 > (double)(this.getY() + this.height)) {
                this.setScrollAmount((double)this.getMaxScrollAmount());
            } else {
                int var0 = this.getScrollBarHeight();
                double var1 = (double)Math.max(1, this.getMaxScrollAmount() / (this.height - var0));
                this.setScrollAmount(this.scrollAmount + param4 * var1);
            }

            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean mouseScrolled(double param0, double param1, double param2) {
        if (!this.visible) {
            return false;
        } else {
            this.setScrollAmount(this.scrollAmount - param2 * this.scrollRate());
            return true;
        }
    }

    @Override
    public void renderButton(PoseStack param0, int param1, int param2, float param3) {
        if (this.visible) {
            this.renderBackground(param0);
            enableScissor(this.getX() + 1, this.getY() + 1, this.getX() + this.width - 1, this.getY() + this.height - 1);
            param0.pushPose();
            param0.translate(0.0, -this.scrollAmount, 0.0);
            this.renderContents(param0, param1, param2, param3);
            param0.popPose();
            disableScissor();
            this.renderDecorations(param0);
        }
    }

    private int getScrollBarHeight() {
        return Mth.clamp((int)((float)(this.height * this.height) / (float)this.getContentHeight()), 32, this.height);
    }

    protected void renderDecorations(PoseStack param0) {
        if (this.scrollbarVisible()) {
            this.renderScrollBar();
        }

    }

    protected int innerPadding() {
        return 4;
    }

    protected int totalInnerPadding() {
        return this.innerPadding() * 2;
    }

    protected double scrollAmount() {
        return this.scrollAmount;
    }

    protected void setScrollAmount(double param0) {
        this.scrollAmount = Mth.clamp(param0, 0.0, (double)this.getMaxScrollAmount());
    }

    protected int getMaxScrollAmount() {
        return Math.max(0, this.getContentHeight() - (this.height - 4));
    }

    private int getContentHeight() {
        return this.getInnerHeight() + 4;
    }

    private void renderBackground(PoseStack param0) {
        int var0 = this.isFocused() ? -1 : -6250336;
        fill(param0, this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, var0);
        fill(param0, this.getX() + 1, this.getY() + 1, this.getX() + this.width - 1, this.getY() + this.height - 1, -16777216);
    }

    private void renderScrollBar() {
        int var0 = this.getScrollBarHeight();
        int var1 = this.getX() + this.width;
        int var2 = this.getX() + this.width + 8;
        int var3 = Math.max(this.getY(), (int)this.scrollAmount * (this.height - var0) / this.getMaxScrollAmount() + this.getY());
        int var4 = var3 + var0;
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator var5 = Tesselator.getInstance();
        BufferBuilder var6 = var5.getBuilder();
        var6.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        var6.vertex((double)var1, (double)var4, 0.0).color(128, 128, 128, 255).endVertex();
        var6.vertex((double)var2, (double)var4, 0.0).color(128, 128, 128, 255).endVertex();
        var6.vertex((double)var2, (double)var3, 0.0).color(128, 128, 128, 255).endVertex();
        var6.vertex((double)var1, (double)var3, 0.0).color(128, 128, 128, 255).endVertex();
        var6.vertex((double)var1, (double)(var4 - 1), 0.0).color(192, 192, 192, 255).endVertex();
        var6.vertex((double)(var2 - 1), (double)(var4 - 1), 0.0).color(192, 192, 192, 255).endVertex();
        var6.vertex((double)(var2 - 1), (double)var3, 0.0).color(192, 192, 192, 255).endVertex();
        var6.vertex((double)var1, (double)var3, 0.0).color(192, 192, 192, 255).endVertex();
        var5.end();
    }

    protected boolean withinContentAreaTopBottom(int param0, int param1) {
        return (double)param1 - this.scrollAmount >= (double)this.getY() && (double)param0 - this.scrollAmount <= (double)(this.getY() + this.height);
    }

    protected boolean withinContentAreaPoint(double param0, double param1) {
        return param0 >= (double)this.getX()
            && param0 < (double)(this.getX() + this.width)
            && param1 >= (double)this.getY()
            && param1 < (double)(this.getY() + this.height);
    }

    protected abstract int getInnerHeight();

    protected abstract boolean scrollbarVisible();

    protected abstract double scrollRate();

    protected abstract void renderContents(PoseStack var1, int var2, int var3, float var4);
}
