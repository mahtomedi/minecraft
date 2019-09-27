package net.minecraft.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Matrix4f;
import com.mojang.math.Transformation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class GuiComponent {
    public static final ResourceLocation BACKGROUND_LOCATION = new ResourceLocation("textures/gui/options_background.png");
    public static final ResourceLocation STATS_ICON_LOCATION = new ResourceLocation("textures/gui/container/stats_icons.png");
    public static final ResourceLocation GUI_ICONS_LOCATION = new ResourceLocation("textures/gui/icons.png");
    private int blitOffset;

    protected void hLine(int param0, int param1, int param2, int param3) {
        if (param1 < param0) {
            int var0 = param0;
            param0 = param1;
            param1 = var0;
        }

        fill(param0, param2, param1 + 1, param2 + 1, param3);
    }

    protected void vLine(int param0, int param1, int param2, int param3) {
        if (param2 < param1) {
            int var0 = param1;
            param1 = param2;
            param2 = var0;
        }

        fill(param0, param1 + 1, param0 + 1, param2, param3);
    }

    public static void fill(int param0, int param1, int param2, int param3, int param4) {
        fill(Transformation.identity().getMatrix(), param0, param1, param2, param3, param4);
    }

    public static void fill(Matrix4f param0, int param1, int param2, int param3, int param4, int param5) {
        if (param1 < param3) {
            int var0 = param1;
            param1 = param3;
            param3 = var0;
        }

        if (param2 < param4) {
            int var1 = param2;
            param2 = param4;
            param4 = var1;
        }

        float var2 = (float)(param5 >> 24 & 0xFF) / 255.0F;
        float var3 = (float)(param5 >> 16 & 0xFF) / 255.0F;
        float var4 = (float)(param5 >> 8 & 0xFF) / 255.0F;
        float var5 = (float)(param5 & 0xFF) / 255.0F;
        BufferBuilder var6 = Tesselator.getInstance().getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        var6.begin(7, DefaultVertexFormat.POSITION_COLOR);
        var6.vertex(param0, (float)param1, (float)param4, 0.0F).color(var3, var4, var5, var2).endVertex();
        var6.vertex(param0, (float)param3, (float)param4, 0.0F).color(var3, var4, var5, var2).endVertex();
        var6.vertex(param0, (float)param3, (float)param2, 0.0F).color(var3, var4, var5, var2).endVertex();
        var6.vertex(param0, (float)param1, (float)param2, 0.0F).color(var3, var4, var5, var2).endVertex();
        var6.end();
        BufferUploader.end(var6);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    protected void fillGradient(int param0, int param1, int param2, int param3, int param4, int param5) {
        float var0 = (float)(param4 >> 24 & 0xFF) / 255.0F;
        float var1 = (float)(param4 >> 16 & 0xFF) / 255.0F;
        float var2 = (float)(param4 >> 8 & 0xFF) / 255.0F;
        float var3 = (float)(param4 & 0xFF) / 255.0F;
        float var4 = (float)(param5 >> 24 & 0xFF) / 255.0F;
        float var5 = (float)(param5 >> 16 & 0xFF) / 255.0F;
        float var6 = (float)(param5 >> 8 & 0xFF) / 255.0F;
        float var7 = (float)(param5 & 0xFF) / 255.0F;
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.disableAlphaTest();
        RenderSystem.defaultBlendFunc();
        RenderSystem.shadeModel(7425);
        Tesselator var8 = Tesselator.getInstance();
        BufferBuilder var9 = var8.getBuilder();
        var9.begin(7, DefaultVertexFormat.POSITION_COLOR);
        var9.vertex((double)param2, (double)param1, (double)this.blitOffset).color(var1, var2, var3, var0).endVertex();
        var9.vertex((double)param0, (double)param1, (double)this.blitOffset).color(var1, var2, var3, var0).endVertex();
        var9.vertex((double)param0, (double)param3, (double)this.blitOffset).color(var5, var6, var7, var4).endVertex();
        var9.vertex((double)param2, (double)param3, (double)this.blitOffset).color(var5, var6, var7, var4).endVertex();
        var8.end();
        RenderSystem.shadeModel(7424);
        RenderSystem.disableBlend();
        RenderSystem.enableAlphaTest();
        RenderSystem.enableTexture();
    }

    public void drawCenteredString(Font param0, String param1, int param2, int param3, int param4) {
        param0.drawShadow(param1, (float)(param2 - param0.width(param1) / 2), (float)param3, param4);
    }

    public void drawRightAlignedString(Font param0, String param1, int param2, int param3, int param4) {
        param0.drawShadow(param1, (float)(param2 - param0.width(param1)), (float)param3, param4);
    }

    public void drawString(Font param0, String param1, int param2, int param3, int param4) {
        param0.drawShadow(param1, (float)param2, (float)param3, param4);
    }

    public static void blit(int param0, int param1, int param2, int param3, int param4, TextureAtlasSprite param5) {
        innerBlit(param0, param0 + param3, param1, param1 + param4, param2, param5.getU0(), param5.getU1(), param5.getV0(), param5.getV1());
    }

    public void blit(int param0, int param1, int param2, int param3, int param4, int param5) {
        blit(param0, param1, this.blitOffset, (float)param2, (float)param3, param4, param5, 256, 256);
    }

    public static void blit(int param0, int param1, int param2, float param3, float param4, int param5, int param6, int param7, int param8) {
        innerBlit(param0, param0 + param5, param1, param1 + param6, param2, param5, param6, param3, param4, param8, param7);
    }

    public static void blit(int param0, int param1, int param2, int param3, float param4, float param5, int param6, int param7, int param8, int param9) {
        innerBlit(param0, param0 + param2, param1, param1 + param3, 0, param6, param7, param4, param5, param8, param9);
    }

    public static void blit(int param0, int param1, float param2, float param3, int param4, int param5, int param6, int param7) {
        blit(param0, param1, param4, param5, param2, param3, param4, param5, param6, param7);
    }

    private static void innerBlit(
        int param0, int param1, int param2, int param3, int param4, int param5, int param6, float param7, float param8, int param9, int param10
    ) {
        innerBlit(
            param0,
            param1,
            param2,
            param3,
            param4,
            (param7 + 0.0F) / (float)param9,
            (param7 + (float)param5) / (float)param9,
            (param8 + 0.0F) / (float)param10,
            (param8 + (float)param6) / (float)param10
        );
    }

    protected static void innerBlit(int param0, int param1, int param2, int param3, int param4, float param5, float param6, float param7, float param8) {
        Tesselator var0 = Tesselator.getInstance();
        BufferBuilder var1 = var0.getBuilder();
        var1.begin(7, DefaultVertexFormat.POSITION_TEX);
        var1.vertex((double)param0, (double)param3, (double)param4).uv(param5, param8).endVertex();
        var1.vertex((double)param1, (double)param3, (double)param4).uv(param6, param8).endVertex();
        var1.vertex((double)param1, (double)param2, (double)param4).uv(param6, param7).endVertex();
        var1.vertex((double)param0, (double)param2, (double)param4).uv(param5, param7).endVertex();
        var0.end();
    }

    public int getBlitOffset() {
        return this.blitOffset;
    }

    public void setBlitOffset(int param0) {
        this.blitOffset = param0;
    }
}
