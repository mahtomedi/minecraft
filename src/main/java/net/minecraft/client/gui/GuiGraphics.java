package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.gui.GuiSpriteScaling;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Vector2ic;

@OnlyIn(Dist.CLIENT)
public class GuiGraphics {
    public static final float MAX_GUI_Z = 10000.0F;
    public static final float MIN_GUI_Z = -10000.0F;
    private static final int EXTRA_SPACE_AFTER_FIRST_TOOLTIP_LINE = 2;
    private final Minecraft minecraft;
    private final PoseStack pose;
    private final MultiBufferSource.BufferSource bufferSource;
    private final GuiGraphics.ScissorStack scissorStack = new GuiGraphics.ScissorStack();
    private final GuiSpriteManager sprites;
    private boolean managed;

    private GuiGraphics(Minecraft param0, PoseStack param1, MultiBufferSource.BufferSource param2) {
        this.minecraft = param0;
        this.pose = param1;
        this.bufferSource = param2;
        this.sprites = param0.getGuiSprites();
    }

    public GuiGraphics(Minecraft param0, MultiBufferSource.BufferSource param1) {
        this(param0, new PoseStack(), param1);
    }

    @Deprecated
    public void drawManaged(Runnable param0) {
        this.flush();
        this.managed = true;
        param0.run();
        this.managed = false;
        this.flush();
    }

    @Deprecated
    private void flushIfUnmanaged() {
        if (!this.managed) {
            this.flush();
        }

    }

    @Deprecated
    private void flushIfManaged() {
        if (this.managed) {
            this.flush();
        }

    }

    public int guiWidth() {
        return this.minecraft.getWindow().getGuiScaledWidth();
    }

    public int guiHeight() {
        return this.minecraft.getWindow().getGuiScaledHeight();
    }

    public PoseStack pose() {
        return this.pose;
    }

    public MultiBufferSource.BufferSource bufferSource() {
        return this.bufferSource;
    }

    public void flush() {
        RenderSystem.disableDepthTest();
        this.bufferSource.endBatch();
        RenderSystem.enableDepthTest();
    }

    public void hLine(int param0, int param1, int param2, int param3) {
        this.hLine(RenderType.gui(), param0, param1, param2, param3);
    }

    public void hLine(RenderType param0, int param1, int param2, int param3, int param4) {
        if (param2 < param1) {
            int var0 = param1;
            param1 = param2;
            param2 = var0;
        }

        this.fill(param0, param1, param3, param2 + 1, param3 + 1, param4);
    }

    public void vLine(int param0, int param1, int param2, int param3) {
        this.vLine(RenderType.gui(), param0, param1, param2, param3);
    }

    public void vLine(RenderType param0, int param1, int param2, int param3, int param4) {
        if (param3 < param2) {
            int var0 = param2;
            param2 = param3;
            param3 = var0;
        }

        this.fill(param0, param1, param2 + 1, param1 + 1, param3, param4);
    }

    public void enableScissor(int param0, int param1, int param2, int param3) {
        this.applyScissor(this.scissorStack.push(new ScreenRectangle(param0, param1, param2 - param0, param3 - param1)));
    }

    public void disableScissor() {
        this.applyScissor(this.scissorStack.pop());
    }

    private void applyScissor(@Nullable ScreenRectangle param0) {
        this.flushIfManaged();
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

    public void setColor(float param0, float param1, float param2, float param3) {
        this.flushIfManaged();
        RenderSystem.setShaderColor(param0, param1, param2, param3);
    }

    public void fill(int param0, int param1, int param2, int param3, int param4) {
        this.fill(param0, param1, param2, param3, 0, param4);
    }

    public void fill(int param0, int param1, int param2, int param3, int param4, int param5) {
        this.fill(RenderType.gui(), param0, param1, param2, param3, param4, param5);
    }

    public void fill(RenderType param0, int param1, int param2, int param3, int param4, int param5) {
        this.fill(param0, param1, param2, param3, param4, 0, param5);
    }

    public void fill(RenderType param0, int param1, int param2, int param3, int param4, int param5, int param6) {
        Matrix4f var0 = this.pose.last().pose();
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
        VertexConsumer var7 = this.bufferSource.getBuffer(param0);
        var7.vertex(var0, (float)param1, (float)param2, (float)param5).color(var4, var5, var6, var3).endVertex();
        var7.vertex(var0, (float)param1, (float)param4, (float)param5).color(var4, var5, var6, var3).endVertex();
        var7.vertex(var0, (float)param3, (float)param4, (float)param5).color(var4, var5, var6, var3).endVertex();
        var7.vertex(var0, (float)param3, (float)param2, (float)param5).color(var4, var5, var6, var3).endVertex();
        this.flushIfUnmanaged();
    }

    public void fillGradient(int param0, int param1, int param2, int param3, int param4, int param5) {
        this.fillGradient(param0, param1, param2, param3, 0, param4, param5);
    }

    public void fillGradient(int param0, int param1, int param2, int param3, int param4, int param5, int param6) {
        this.fillGradient(RenderType.gui(), param0, param1, param2, param3, param5, param6, param4);
    }

    public void fillGradient(RenderType param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7) {
        VertexConsumer var0 = this.bufferSource.getBuffer(param0);
        this.fillGradient(var0, param1, param2, param3, param4, param7, param5, param6);
        this.flushIfUnmanaged();
    }

    private void fillGradient(VertexConsumer param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7) {
        float var0 = (float)FastColor.ARGB32.alpha(param6) / 255.0F;
        float var1 = (float)FastColor.ARGB32.red(param6) / 255.0F;
        float var2 = (float)FastColor.ARGB32.green(param6) / 255.0F;
        float var3 = (float)FastColor.ARGB32.blue(param6) / 255.0F;
        float var4 = (float)FastColor.ARGB32.alpha(param7) / 255.0F;
        float var5 = (float)FastColor.ARGB32.red(param7) / 255.0F;
        float var6 = (float)FastColor.ARGB32.green(param7) / 255.0F;
        float var7 = (float)FastColor.ARGB32.blue(param7) / 255.0F;
        Matrix4f var8 = this.pose.last().pose();
        param0.vertex(var8, (float)param1, (float)param2, (float)param5).color(var1, var2, var3, var0).endVertex();
        param0.vertex(var8, (float)param1, (float)param4, (float)param5).color(var5, var6, var7, var4).endVertex();
        param0.vertex(var8, (float)param3, (float)param4, (float)param5).color(var5, var6, var7, var4).endVertex();
        param0.vertex(var8, (float)param3, (float)param2, (float)param5).color(var1, var2, var3, var0).endVertex();
    }

    public void drawCenteredString(Font param0, String param1, int param2, int param3, int param4) {
        this.drawString(param0, param1, param2 - param0.width(param1) / 2, param3, param4);
    }

    public void drawCenteredString(Font param0, Component param1, int param2, int param3, int param4) {
        FormattedCharSequence var0 = param1.getVisualOrderText();
        this.drawString(param0, var0, param2 - param0.width(var0) / 2, param3, param4);
    }

    public void drawCenteredString(Font param0, FormattedCharSequence param1, int param2, int param3, int param4) {
        this.drawString(param0, param1, param2 - param0.width(param1) / 2, param3, param4);
    }

    public int drawString(Font param0, @Nullable String param1, int param2, int param3, int param4) {
        return this.drawString(param0, param1, param2, param3, param4, true);
    }

    public int drawString(Font param0, @Nullable String param1, int param2, int param3, int param4, boolean param5) {
        if (param1 == null) {
            return 0;
        } else {
            int var0 = param0.drawInBatch(
                param1,
                (float)param2,
                (float)param3,
                param4,
                param5,
                this.pose.last().pose(),
                this.bufferSource,
                Font.DisplayMode.NORMAL,
                0,
                15728880,
                param0.isBidirectional()
            );
            this.flushIfUnmanaged();
            return var0;
        }
    }

    public int drawString(Font param0, FormattedCharSequence param1, int param2, int param3, int param4) {
        return this.drawString(param0, param1, param2, param3, param4, true);
    }

    public int drawString(Font param0, FormattedCharSequence param1, int param2, int param3, int param4, boolean param5) {
        int var0 = param0.drawInBatch(
            param1, (float)param2, (float)param3, param4, param5, this.pose.last().pose(), this.bufferSource, Font.DisplayMode.NORMAL, 0, 15728880
        );
        this.flushIfUnmanaged();
        return var0;
    }

    public int drawString(Font param0, Component param1, int param2, int param3, int param4) {
        return this.drawString(param0, param1, param2, param3, param4, true);
    }

    public int drawString(Font param0, Component param1, int param2, int param3, int param4, boolean param5) {
        return this.drawString(param0, param1.getVisualOrderText(), param2, param3, param4, param5);
    }

    public void drawWordWrap(Font param0, FormattedText param1, int param2, int param3, int param4, int param5) {
        for(FormattedCharSequence var0 : param0.split(param1, param4)) {
            this.drawString(param0, var0, param2, param3, param5, false);
            param3 += 9;
        }

    }

    public void blit(int param0, int param1, int param2, int param3, int param4, TextureAtlasSprite param5) {
        this.blitSprite(param5, param0, param1, param2, param3, param4);
    }

    public void blit(
        int param0, int param1, int param2, int param3, int param4, TextureAtlasSprite param5, float param6, float param7, float param8, float param9
    ) {
        this.innerBlit(
            param5.atlasLocation(),
            param0,
            param0 + param3,
            param1,
            param1 + param4,
            param2,
            param5.getU0(),
            param5.getU1(),
            param5.getV0(),
            param5.getV1(),
            param6,
            param7,
            param8,
            param9
        );
    }

    public void renderOutline(int param0, int param1, int param2, int param3, int param4) {
        this.fill(param0, param1, param0 + param2, param1 + 1, param4);
        this.fill(param0, param1 + param3 - 1, param0 + param2, param1 + param3, param4);
        this.fill(param0, param1 + 1, param0 + 1, param1 + param3 - 1, param4);
        this.fill(param0 + param2 - 1, param1 + 1, param0 + param2, param1 + param3 - 1, param4);
    }

    public void blitSprite(ResourceLocation param0, int param1, int param2, int param3, int param4) {
        this.blitSprite(param0, param1, param2, 0, param3, param4);
    }

    public void blitSprite(ResourceLocation param0, int param1, int param2, int param3, int param4, int param5) {
        TextureAtlasSprite var0 = this.sprites.getSprite(param0);
        GuiSpriteScaling var1 = this.sprites.getSpriteScaling(var0);
        if (var1 instanceof GuiSpriteScaling.Stretch) {
            this.blitSprite(var0, param1, param2, param3, param4, param5);
        } else if (var1 instanceof GuiSpriteScaling.Tile var2) {
            this.blitTiledSprite(var0, param1, param2, param3, param4, param5, 0, 0, var2.width(), var2.height(), var2.width(), var2.height());
        } else if (var1 instanceof GuiSpriteScaling.NineSlice var3) {
            this.blitNineSlicedSprite(var0, var3, param1, param2, param3, param4, param5);
        }

    }

    public void blitSprite(ResourceLocation param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, int param8) {
        this.blitSprite(param0, param1, param2, param3, param4, param5, param6, 0, param7, param8);
    }

    public void blitSprite(ResourceLocation param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, int param8, int param9) {
        TextureAtlasSprite var0 = this.sprites.getSprite(param0);
        GuiSpriteScaling var1 = this.sprites.getSpriteScaling(var0);
        if (var1 instanceof GuiSpriteScaling.Stretch) {
            this.blitSprite(var0, param1, param2, param3, param4, param5, param6, param7, param8, param9);
        } else {
            this.blitSprite(var0, param5, param6, param7, param8, param9);
        }

    }

    private void blitSprite(
        TextureAtlasSprite param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, int param8, int param9
    ) {
        if (param8 != 0 && param9 != 0) {
            this.innerBlit(
                param0.atlasLocation(),
                param5,
                param5 + param8,
                param6,
                param6 + param9,
                param7,
                param0.getU((float)param3 / (float)param1),
                param0.getU((float)(param3 + param8) / (float)param1),
                param0.getV((float)param4 / (float)param2),
                param0.getV((float)(param4 + param9) / (float)param2)
            );
        }
    }

    private void blitSprite(TextureAtlasSprite param0, int param1, int param2, int param3, int param4, int param5) {
        if (param4 != 0 && param5 != 0) {
            this.innerBlit(
                param0.atlasLocation(),
                param1,
                param1 + param4,
                param2,
                param2 + param5,
                param3,
                param0.getU0(),
                param0.getU1(),
                param0.getV0(),
                param0.getV1()
            );
        }
    }

    public void blit(ResourceLocation param0, int param1, int param2, int param3, int param4, int param5, int param6) {
        this.blit(param0, param1, param2, 0, (float)param3, (float)param4, param5, param6, 256, 256);
    }

    public void blit(ResourceLocation param0, int param1, int param2, int param3, float param4, float param5, int param6, int param7, int param8, int param9) {
        this.blit(param0, param1, param1 + param6, param2, param2 + param7, param3, param6, param7, param4, param5, param8, param9);
    }

    public void blit(
        ResourceLocation param0, int param1, int param2, int param3, int param4, float param5, float param6, int param7, int param8, int param9, int param10
    ) {
        this.blit(param0, param1, param1 + param3, param2, param2 + param4, 0, param7, param8, param5, param6, param9, param10);
    }

    public void blit(ResourceLocation param0, int param1, int param2, float param3, float param4, int param5, int param6, int param7, int param8) {
        this.blit(param0, param1, param2, param5, param6, param3, param4, param5, param6, param7, param8);
    }

    void blit(
        ResourceLocation param0,
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
        this.innerBlit(
            param0,
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

    void innerBlit(ResourceLocation param0, int param1, int param2, int param3, int param4, int param5, float param6, float param7, float param8, float param9) {
        RenderSystem.setShaderTexture(0, param0);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        Matrix4f var0 = this.pose.last().pose();
        BufferBuilder var1 = Tesselator.getInstance().getBuilder();
        var1.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        var1.vertex(var0, (float)param1, (float)param3, (float)param5).uv(param6, param8).endVertex();
        var1.vertex(var0, (float)param1, (float)param4, (float)param5).uv(param6, param9).endVertex();
        var1.vertex(var0, (float)param2, (float)param4, (float)param5).uv(param7, param9).endVertex();
        var1.vertex(var0, (float)param2, (float)param3, (float)param5).uv(param7, param8).endVertex();
        BufferUploader.drawWithShader(var1.end());
    }

    void innerBlit(
        ResourceLocation param0,
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
        RenderSystem.setShaderTexture(0, param0);
        RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
        RenderSystem.enableBlend();
        Matrix4f var0 = this.pose.last().pose();
        BufferBuilder var1 = Tesselator.getInstance().getBuilder();
        var1.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
        var1.vertex(var0, (float)param1, (float)param3, (float)param5).color(param10, param11, param12, param13).uv(param6, param8).endVertex();
        var1.vertex(var0, (float)param1, (float)param4, (float)param5).color(param10, param11, param12, param13).uv(param6, param9).endVertex();
        var1.vertex(var0, (float)param2, (float)param4, (float)param5).color(param10, param11, param12, param13).uv(param7, param9).endVertex();
        var1.vertex(var0, (float)param2, (float)param3, (float)param5).color(param10, param11, param12, param13).uv(param7, param8).endVertex();
        BufferUploader.drawWithShader(var1.end());
        RenderSystem.disableBlend();
    }

    private void blitNineSlicedSprite(TextureAtlasSprite param0, GuiSpriteScaling.NineSlice param1, int param2, int param3, int param4, int param5, int param6) {
        GuiSpriteScaling.NineSlice.Border var0 = param1.border();
        int var1 = Math.min(var0.left(), param5 / 2);
        int var2 = Math.min(var0.right(), param5 / 2);
        int var3 = Math.min(var0.top(), param6 / 2);
        int var4 = Math.min(var0.bottom(), param6 / 2);
        if (param5 == param1.width() && param6 == param1.height()) {
            this.blitSprite(param0, param1.width(), param1.height(), 0, 0, param2, param3, param4, param5, param6);
        } else if (param6 == param1.height()) {
            this.blitSprite(param0, param1.width(), param1.height(), 0, 0, param2, param3, param4, var1, param6);
            this.blitTiledSprite(
                param0,
                param2 + var1,
                param3,
                param4,
                param5 - var2 - var1,
                param6,
                var1,
                0,
                param1.width() - var2 - var1,
                param1.height(),
                param1.width(),
                param1.height()
            );
            this.blitSprite(param0, param1.width(), param1.height(), param1.width() - var2, 0, param2 + param5 - var2, param3, param4, var2, param6);
        } else if (param5 == param1.width()) {
            this.blitSprite(param0, param1.width(), param1.height(), 0, 0, param2, param3, param4, param5, var3);
            this.blitTiledSprite(
                param0,
                param2,
                param3 + var3,
                param4,
                param5,
                param6 - var4 - var3,
                0,
                var3,
                param1.width(),
                param1.height() - var4 - var3,
                param1.width(),
                param1.height()
            );
            this.blitSprite(param0, param1.width(), param1.height(), 0, param1.height() - var4, param2, param3 + param6 - var4, param4, param5, var4);
        } else {
            this.blitSprite(param0, param1.width(), param1.height(), 0, 0, param2, param3, param4, var1, var3);
            this.blitTiledSprite(
                param0, param2 + var1, param3, param4, param5 - var2 - var1, var3, var1, 0, param1.width() - var2 - var1, var3, param1.width(), param1.height()
            );
            this.blitSprite(param0, param1.width(), param1.height(), param1.width() - var2, 0, param2 + param5 - var2, param3, param4, var2, var3);
            this.blitSprite(param0, param1.width(), param1.height(), 0, param1.height() - var4, param2, param3 + param6 - var4, param4, var1, var4);
            this.blitTiledSprite(
                param0,
                param2 + var1,
                param3 + param6 - var4,
                param4,
                param5 - var2 - var1,
                var4,
                var1,
                param1.height() - var4,
                param1.width() - var2 - var1,
                var4,
                param1.width(),
                param1.height()
            );
            this.blitSprite(
                param0,
                param1.width(),
                param1.height(),
                param1.width() - var2,
                param1.height() - var4,
                param2 + param5 - var2,
                param3 + param6 - var4,
                param4,
                var2,
                var4
            );
            this.blitTiledSprite(
                param0,
                param2,
                param3 + var3,
                param4,
                var1,
                param6 - var4 - var3,
                0,
                var3,
                var1,
                param1.height() - var4 - var3,
                param1.width(),
                param1.height()
            );
            this.blitTiledSprite(
                param0,
                param2 + var1,
                param3 + var3,
                param4,
                param5 - var2 - var1,
                param6 - var4 - var3,
                var1,
                var3,
                param1.width() - var2 - var1,
                param1.height() - var4 - var3,
                param1.width(),
                param1.height()
            );
            this.blitTiledSprite(
                param0,
                param2 + param5 - var2,
                param3 + var3,
                param4,
                var1,
                param6 - var4 - var3,
                param1.width() - var2,
                var3,
                var2,
                param1.height() - var4 - var3,
                param1.width(),
                param1.height()
            );
        }
    }

    private void blitTiledSprite(
        TextureAtlasSprite param0,
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
        int param11
    ) {
        if (param4 > 0 && param5 > 0) {
            if (param8 > 0 && param9 > 0) {
                for(int var0 = 0; var0 < param4; var0 += param8) {
                    int var1 = Math.min(param8, param4 - var0);

                    for(int var2 = 0; var2 < param5; var2 += param9) {
                        int var3 = Math.min(param9, param5 - var2);
                        this.blitSprite(param0, param10, param11, param6, param7, param1 + var0, param2 + var2, param3, var1, var3);
                    }
                }

            } else {
                throw new IllegalArgumentException("Tiled sprite texture size must be positive, got " + param8 + "x" + param9);
            }
        }
    }

    public void renderItem(ItemStack param0, int param1, int param2) {
        this.renderItem(this.minecraft.player, this.minecraft.level, param0, param1, param2, 0);
    }

    public void renderItem(ItemStack param0, int param1, int param2, int param3) {
        this.renderItem(this.minecraft.player, this.minecraft.level, param0, param1, param2, param3);
    }

    public void renderItem(ItemStack param0, int param1, int param2, int param3, int param4) {
        this.renderItem(this.minecraft.player, this.minecraft.level, param0, param1, param2, param3, param4);
    }

    public void renderFakeItem(ItemStack param0, int param1, int param2) {
        this.renderFakeItem(param0, param1, param2, 0);
    }

    public void renderFakeItem(ItemStack param0, int param1, int param2, int param3) {
        this.renderItem(null, this.minecraft.level, param0, param1, param2, param3);
    }

    public void renderItem(LivingEntity param0, ItemStack param1, int param2, int param3, int param4) {
        this.renderItem(param0, param0.level(), param1, param2, param3, param4);
    }

    private void renderItem(@Nullable LivingEntity param0, @Nullable Level param1, ItemStack param2, int param3, int param4, int param5) {
        this.renderItem(param0, param1, param2, param3, param4, param5, 0);
    }

    private void renderItem(@Nullable LivingEntity param0, @Nullable Level param1, ItemStack param2, int param3, int param4, int param5, int param6) {
        if (!param2.isEmpty()) {
            BakedModel var0 = this.minecraft.getItemRenderer().getModel(param2, param1, param0, param5);
            this.pose.pushPose();
            this.pose.translate((float)(param3 + 8), (float)(param4 + 8), (float)(150 + (var0.isGui3d() ? param6 : 0)));

            try {
                this.pose.mulPoseMatrix(new Matrix4f().scaling(1.0F, -1.0F, 1.0F));
                this.pose.scale(16.0F, 16.0F, 16.0F);
                boolean var1 = !var0.usesBlockLight();
                if (var1) {
                    Lighting.setupForFlatItems();
                }

                this.minecraft
                    .getItemRenderer()
                    .render(param2, ItemDisplayContext.GUI, false, this.pose, this.bufferSource(), 15728880, OverlayTexture.NO_OVERLAY, var0);
                this.flush();
                if (var1) {
                    Lighting.setupFor3DItems();
                }
            } catch (Throwable var12) {
                CrashReport var3 = CrashReport.forThrowable(var12, "Rendering item");
                CrashReportCategory var4 = var3.addCategory("Item being rendered");
                var4.setDetail("Item Type", () -> String.valueOf(param2.getItem()));
                var4.setDetail("Item Damage", () -> String.valueOf(param2.getDamageValue()));
                var4.setDetail("Item NBT", () -> String.valueOf(param2.getTag()));
                var4.setDetail("Item Foil", () -> String.valueOf(param2.hasFoil()));
                throw new ReportedException(var3);
            }

            this.pose.popPose();
        }
    }

    public void renderItemDecorations(Font param0, ItemStack param1, int param2, int param3) {
        this.renderItemDecorations(param0, param1, param2, param3, null);
    }

    public void renderItemDecorations(Font param0, ItemStack param1, int param2, int param3, @Nullable String param4) {
        if (!param1.isEmpty()) {
            this.pose.pushPose();
            if (param1.getCount() != 1 || param4 != null) {
                String var0 = param4 == null ? String.valueOf(param1.getCount()) : param4;
                this.pose.translate(0.0F, 0.0F, 200.0F);
                this.drawString(param0, var0, param2 + 19 - 2 - param0.width(var0), param3 + 6 + 3, 16777215, true);
            }

            if (param1.isBarVisible()) {
                int var1 = param1.getBarWidth();
                int var2 = param1.getBarColor();
                int var3 = param2 + 2;
                int var4 = param3 + 13;
                this.fill(RenderType.guiOverlay(), var3, var4, var3 + 13, var4 + 2, -16777216);
                this.fill(RenderType.guiOverlay(), var3, var4, var3 + var1, var4 + 1, var2 | 0xFF000000);
            }

            LocalPlayer var5 = this.minecraft.player;
            float var6 = var5 == null ? 0.0F : var5.getCooldowns().getCooldownPercent(param1.getItem(), this.minecraft.getFrameTime());
            if (var6 > 0.0F) {
                int var7 = param3 + Mth.floor(16.0F * (1.0F - var6));
                int var8 = var7 + Mth.ceil(16.0F * var6);
                this.fill(RenderType.guiOverlay(), param2, var7, param2 + 16, var8, Integer.MAX_VALUE);
            }

            this.pose.popPose();
        }
    }

    public void renderTooltip(Font param0, ItemStack param1, int param2, int param3) {
        this.renderTooltip(param0, Screen.getTooltipFromItem(this.minecraft, param1), param1.getTooltipImage(), param2, param3);
    }

    public void renderTooltip(Font param0, List<Component> param1, Optional<TooltipComponent> param2, int param3, int param4) {
        List<ClientTooltipComponent> var0 = param1.stream().map(Component::getVisualOrderText).map(ClientTooltipComponent::create).collect(Collectors.toList());
        param2.ifPresent(param1x -> var0.add(1, ClientTooltipComponent.create(param1x)));
        this.renderTooltipInternal(param0, var0, param3, param4, DefaultTooltipPositioner.INSTANCE);
    }

    public void renderTooltip(Font param0, Component param1, int param2, int param3) {
        this.renderTooltip(param0, List.of(param1.getVisualOrderText()), param2, param3);
    }

    public void renderComponentTooltip(Font param0, List<Component> param1, int param2, int param3) {
        this.renderTooltip(param0, Lists.transform(param1, Component::getVisualOrderText), param2, param3);
    }

    public void renderTooltip(Font param0, List<? extends FormattedCharSequence> param1, int param2, int param3) {
        this.renderTooltipInternal(
            param0, param1.stream().map(ClientTooltipComponent::create).collect(Collectors.toList()), param2, param3, DefaultTooltipPositioner.INSTANCE
        );
    }

    public void renderTooltip(Font param0, List<FormattedCharSequence> param1, ClientTooltipPositioner param2, int param3, int param4) {
        this.renderTooltipInternal(param0, param1.stream().map(ClientTooltipComponent::create).collect(Collectors.toList()), param3, param4, param2);
    }

    private void renderTooltipInternal(Font param0, List<ClientTooltipComponent> param1, int param2, int param3, ClientTooltipPositioner param4) {
        if (!param1.isEmpty()) {
            int var0 = 0;
            int var1 = param1.size() == 1 ? -2 : 0;

            for(ClientTooltipComponent var2 : param1) {
                int var3 = var2.getWidth(param0);
                if (var3 > var0) {
                    var0 = var3;
                }

                var1 += var2.getHeight();
            }

            int var4 = var0;
            int var5 = var1;
            Vector2ic var6 = param4.positionTooltip(this.guiWidth(), this.guiHeight(), param2, param3, var4, var5);
            int var7 = var6.x();
            int var8 = var6.y();
            this.pose.pushPose();
            int var9 = 400;
            this.drawManaged(() -> TooltipRenderUtil.renderTooltipBackground(this, var7, var8, var4, var5, 400));
            this.pose.translate(0.0F, 0.0F, 400.0F);
            int var10 = var8;

            for(int var11 = 0; var11 < param1.size(); ++var11) {
                ClientTooltipComponent var12 = param1.get(var11);
                var12.renderText(param0, var7, var10, this.pose.last().pose(), this.bufferSource);
                var10 += var12.getHeight() + (var11 == 0 ? 2 : 0);
            }

            var10 = var8;

            for(int var13 = 0; var13 < param1.size(); ++var13) {
                ClientTooltipComponent var14 = param1.get(var13);
                var14.renderImage(param0, var7, var10, this);
                var10 += var14.getHeight() + (var13 == 0 ? 2 : 0);
            }

            this.pose.popPose();
        }
    }

    public void renderComponentHoverEffect(Font param0, @Nullable Style param1, int param2, int param3) {
        if (param1 != null && param1.getHoverEvent() != null) {
            HoverEvent var0 = param1.getHoverEvent();
            HoverEvent.ItemStackInfo var1 = var0.getValue(HoverEvent.Action.SHOW_ITEM);
            if (var1 != null) {
                this.renderTooltip(param0, var1.getItemStack(), param2, param3);
            } else {
                HoverEvent.EntityTooltipInfo var2 = var0.getValue(HoverEvent.Action.SHOW_ENTITY);
                if (var2 != null) {
                    if (this.minecraft.options.advancedItemTooltips) {
                        this.renderComponentTooltip(param0, var2.getTooltipLines(), param2, param3);
                    }
                } else {
                    Component var3 = var0.getValue(HoverEvent.Action.SHOW_TEXT);
                    if (var3 != null) {
                        this.renderTooltip(param0, param0.split(var3, Math.max(this.guiWidth() / 2, 200)), param2, param3);
                    }
                }
            }

        }
    }

    @OnlyIn(Dist.CLIENT)
    static class ScissorStack {
        private final Deque<ScreenRectangle> stack = new ArrayDeque<>();

        public ScreenRectangle push(ScreenRectangle param0) {
            ScreenRectangle var0 = this.stack.peekLast();
            if (var0 != null) {
                ScreenRectangle var1 = Objects.requireNonNullElse(param0.intersection(var0), ScreenRectangle.empty());
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
                return this.stack.peekLast();
            }
        }
    }
}
