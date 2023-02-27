package net.minecraft.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Divisor;
import it.unimi.dsi.fastutil.ints.IntIterator;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public abstract class GuiComponent {
    public static final ResourceLocation BACKGROUND_LOCATION = new ResourceLocation("textures/gui/options_background.png");
    public static final ResourceLocation STATS_ICON_LOCATION = new ResourceLocation("textures/gui/container/stats_icons.png");
    public static final ResourceLocation GUI_ICONS_LOCATION = new ResourceLocation("textures/gui/icons.png");
    public static final ResourceLocation LIGHT_DIRT_BACKGROUND = new ResourceLocation("textures/gui/light_dirt_background.png");
    private static final GuiComponent.ScissorStack SCISSOR_STACK = new GuiComponent.ScissorStack();

    protected static void hLine(PoseStack param0, int param1, int param2, int param3, int param4) {
        if (param2 < param1) {
            int var0 = param1;
            param1 = param2;
            param2 = var0;
        }

        fill(param0, param1, param3, param2 + 1, param3 + 1, param4);
    }

    protected static void vLine(PoseStack param0, int param1, int param2, int param3, int param4) {
        if (param3 < param2) {
            int var0 = param2;
            param2 = param3;
            param3 = var0;
        }

        fill(param0, param1, param2 + 1, param1 + 1, param3, param4);
    }

    public static void enableScissor(int param0, int param1, int param2, int param3) {
        applyScissor(SCISSOR_STACK.push(new ScreenRectangle(param0, param1, param2 - param0, param3 - param1)));
    }

    public static void disableScissor() {
        applyScissor(SCISSOR_STACK.pop());
    }

    private static void applyScissor(@Nullable ScreenRectangle param0) {
        if (param0 != null) {
            Window var0 = Minecraft.getInstance().getWindow();
            int var1 = var0.getHeight();
            double var2 = var0.getGuiScale();
            double var3 = (double)param0.left() * var2;
            double var4 = (double)var1 - (double)param0.bottom() * var2;
            double var5 = (double)param0.width() * var2;
            double var6 = (double)param0.height() * var2;
            RenderSystem.enableScissor((int)var3, (int)var4, Math.max(0, (int)var5), Math.max(0, (int)var6));
        } else {
            RenderSystem.disableScissor();
        }

    }

    public static void fill(PoseStack param0, int param1, int param2, int param3, int param4, int param5) {
        fill(param0, param1, param2, param3, param4, 0, param5);
    }

    public static void fill(PoseStack param0, int param1, int param2, int param3, int param4, int param5, int param6) {
        Matrix4f var0 = param0.last().pose();
        if (param1 < param3) {
            int var1 = param1;
            param1 = param3;
            param3 = var1;
        }

        if (param2 < param4) {
            int var2 = param2;
            param2 = param4;
            param4 = var2;
        }

        float var3 = (float)FastColor.ARGB32.alpha(param6) / 255.0F;
        float var4 = (float)FastColor.ARGB32.red(param6) / 255.0F;
        float var5 = (float)FastColor.ARGB32.green(param6) / 255.0F;
        float var6 = (float)FastColor.ARGB32.blue(param6) / 255.0F;
        BufferBuilder var7 = Tesselator.getInstance().getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        var7.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        var7.vertex(var0, (float)param1, (float)param2, (float)param5).color(var4, var5, var6, var3).endVertex();
        var7.vertex(var0, (float)param1, (float)param4, (float)param5).color(var4, var5, var6, var3).endVertex();
        var7.vertex(var0, (float)param3, (float)param4, (float)param5).color(var4, var5, var6, var3).endVertex();
        var7.vertex(var0, (float)param3, (float)param2, (float)param5).color(var4, var5, var6, var3).endVertex();
        BufferUploader.drawWithShader(var7.end());
        RenderSystem.disableBlend();
    }

    protected static void fillGradient(PoseStack param0, int param1, int param2, int param3, int param4, int param5, int param6) {
        fillGradient(param0, param1, param2, param3, param4, param5, param6, 0);
    }

    protected static void fillGradient(PoseStack param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7) {
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator var0 = Tesselator.getInstance();
        BufferBuilder var1 = var0.getBuilder();
        var1.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        fillGradient(param0.last().pose(), var1, param1, param2, param3, param4, param7, param5, param6);
        var0.end();
        RenderSystem.disableBlend();
    }

    protected static void fillGradient(
        Matrix4f param0, BufferBuilder param1, int param2, int param3, int param4, int param5, int param6, int param7, int param8
    ) {
        float var0 = (float)FastColor.ARGB32.alpha(param7) / 255.0F;
        float var1 = (float)FastColor.ARGB32.red(param7) / 255.0F;
        float var2 = (float)FastColor.ARGB32.green(param7) / 255.0F;
        float var3 = (float)FastColor.ARGB32.blue(param7) / 255.0F;
        float var4 = (float)FastColor.ARGB32.alpha(param8) / 255.0F;
        float var5 = (float)FastColor.ARGB32.red(param8) / 255.0F;
        float var6 = (float)FastColor.ARGB32.green(param8) / 255.0F;
        float var7 = (float)FastColor.ARGB32.blue(param8) / 255.0F;
        param1.vertex(param0, (float)param2, (float)param3, (float)param6).color(var1, var2, var3, var0).endVertex();
        param1.vertex(param0, (float)param2, (float)param5, (float)param6).color(var5, var6, var7, var4).endVertex();
        param1.vertex(param0, (float)param4, (float)param5, (float)param6).color(var5, var6, var7, var4).endVertex();
        param1.vertex(param0, (float)param4, (float)param3, (float)param6).color(var1, var2, var3, var0).endVertex();
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

    public static void blitOutlineBlack(int param0, int param1, BiConsumer<Integer, Integer> param2) {
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
        RenderSystem.defaultBlendFunc();
        param2.accept(param0, param1);
    }

    public static void blit(PoseStack param0, int param1, int param2, int param3, int param4, int param5, TextureAtlasSprite param6) {
        innerBlit(
            param0.last().pose(), param1, param1 + param4, param2, param2 + param5, param3, param6.getU0(), param6.getU1(), param6.getV0(), param6.getV1()
        );
    }

    public static void blit(
        PoseStack param0,
        int param1,
        int param2,
        int param3,
        int param4,
        int param5,
        TextureAtlasSprite param6,
        float param7,
        float param8,
        float param9,
        float param10
    ) {
        innerBlit(
            param0.last().pose(),
            param1,
            param1 + param4,
            param2,
            param2 + param5,
            param3,
            param6.getU0(),
            param6.getU1(),
            param6.getV0(),
            param6.getV1(),
            param7,
            param8,
            param9,
            param10
        );
    }

    public static void renderOutline(PoseStack param0, int param1, int param2, int param3, int param4, int param5) {
        fill(param0, param1, param2, param1 + param3, param2 + 1, param5);
        fill(param0, param1, param2 + param4 - 1, param1 + param3, param2 + param4, param5);
        fill(param0, param1, param2 + 1, param1 + 1, param2 + param4 - 1, param5);
        fill(param0, param1 + param3 - 1, param2 + 1, param1 + param3, param2 + param4 - 1, param5);
    }

    public static void blit(PoseStack param0, int param1, int param2, int param3, int param4, int param5, int param6) {
        blit(param0, param1, param2, 0, (float)param3, (float)param4, param5, param6, 256, 256);
    }

    public static void blit(PoseStack param0, int param1, int param2, int param3, float param4, float param5, int param6, int param7, int param8, int param9) {
        blit(param0, param1, param1 + param6, param2, param2 + param7, param3, param6, param7, param4, param5, param8, param9);
    }

    public static void blit(
        PoseStack param0, int param1, int param2, int param3, int param4, float param5, float param6, int param7, int param8, int param9, int param10
    ) {
        blit(param0, param1, param1 + param3, param2, param2 + param4, 0, param7, param8, param5, param6, param9, param10);
    }

    public static void blit(PoseStack param0, int param1, int param2, float param3, float param4, int param5, int param6, int param7, int param8) {
        blit(param0, param1, param2, param5, param6, param3, param4, param5, param6, param7, param8);
    }

    private static void blit(
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
        var0.vertex(param0, (float)param1, (float)param3, (float)param5).uv(param6, param8).endVertex();
        var0.vertex(param0, (float)param1, (float)param4, (float)param5).uv(param6, param9).endVertex();
        var0.vertex(param0, (float)param2, (float)param4, (float)param5).uv(param7, param9).endVertex();
        var0.vertex(param0, (float)param2, (float)param3, (float)param5).uv(param7, param8).endVertex();
        BufferUploader.drawWithShader(var0.end());
    }

    private static void innerBlit(
        Matrix4f param0,
        int param1,
        int param2,
        int param3,
        int param4,
        int param5,
        float param6,
        float param7,
        float param8,
        float param9,
        float param10,
        float param11,
        float param12,
        float param13
    ) {
        RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
        RenderSystem.enableBlend();
        BufferBuilder var0 = Tesselator.getInstance().getBuilder();
        var0.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
        var0.vertex(param0, (float)param1, (float)param3, (float)param5).color(param10, param11, param12, param13).uv(param6, param8).endVertex();
        var0.vertex(param0, (float)param1, (float)param4, (float)param5).color(param10, param11, param12, param13).uv(param6, param9).endVertex();
        var0.vertex(param0, (float)param2, (float)param4, (float)param5).color(param10, param11, param12, param13).uv(param7, param9).endVertex();
        var0.vertex(param0, (float)param2, (float)param3, (float)param5).color(param10, param11, param12, param13).uv(param7, param8).endVertex();
        BufferUploader.drawWithShader(var0.end());
        RenderSystem.disableBlend();
    }

    public static void blitNineSliced(
        PoseStack param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, int param8, int param9
    ) {
        blitNineSliced(param0, param1, param2, param3, param4, param5, param5, param5, param5, param6, param7, param8, param9);
    }

    public static void blitNineSliced(
        PoseStack param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, int param8, int param9, int param10
    ) {
        blitNineSliced(param0, param1, param2, param3, param4, param5, param6, param5, param6, param7, param8, param9, param10);
    }

    public static void blitNineSliced(
        PoseStack param0,
        int param1,
        int param2,
        int param3,
        int param4,
        int param5,
        int param6,
        int param7,
        int param8,
        int param9,
        int param10,
        int param11,
        int param12
    ) {
        param5 = Math.min(param5, param3 / 2);
        param7 = Math.min(param7, param3 / 2);
        param6 = Math.min(param6, param4 / 2);
        param8 = Math.min(param8, param4 / 2);
        if (param3 == param9 && param4 == param10) {
            blit(param0, param1, param2, param11, param12, param3, param4);
        } else if (param4 == param10) {
            blit(param0, param1, param2, param11, param12, param5, param4);
            blitRepeating(param0, param1 + param5, param2, param3 - param7 - param5, param4, param11 + param5, param12, param9 - param7 - param5, param10);
            blit(param0, param1 + param3 - param7, param2, param11 + param9 - param7, param12, param7, param4);
        } else if (param3 == param9) {
            blit(param0, param1, param2, param11, param12, param3, param6);
            blitRepeating(param0, param1, param2 + param6, param3, param4 - param8 - param6, param11, param12 + param6, param9, param10 - param8 - param6);
            blit(param0, param1, param2 + param4 - param8, param11, param12 + param10 - param8, param3, param8);
        } else {
            blit(param0, param1, param2, param11, param12, param5, param6);
            blitRepeating(param0, param1 + param5, param2, param3 - param7 - param5, param6, param11 + param5, param12, param9 - param7 - param5, param6);
            blit(param0, param1 + param3 - param7, param2, param11 + param9 - param7, param12, param7, param6);
            blit(param0, param1, param2 + param4 - param8, param11, param12 + param10 - param8, param5, param8);
            blitRepeating(
                param0,
                param1 + param5,
                param2 + param4 - param8,
                param3 - param7 - param5,
                param8,
                param11 + param5,
                param12 + param10 - param8,
                param9 - param7 - param5,
                param8
            );
            blit(param0, param1 + param3 - param7, param2 + param4 - param8, param11 + param9 - param7, param12 + param10 - param8, param7, param8);
            blitRepeating(param0, param1, param2 + param6, param5, param4 - param8 - param6, param11, param12 + param6, param5, param10 - param8 - param6);
            blitRepeating(
                param0,
                param1 + param5,
                param2 + param6,
                param3 - param7 - param5,
                param4 - param8 - param6,
                param11 + param5,
                param12 + param6,
                param9 - param7 - param5,
                param10 - param8 - param6
            );
            blitRepeating(
                param0,
                param1 + param3 - param7,
                param2 + param6,
                param5,
                param4 - param8 - param6,
                param11 + param9 - param7,
                param12 + param6,
                param7,
                param10 - param8 - param6
            );
        }
    }

    public static void blitRepeating(PoseStack param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, int param8) {
        int var0 = param1;

        int var2;
        for(IntIterator var1 = slices(param3, param7); var1.hasNext(); var0 += var2) {
            var2 = var1.nextInt();
            int var3 = (param7 - var2) / 2;
            int var4 = param2;

            int var6;
            for(IntIterator var5 = slices(param4, param8); var5.hasNext(); var4 += var6) {
                var6 = var5.nextInt();
                int var7 = (param8 - var6) / 2;
                blit(param0, var0, var4, param5 + var3, param6 + var7, var2, var6);
            }
        }

    }

    private static IntIterator slices(int param0, int param1) {
        int var0 = Mth.positiveCeilDiv(param0, param1);
        return new Divisor(param0, var0);
    }

    @OnlyIn(Dist.CLIENT)
    static class ScissorStack {
        private final Deque<ScreenRectangle> stack = new ArrayDeque();

        public ScreenRectangle push(ScreenRectangle param0) {
            ScreenRectangle var0 = (ScreenRectangle)this.stack.peekLast();
            if (var0 != null) {
                ScreenRectangle var1 = (ScreenRectangle)Objects.requireNonNullElse(param0.intersection(var0), ScreenRectangle.empty());
                this.stack.addLast(var1);
                return var1;
            } else {
                this.stack.addLast(param0);
                return param0;
            }
        }

        @Nullable
        public ScreenRectangle pop() {
            if (this.stack.isEmpty()) {
                throw new IllegalStateException("Scissor stack underflow");
            } else {
                this.stack.removeLast();
                return (ScreenRectangle)this.stack.peekLast();
            }
        }
    }
}
