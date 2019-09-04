package net.minecraft.realms;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ScrolledSelectionList;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsSimpleScrolledSelectionListProxy extends ScrolledSelectionList {
    private final RealmsSimpleScrolledSelectionList realmsSimpleScrolledSelectionList;

    public RealmsSimpleScrolledSelectionListProxy(RealmsSimpleScrolledSelectionList param0, int param1, int param2, int param3, int param4, int param5) {
        super(Minecraft.getInstance(), param1, param2, param3, param4, param5);
        this.realmsSimpleScrolledSelectionList = param0;
    }

    @Override
    public int getItemCount() {
        return this.realmsSimpleScrolledSelectionList.getItemCount();
    }

    @Override
    public boolean selectItem(int param0, int param1, double param2, double param3) {
        return this.realmsSimpleScrolledSelectionList.selectItem(param0, param1, param2, param3);
    }

    @Override
    public boolean isSelectedItem(int param0) {
        return this.realmsSimpleScrolledSelectionList.isSelectedItem(param0);
    }

    @Override
    public void renderBackground() {
        this.realmsSimpleScrolledSelectionList.renderBackground();
    }

    @Override
    public void renderItem(int param0, int param1, int param2, int param3, int param4, int param5, float param6) {
        this.realmsSimpleScrolledSelectionList.renderItem(param0, param1, param2, param3, param4, param5);
    }

    public int getWidth() {
        return this.width;
    }

    @Override
    public int getMaxPosition() {
        return this.realmsSimpleScrolledSelectionList.getMaxPosition();
    }

    @Override
    public int getScrollbarPosition() {
        return this.realmsSimpleScrolledSelectionList.getScrollbarPosition();
    }

    @Override
    public void render(int param0, int param1, float param2) {
        if (this.visible) {
            this.renderBackground();
            int var0 = this.getScrollbarPosition();
            int var1 = var0 + 6;
            this.capYPosition();
            RenderSystem.disableLighting();
            RenderSystem.disableFog();
            Tesselator var2 = Tesselator.getInstance();
            BufferBuilder var3 = var2.getBuilder();
            int var4 = this.x0 + this.width / 2 - this.getRowWidth() / 2 + 2;
            int var5 = this.y0 + 4 - (int)this.yo;
            if (this.renderHeader) {
                this.renderHeader(var4, var5, var2);
            }

            this.renderList(var4, var5, param0, param1, param2);
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
            int var6 = this.getMaxScroll();
            if (var6 > 0) {
                int var7 = (this.y1 - this.y0) * (this.y1 - this.y0) / this.getMaxPosition();
                var7 = Mth.clamp(var7, 32, this.y1 - this.y0 - 8);
                int var8 = (int)this.yo * (this.y1 - this.y0 - var7) / var6 + this.y0;
                if (var8 < this.y0) {
                    var8 = this.y0;
                }

                var3.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
                var3.vertex((double)var0, (double)this.y1, 0.0).uv(0.0, 1.0).color(0, 0, 0, 255).endVertex();
                var3.vertex((double)var1, (double)this.y1, 0.0).uv(1.0, 1.0).color(0, 0, 0, 255).endVertex();
                var3.vertex((double)var1, (double)this.y0, 0.0).uv(1.0, 0.0).color(0, 0, 0, 255).endVertex();
                var3.vertex((double)var0, (double)this.y0, 0.0).uv(0.0, 0.0).color(0, 0, 0, 255).endVertex();
                var2.end();
                var3.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
                var3.vertex((double)var0, (double)(var8 + var7), 0.0).uv(0.0, 1.0).color(128, 128, 128, 255).endVertex();
                var3.vertex((double)var1, (double)(var8 + var7), 0.0).uv(1.0, 1.0).color(128, 128, 128, 255).endVertex();
                var3.vertex((double)var1, (double)var8, 0.0).uv(1.0, 0.0).color(128, 128, 128, 255).endVertex();
                var3.vertex((double)var0, (double)var8, 0.0).uv(0.0, 0.0).color(128, 128, 128, 255).endVertex();
                var2.end();
                var3.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
                var3.vertex((double)var0, (double)(var8 + var7 - 1), 0.0).uv(0.0, 1.0).color(192, 192, 192, 255).endVertex();
                var3.vertex((double)(var1 - 1), (double)(var8 + var7 - 1), 0.0).uv(1.0, 1.0).color(192, 192, 192, 255).endVertex();
                var3.vertex((double)(var1 - 1), (double)var8, 0.0).uv(1.0, 0.0).color(192, 192, 192, 255).endVertex();
                var3.vertex((double)var0, (double)var8, 0.0).uv(0.0, 0.0).color(192, 192, 192, 255).endVertex();
                var2.end();
            }

            this.renderDecorations(param0, param1);
            RenderSystem.enableTexture();
            RenderSystem.shadeModel(7424);
            RenderSystem.enableAlphaTest();
            RenderSystem.disableBlend();
        }
    }

    @Override
    public boolean mouseScrolled(double param0, double param1, double param2) {
        return this.realmsSimpleScrolledSelectionList.mouseScrolled(param0, param1, param2) ? true : super.mouseScrolled(param0, param1, param2);
    }

    @Override
    public boolean mouseClicked(double param0, double param1, int param2) {
        return this.realmsSimpleScrolledSelectionList.mouseClicked(param0, param1, param2) ? true : super.mouseClicked(param0, param1, param2);
    }

    @Override
    public boolean mouseReleased(double param0, double param1, int param2) {
        return this.realmsSimpleScrolledSelectionList.mouseReleased(param0, param1, param2);
    }

    @Override
    public boolean mouseDragged(double param0, double param1, int param2, double param3, double param4) {
        return this.realmsSimpleScrolledSelectionList.mouseDragged(param0, param1, param2, param3, param4);
    }
}
