package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LogoOverlay extends Overlay {
    private static final int TEXT_WIDTH = 208;
    private static final int TEXT_HEIGHT = 38;
    private static final int LOGO_WIDTH = 39;
    private static final int LOGO_HEIGHT = 38;
    private static final ResourceLocation LOGO_TEXTURE = new ResourceLocation("textures/gui/mojang_logo.png");
    private static final ResourceLocation TEXT_TEXTURE = new ResourceLocation("textures/gui/mojang_text.png");
    private static final float SMOOTHING = 0.99F;
    private LogoOverlay.Stage stage = LogoOverlay.Stage.INIT;
    private long textFadeInStart = -1L;
    private float logoFlyInProgress;
    private long lastTick;
    private SoundInstance logoSound;
    private final Minecraft minecraft;
    private final ReloadInstance reload;
    private final Consumer<Optional<Throwable>> onFinish;
    private float currentProgress;
    private final LogoOverlay.Effect effect;

    public LogoOverlay(Minecraft param0, ReloadInstance param1, Consumer<Optional<Throwable>> param2) {
        this.minecraft = param0;
        this.reload = param1;
        this.onFinish = param2;
        LogoOverlay.Effect[] var0 = LogoOverlay.Effect.values();
        this.effect = var0[(int)(System.currentTimeMillis() % (long)var0.length)];
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        int var0 = this.minecraft.getWindow().getGuiScaledWidth();
        int var1 = this.minecraft.getWindow().getGuiScaledHeight();
        RenderSystem.enableBlend();
        RenderSystem.blendEquation(32774);
        RenderSystem.blendFunc(770, 1);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.blendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO
        );
        RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 1.0F);
        RenderSystem.clear(16640, Minecraft.ON_OSX);
        long var2 = Util.getMillis();
        long var3 = var2 - this.lastTick;
        this.lastTick = var2;
        if (this.stage == LogoOverlay.Stage.INIT) {
            this.stage = LogoOverlay.Stage.FLY;
            this.logoFlyInProgress = 10.0F;
            this.textFadeInStart = -1L;
        } else {
            if (this.stage == LogoOverlay.Stage.FLY) {
                this.logoFlyInProgress -= (float)var3 / 500.0F;
                if (this.logoFlyInProgress <= 0.0F) {
                    this.stage = LogoOverlay.Stage.WAIT_FOR_LOAD;
                }
            } else if (this.stage == LogoOverlay.Stage.WAIT_FOR_LOAD) {
                if (this.reload.isDone()) {
                    this.textFadeInStart = var2;
                    this.logoSound = new SimpleSoundInstance(
                        SoundEvents.AWESOME_INTRO.getLocation(), SoundSource.MASTER, 0.25F, 1.0F, false, 0, SoundInstance.Attenuation.NONE, 0.0, 0.0, 0.0, true
                    );
                    this.minecraft.getSoundManager().play(this.logoSound);
                    this.stage = LogoOverlay.Stage.TEXT;
                }
            } else if (!this.minecraft.getSoundManager().isActive(this.logoSound)) {
                this.minecraft.setOverlay(null);

                try {
                    this.reload.checkExceptions();
                    this.onFinish.accept(Optional.empty());
                } catch (Throwable var17) {
                    this.onFinish.accept(Optional.of(var17));
                }

                if (this.minecraft.screen != null) {
                    this.minecraft.screen.init(this.minecraft, this.minecraft.getWindow().getGuiScaledWidth(), this.minecraft.getWindow().getGuiScaledHeight());
                }

                this.stage = LogoOverlay.Stage.INIT;
            }

            Tesselator var5 = Tesselator.getInstance();
            BufferBuilder var6 = var5.getBuilder();
            var6.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            int var7 = Mth.clamp((int)(var2 - this.textFadeInStart), 0, 255);
            if (this.textFadeInStart != -1L) {
                RenderSystem.setShaderTexture(0, TEXT_TEXTURE);
                this.blit(param0, var6, var0 / 2, var1 - var1 / 8, 208, 38, var7);
            }

            var5.end();
            param0.pushPose();
            param0.translate((double)((float)var0 / 2.0F), (double)((float)var1 / 2.0F), 0.0);
            switch(this.effect) {
                case CLASSIC:
                    float var8 = 20.0F * this.logoFlyInProgress;
                    float var9 = 100.0F * Mth.sin(this.logoFlyInProgress);
                    param0.mulPose(Vector3f.ZP.rotationDegrees(var8));
                    param0.translate((double)var9, 0.0, 0.0);
                    float var10 = 1.0F / (2.0F * this.logoFlyInProgress + 1.0F);
                    param0.mulPose(Vector3f.ZP.rotationDegrees(1.5F * this.logoFlyInProgress));
                    param0.scale(var10, var10, 1.0F);
                    break;
                case SPRING:
                    float var11 = 40.0F * ((float)Math.exp((double)(this.logoFlyInProgress / 3.0F)) - 1.0F) * Mth.sin(this.logoFlyInProgress);
                    param0.translate((double)var11, 0.0, 0.0);
                    break;
                case SLOWDOWN:
                    float var12 = (float)Math.exp((double)this.logoFlyInProgress) - 1.0F;
                    param0.mulPose(Vector3f.XP.rotationDegrees(var12));
                    break;
                case REVERSE:
                    float var13 = Mth.cos(this.logoFlyInProgress / 10.0F * (float) Math.PI);
                    param0.scale(var13, var13, 1.0F);
                    break;
                case GROW:
                    float var14 = (1.0F - this.logoFlyInProgress / 10.0F) * 0.75F;
                    float var15 = 2.0F * Mth.sin(var14 * (float) Math.PI);
                    param0.scale(var15, var15, 1.0F);
            }

            RenderSystem.setShaderTexture(0, LOGO_TEXTURE);
            var6.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            this.blit(param0, var6, 0, 0, 78, 76, 255);
            var5.end();
            float var16 = this.reload.getActualProgress();
            this.currentProgress = Mth.clamp(this.currentProgress * 0.99F + var16 * 0.00999999F, 0.0F, 1.0F);
            this.drawProgressBar(param0, -39, 38, 39, 48, this.currentProgress != 1.0F ? 1.0F : 0.0F);
            param0.popPose();
        }
    }

    private void blit(PoseStack param0, BufferBuilder param1, int param2, int param3, int param4, int param5, int param6) {
        int var0 = param4 / 2;
        int var1 = param5 / 2;
        Matrix4f var2 = param0.last().pose();
        param1.vertex(var2, (float)(param2 - var0), (float)(param3 + var1), 0.0F).uv(0.0F, 1.0F).color(255, 255, 255, param6).endVertex();
        param1.vertex(var2, (float)(param2 + var0), (float)(param3 + var1), 0.0F).uv(1.0F, 1.0F).color(255, 255, 255, param6).endVertex();
        param1.vertex(var2, (float)(param2 + var0), (float)(param3 - var1), 0.0F).uv(1.0F, 0.0F).color(255, 255, 255, param6).endVertex();
        param1.vertex(var2, (float)(param2 - var0), (float)(param3 - var1), 0.0F).uv(0.0F, 0.0F).color(255, 255, 255, param6).endVertex();
    }

    private void drawProgressBar(PoseStack param0, int param1, int param2, int param3, int param4, float param5) {
        int var0 = Mth.ceil((float)(param3 - param1 - 2) * this.currentProgress);
        int var1 = Math.round(param5 * 255.0F);
        int var2 = FastColor.ARGB32.color(var1, 255, 255, 255);
        fill(param0, param1 + 2, param2 + 2, param1 + var0, param4 - 2, var2);
        fill(param0, param1 + 1, param2, param3 - 1, param2 + 1, var2);
        fill(param0, param1 + 1, param4, param3 - 1, param4 - 1, var2);
        fill(param0, param1, param2, param1 + 1, param4, var2);
        fill(param0, param3, param2, param3 - 1, param4, var2);
    }

    @OnlyIn(Dist.CLIENT)
    static enum Effect {
        CLASSIC,
        SPRING,
        SLOWDOWN,
        REVERSE,
        GROW;
    }

    @OnlyIn(Dist.CLIENT)
    static enum Stage {
        INIT,
        FLY,
        WAIT_FOR_LOAD,
        TEXT;
    }
}
