package net.minecraft.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import java.util.function.BiConsumer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class GuiComponent {
    public static final ResourceLocation BACKGROUND_LOCATION = new ResourceLocation("textures/gui/options_background.png");
    public static final ResourceLocation STATS_ICON_LOCATION = new ResourceLocation("textures/gui/container/stats_icons.png");
    public static final ResourceLocation GUI_ICONS_LOCATION = new ResourceLocation("textures/gui/icons.png");
    private int blitOffset;

    protected void hLine(PoseStack param0, int param1, int param2, int param3, int param4) {
        if (param2 < param1) {
            int var0 = param1;
            param1 = param2;
            param2 = var0;
        }

        fill(param0, param1, param3, param2 + 1, param3 + 1, param4);
    }

    protected void vLine(PoseStack param0, int param1, int param2, int param3, int param4) {
        if (param3 < param2) {
            int var0 = param2;
            param2 = param3;
            param3 = var0;
        }

        fill(param0, param1, param2 + 1, param1 + 1, param3, param4);
    }

    public static void fill(PoseStack param0, int param1, int param2, int param3, int param4, int param5) {
        innerFill(param0.last().pose(), param1, param2, param3, param4, param5);
    }

    private static void innerFill(Matrix4f param0, int param1, int param2, int param3, int param4, int param5) {
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
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        var6.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        var6.vertex(param0, (float)param1, (float)param4, 0.0F).color(var3, var4, var5, var2).endVertex();
        var6.vertex(param0, (float)param3, (float)param4, 0.0F).color(var3, var4, var5, var2).endVertex();
        var6.vertex(param0, (float)param3, (float)param2, 0.0F).color(var3, var4, var5, var2).endVertex();
        var6.vertex(param0, (float)param1, (float)param2, 0.0F).color(var3, var4, var5, var2).endVertex();
        var6.end();
        BufferUploader.end(var6);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    protected void fillGradient(PoseStack param0, int param1, int param2, int param3, int param4, int param5, int param6) {
        fillGradient(param0, param1, param2, param3, param4, param5, param6, this.blitOffset);
    }

    protected static void fillGradient(PoseStack param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7) {
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator var0 = Tesselator.getInstance();
        BufferBuilder var1 = var0.getBuilder();
        var1.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        fillGradient(param0.last().pose(), var1, param1, param2, param3, param4, param7, param5, param6);
        var0.end();
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
    }

    protected static void fillGradient(
        Matrix4f param0, BufferBuilder param1, int param2, int param3, int param4, int param5, int param6, int param7, int param8
    ) {
        float var0 = (float)(param7 >> 24 & 0xFF) / 255.0F;
        float var1 = (float)(param7 >> 16 & 0xFF) / 255.0F;
        float var2 = (float)(param7 >> 8 & 0xFF) / 255.0F;
        float var3 = (float)(param7 & 0xFF) / 255.0F;
        float var4 = (float)(param8 >> 24 & 0xFF) / 255.0F;
        float var5 = (float)(param8 >> 16 & 0xFF) / 255.0F;
        float var6 = (float)(param8 >> 8 & 0xFF) / 255.0F;
        float var7 = (float)(param8 & 0xFF) / 255.0F;
        param1.vertex(param0, (float)param4, (float)param3, (float)param6).color(var1, var2, var3, var0).endVertex();
        param1.vertex(param0, (float)param2, (float)param3, (float)param6).color(var1, var2, var3, var0).endVertex();
        param1.vertex(param0, (float)param2, (float)param5, (float)param6).color(var5, var6, var7, var4).endVertex();
        param1.vertex(param0, (float)param4, (float)param5, (float)param6).color(var5, var6, var7, var4).endVertex();
    }

    public static void drawCenteredString(PoseStack param0, Font param1, String param2, int param3, int param4, int param5) {
        param1.drawShadow(param0, param2, (float)(param3 - param1.width(param2) / 2), (float)param4, param5);
    }

    public static void drawCenteredString(PoseStack param0, Font param1, Component param2, int param3, int param4, int param5) {
        FormattedCharSequence var0 = param2.getVisualOrderText();
        param1.drawShadow(param0, var0, (float)(param3 - param1.width(var0) / 2), (float)param4, param5);
    }

    public static void drawCenteredString(PoseStack param0, Font param1, FormattedCharSequence param2, int param3, int param4, int param5) {
        param1.drawShadow(param0, param2, (float)(param3 - param1.width(param2) / 2), (float)param4, param5);
    }

    public static void drawString(PoseStack param0, Font param1, String param2, int param3, int param4, int param5) {
        param1.drawShadow(param0, param2, (float)param3, (float)param4, param5);
    }

    public static void drawString(PoseStack param0, Font param1, FormattedCharSequence param2, int param3, int param4, int param5) {
        param1.drawShadow(param0, param2, (float)param3, (float)param4, param5);
    }

    public static void drawString(PoseStack param0, Font param1, Component param2, int param3, int param4, int param5) {
        param1.drawShadow(param0, param2, (float)param3, (float)param4, param5);
    }

    public void blitOutlineBlack(int param0, int param1, BiConsumer<Integer, Integer> param2) {
        RenderSystem.blendFuncSeparate(
            GlStateManager.SourceFactor.ZERO,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
        );
        param2.accept(param0 + 1, param1);
        param2.accept(param0 - 1, param1);
        param2.accept(param0, param1 + 1);
        param2.accept(param0, param1 - 1);
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        param2.accept(param0, param1);
    }

    public static void blit(PoseStack param0, int param1, int param2, int param3, int param4, int param5, TextureAtlasSprite param6) {
        innerBlit(
            param0.last().pose(), param1, param1 + param4, param2, param2 + param5, param3, param6.getU0(), param6.getU1(), param6.getV0(), param6.getV1()
        );
    }

    public void blit(PoseStack param0, int param1, int param2, int param3, int param4, int param5, int param6) {
        blit(param0, param1, param2, this.blitOffset, (float)param3, (float)param4, param5, param6, 256, 256);
    }

    public static void blit(PoseStack param0, int param1, int param2, int param3, float param4, float param5, int param6, int param7, int param8, int param9) {
        innerBlit(param0, param1, param1 + param6, param2, param2 + param7, param3, param6, param7, param4, param5, param8, param9);
    }

    public static void blit(
        PoseStack param0, int param1, int param2, int param3, int param4, float param5, float param6, int param7, int param8, int param9, int param10
    ) {
        innerBlit(param0, param1, param1 + param3, param2, param2 + param4, 0, param7, param8, param5, param6, param9, param10);
    }

    public static void blit(PoseStack param0, int param1, int param2, float param3, float param4, int param5, int param6, int param7, int param8) {
        blit(param0, param1, param2, param5, param6, param3, param4, param5, param6, param7, param8);
    }

    private static void innerBlit(
        PoseStack param0,
        int param1,
        int param2,
        int param3,
        int param4,
        int param5,
        int param6,
        int param7,
        float param8,
        float param9,
        int param10,
        int param11
    ) {
        innerBlit(
            param0.last().pose(),
            param1,
            param2,
            param3,
            param4,
            param5,
            (param8 + 0.0F) / (float)param10,
            (param8 + (float)param6) / (float)param10,
            (param9 + 0.0F) / (float)param11,
            (param9 + (float)param7) / (float)param11
        );
    }

    private static void innerBlit(
        Matrix4f param0, int param1, int param2, int param3, int param4, int param5, float param6, float param7, float param8, float param9
    ) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        BufferBuilder var0 = Tesselator.getInstance().getBuilder();
        var0.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        var0.vertex(param0, (float)param1, (float)param4, (float)param5).uv(param6, param9).endVertex();
        var0.vertex(param0, (float)param2, (float)param4, (float)param5).uv(param7, param9).endVertex();
        var0.vertex(param0, (float)param2, (float)param3, (float)param5).uv(param7, param8).endVertex();
        var0.vertex(param0, (float)param1, (float)param3, (float)param5).uv(param6, param8).endVertex();
        var0.end();
        BufferUploader.end(var0);
    }

    public int getBlitOffset() {
        return this.blitOffset;
    }

    public void setBlitOffset(int param0) {
        this.blitOffset = param0;
    }
}
