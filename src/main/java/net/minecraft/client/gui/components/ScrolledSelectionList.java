package net.minecraft.client.gui.components;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class ScrolledSelectionList extends AbstractContainerEventHandler implements Widget {
    protected static final int NO_DRAG = -1;
    protected static final int DRAG_OUTSIDE = -2;
    protected final Minecraft minecraft;
    protected int width;
    protected int height;
    protected int y0;
    protected int y1;
    protected int x1;
    protected int x0;
    protected final int itemHeight;
    protected boolean centerListVertically = true;
    protected int yDrag = -2;
    protected double yo;
    protected boolean visible = true;
    protected boolean renderSelection = true;
    protected boolean renderHeader;
    protected int headerHeight;
    private boolean scrolling;

    public ScrolledSelectionList(Minecraft param0, int param1, int param2, int param3, int param4, int param5) {
        this.minecraft = param0;
        this.width = param1;
        this.height = param2;
        this.y0 = param3;
        this.y1 = param4;
        this.itemHeight = param5;
        this.x0 = 0;
        this.x1 = param1;
    }

    public void updateSize(int param0, int param1, int param2, int param3) {
        this.width = param0;
        this.height = param1;
        this.y0 = param2;
        this.y1 = param3;
        this.x0 = 0;
        this.x1 = param0;
    }

    public void setRenderSelection(boolean param0) {
        this.renderSelection = param0;
    }

    protected void setRenderHeader(boolean param0, int param1) {
        this.renderHeader = param0;
        this.headerHeight = param1;
        if (!param0) {
            this.headerHeight = 0;
        }

    }

    public void setVisible(boolean param0) {
        this.visible = param0;
    }

    public boolean isVisible() {
        return this.visible;
    }

    protected abstract int getItemCount();

    @Override
    public List<? extends GuiEventListener> children() {
        return Collections.emptyList();
    }

    protected boolean selectItem(int param0, int param1, double param2, double param3) {
        return true;
    }

    protected abstract boolean isSelectedItem(int var1);

    protected int getMaxPosition() {
        return this.getItemCount() * this.itemHeight + this.headerHeight;
    }

    protected abstract void renderBackground();

    protected void updateItemPosition(int param0, int param1, int param2, float param3) {
    }

    protected abstract void renderItem(int var1, int var2, int var3, int var4, int var5, int var6, float var7);

    protected void renderHeader(int param0, int param1, Tesselator param2) {
    }

    protected void clickedHeader(int param0, int param1) {
    }

    protected void renderDecorations(int param0, int param1) {
    }

    public int getItemAtPosition(double param0, double param1) {
        int var0 = this.x0 + this.width / 2 - this.getRowWidth() / 2;
        int var1 = this.x0 + this.width / 2 + this.getRowWidth() / 2;
        int var2 = Mth.floor(param1 - (double)this.y0) - this.headerHeight + (int)this.yo - 4;
        int var3 = var2 / this.itemHeight;
        return param0 < (double)this.getScrollbarPosition()
                && param0 >= (double)var0
                && param0 <= (double)var1
                && var3 >= 0
                && var2 >= 0
                && var3 < this.getItemCount()
            ? var3
            : -1;
    }

    protected void capYPosition() {
        this.yo = Mth.clamp(this.yo, 0.0, (double)this.getMaxScroll());
    }

    public int getMaxScroll() {
        return Math.max(0, this.getMaxPosition() - (this.y1 - this.y0 - 4));
    }

    public void centerScrollOn(int param0) {
        this.yo = (double)(param0 * this.itemHeight + this.itemHeight / 2 - (this.y1 - this.y0) / 2);
        this.capYPosition();
    }

    public int getScroll() {
        return (int)this.yo;
    }

    public boolean isMouseInList(double param0, double param1) {
        return param1 >= (double)this.y0 && param1 <= (double)this.y1 && param0 >= (double)this.x0 && param0 <= (double)this.x1;
    }

    public int getScrollBottom() {
        return (int)this.yo - this.height - this.headerHeight;
    }

    public void scroll(int param0) {
        this.yo += (double)param0;
        this.capYPosition();
        this.yDrag = -2;
    }

    @Override
    public void render(int param0, int param1, float param2) {
        if (this.visible) {
            this.renderBackground();
            int var0 = this.getScrollbarPosition();
            int var1 = var0 + 6;
            this.capYPosition();
            RenderSystem.disableFog();
            Tesselator var2 = Tesselator.getInstance();
            BufferBuilder var3 = var2.getBuilder();
            this.minecraft.getTextureManager().bind(GuiComponent.BACKGROUND_LOCATION);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            float var4 = 32.0F;
            var3.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
            var3.vertex((double)this.x0, (double)this.y1, 0.0)
                .uv((float)this.x0 / 32.0F, (float)(this.y1 + (int)this.yo) / 32.0F)
                .color(32, 32, 32, 255)
                .endVertex();
            var3.vertex((double)this.x1, (double)this.y1, 0.0)
                .uv((float)this.x1 / 32.0F, (float)(this.y1 + (int)this.yo) / 32.0F)
                .color(32, 32, 32, 255)
                .endVertex();
            var3.vertex((double)this.x1, (double)this.y0, 0.0)
                .uv((float)this.x1 / 32.0F, (float)(this.y0 + (int)this.yo) / 32.0F)
                .color(32, 32, 32, 255)
                .endVertex();
            var3.vertex((double)this.x0, (double)this.y0, 0.0)
                .uv((float)this.x0 / 32.0F, (float)(this.y0 + (int)this.yo) / 32.0F)
                .color(32, 32, 32, 255)
                .endVertex();
            var2.end();
            int var5 = this.x0 + this.width / 2 - this.getRowWidth() / 2 + 2;
            int var6 = this.y0 + 4 - (int)this.yo;
            if (this.renderHeader) {
                this.renderHeader(var5, var6, var2);
            }

            this.renderList(var5, var6, param0, param1, param2);
            RenderSystem.disableDepthTest();
            this.renderHoleBackground(0, this.y0, 255, 255);
            this.renderHoleBackground(this.y1, this.height, 255, 255);
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ZERO,
                GlStateManager.DestFactor.ONE
            );
            RenderSystem.disableAlphaTest();
            RenderSystem.shadeModel(7425);
            RenderSystem.disableTexture();
            int var7 = 4;
            var3.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
            var3.vertex((double)this.x0, (double)(this.y0 + 4), 0.0).uv(0.0F, 1.0F).color(0, 0, 0, 0).endVertex();
            var3.vertex((double)this.x1, (double)(this.y0 + 4), 0.0).uv(1.0F, 1.0F).color(0, 0, 0, 0).endVertex();
            var3.vertex((double)this.x1, (double)this.y0, 0.0).uv(1.0F, 0.0F).color(0, 0, 0, 255).endVertex();
            var3.vertex((double)this.x0, (double)this.y0, 0.0).uv(0.0F, 0.0F).color(0, 0, 0, 255).endVertex();
            var2.end();
            var3.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
            var3.vertex((double)this.x0, (double)this.y1, 0.0).uv(0.0F, 1.0F).color(0, 0, 0, 255).endVertex();
            var3.vertex((double)this.x1, (double)this.y1, 0.0).uv(1.0F, 1.0F).color(0, 0, 0, 255).endVertex();
            var3.vertex((double)this.x1, (double)(this.y1 - 4), 0.0).uv(1.0F, 0.0F).color(0, 0, 0, 0).endVertex();
            var3.vertex((double)this.x0, (double)(this.y1 - 4), 0.0).uv(0.0F, 0.0F).color(0, 0, 0, 0).endVertex();
            var2.end();
            int var8 = this.getMaxScroll();
            if (var8 > 0) {
                int var9 = (int)((float)((this.y1 - this.y0) * (this.y1 - this.y0)) / (float)this.getMaxPosition());
                var9 = Mth.clamp(var9, 32, this.y1 - this.y0 - 8);
                int var10 = (int)this.yo * (this.y1 - this.y0 - var9) / var8 + this.y0;
                if (var10 < this.y0) {
                    var10 = this.y0;
                }

                var3.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
                var3.vertex((double)var0, (double)this.y1, 0.0).uv(0.0F, 1.0F).color(0, 0, 0, 255).endVertex();
                var3.vertex((double)var1, (double)this.y1, 0.0).uv(1.0F, 1.0F).color(0, 0, 0, 255).endVertex();
                var3.vertex((double)var1, (double)this.y0, 0.0).uv(1.0F, 0.0F).color(0, 0, 0, 255).endVertex();
                var3.vertex((double)var0, (double)this.y0, 0.0).uv(0.0F, 0.0F).color(0, 0, 0, 255).endVertex();
                var2.end();
                var3.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
                var3.vertex((double)var0, (double)(var10 + var9), 0.0).uv(0.0F, 1.0F).color(128, 128, 128, 255).endVertex();
                var3.vertex((double)var1, (double)(var10 + var9), 0.0).uv(1.0F, 1.0F).color(128, 128, 128, 255).endVertex();
                var3.vertex((double)var1, (double)var10, 0.0).uv(1.0F, 0.0F).color(128, 128, 128, 255).endVertex();
                var3.vertex((double)var0, (double)var10, 0.0).uv(0.0F, 0.0F).color(128, 128, 128, 255).endVertex();
                var2.end();
                var3.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
                var3.vertex((double)var0, (double)(var10 + var9 - 1), 0.0).uv(0.0F, 1.0F).color(192, 192, 192, 255).endVertex();
                var3.vertex((double)(var1 - 1), (double)(var10 + var9 - 1), 0.0).uv(1.0F, 1.0F).color(192, 192, 192, 255).endVertex();
                var3.vertex((double)(var1 - 1), (double)var10, 0.0).uv(1.0F, 0.0F).color(192, 192, 192, 255).endVertex();
                var3.vertex((double)var0, (double)var10, 0.0).uv(0.0F, 0.0F).color(192, 192, 192, 255).endVertex();
                var2.end();
            }

            this.renderDecorations(param0, param1);
            RenderSystem.enableTexture();
            RenderSystem.shadeModel(7424);
            RenderSystem.enableAlphaTest();
            RenderSystem.disableBlend();
        }
    }

    protected void updateScrollingState(double param0, double param1, int param2) {
        this.scrolling = param2 == 0 && param0 >= (double)this.getScrollbarPosition() && param0 < (double)(this.getScrollbarPosition() + 6);
    }

    @Override
    public boolean mouseClicked(double param0, double param1, int param2) {
        this.updateScrollingState(param0, param1, param2);
        if (this.isVisible() && this.isMouseInList(param0, param1)) {
            int var0 = this.getItemAtPosition(param0, param1);
            if (var0 == -1 && param2 == 0) {
                this.clickedHeader(
                    (int)(param0 - (double)(this.x0 + this.width / 2 - this.getRowWidth() / 2)), (int)(param1 - (double)this.y0) + (int)this.yo - 4
                );
                return true;
            } else if (var0 != -1 && this.selectItem(var0, param2, param0, param1)) {
                if (this.children().size() > var0) {
                    this.setFocused(this.children().get(var0));
                }

                this.setDragging(true);
                return true;
            } else {
                return this.scrolling;
            }
        } else {
            return false;
        }
    }

    @Override
    public boolean mouseReleased(double param0, double param1, int param2) {
        if (this.getFocused() != null) {
            this.getFocused().mouseReleased(param0, param1, param2);
        }

        return false;
    }

    @Override
    public boolean mouseDragged(double param0, double param1, int param2, double param3, double param4) {
        if (super.mouseDragged(param0, param1, param2, param3, param4)) {
            return true;
        } else if (this.isVisible() && param2 == 0 && this.scrolling) {
            if (param1 < (double)this.y0) {
                this.yo = 0.0;
            } else if (param1 > (double)this.y1) {
                this.yo = (double)this.getMaxScroll();
            } else {
                double var0 = (double)this.getMaxScroll();
                if (var0 < 1.0) {
                    var0 = 1.0;
                }

                int var1 = (int)((float)((this.y1 - this.y0) * (this.y1 - this.y0)) / (float)this.getMaxPosition());
                var1 = Mth.clamp(var1, 32, this.y1 - this.y0 - 8);
                double var2 = var0 / (double)(this.y1 - this.y0 - var1);
                if (var2 < 1.0) {
                    var2 = 1.0;
                }

                this.yo += param4 * var2;
                this.capYPosition();
            }

            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean mouseScrolled(double param0, double param1, double param2) {
        if (!this.isVisible()) {
            return false;
        } else {
            this.yo -= param2 * (double)this.itemHeight / 2.0;
            return true;
        }
    }

    @Override
    public boolean keyPressed(int param0, int param1, int param2) {
        if (!this.isVisible()) {
            return false;
        } else if (super.keyPressed(param0, param1, param2)) {
            return true;
        } else if (param0 == 264) {
            this.moveSelection(1);
            return true;
        } else if (param0 == 265) {
            this.moveSelection(-1);
            return true;
        } else {
            return false;
        }
    }

    protected void moveSelection(int param0) {
    }

    @Override
    public boolean charTyped(char param0, int param1) {
        return !this.isVisible() ? false : super.charTyped(param0, param1);
    }

    @Override
    public boolean isMouseOver(double param0, double param1) {
        return this.isMouseInList(param0, param1);
    }

    public int getRowWidth() {
        return 220;
    }

    protected void renderList(int param0, int param1, int param2, int param3, float param4) {
        int var0 = this.getItemCount();
        Tesselator var1 = Tesselator.getInstance();
        BufferBuilder var2 = var1.getBuilder();

        for(int var3 = 0; var3 < var0; ++var3) {
            int var4 = param1 + var3 * this.itemHeight + this.headerHeight;
            int var5 = this.itemHeight - 4;
            if (var4 > this.y1 || var4 + var5 < this.y0) {
                this.updateItemPosition(var3, param0, var4, param4);
            }

            if (this.renderSelection && this.isSelectedItem(var3)) {
                int var6 = this.x0 + this.width / 2 - this.getRowWidth() / 2;
                int var7 = this.x0 + this.width / 2 + this.getRowWidth() / 2;
                RenderSystem.disableTexture();
                float var8 = this.isFocused() ? 1.0F : 0.5F;
                RenderSystem.color4f(var8, var8, var8, 1.0F);
                var2.begin(7, DefaultVertexFormat.POSITION);
                var2.vertex((double)var6, (double)(var4 + var5 + 2), 0.0).endVertex();
                var2.vertex((double)var7, (double)(var4 + var5 + 2), 0.0).endVertex();
                var2.vertex((double)var7, (double)(var4 - 2), 0.0).endVertex();
                var2.vertex((double)var6, (double)(var4 - 2), 0.0).endVertex();
                var1.end();
                RenderSystem.color4f(0.0F, 0.0F, 0.0F, 1.0F);
                var2.begin(7, DefaultVertexFormat.POSITION);
                var2.vertex((double)(var6 + 1), (double)(var4 + var5 + 1), 0.0).endVertex();
                var2.vertex((double)(var7 - 1), (double)(var4 + var5 + 1), 0.0).endVertex();
                var2.vertex((double)(var7 - 1), (double)(var4 - 1), 0.0).endVertex();
                var2.vertex((double)(var6 + 1), (double)(var4 - 1), 0.0).endVertex();
                var1.end();
                RenderSystem.enableTexture();
            }

            this.renderItem(var3, param0, var4, var5, param2, param3, param4);
        }

    }

    protected boolean isFocused() {
        return false;
    }

    protected int getScrollbarPosition() {
        return this.width / 2 + 124;
    }

    protected void renderHoleBackground(int param0, int param1, int param2, int param3) {
        Tesselator var0 = Tesselator.getInstance();
        BufferBuilder var1 = var0.getBuilder();
        this.minecraft.getTextureManager().bind(GuiComponent.BACKGROUND_LOCATION);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        float var2 = 32.0F;
        var1.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
        var1.vertex((double)this.x0, (double)param1, 0.0).uv(0.0F, (float)param1 / 32.0F).color(64, 64, 64, param3).endVertex();
        var1.vertex((double)(this.x0 + this.width), (double)param1, 0.0)
            .uv((float)this.width / 32.0F, (float)param1 / 32.0F)
            .color(64, 64, 64, param3)
            .endVertex();
        var1.vertex((double)(this.x0 + this.width), (double)param0, 0.0)
            .uv((float)this.width / 32.0F, (float)param0 / 32.0F)
            .color(64, 64, 64, param2)
            .endVertex();
        var1.vertex((double)this.x0, (double)param0, 0.0).uv(0.0F, (float)param0 / 32.0F).color(64, 64, 64, param2).endVertex();
        var0.end();
    }

    public void setLeftPos(int param0) {
        this.x0 = param0;
        this.x1 = param0 + this.width;
    }

    public int getItemHeight() {
        return this.itemHeight;
    }
}
