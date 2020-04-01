package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LogoOverlay extends Overlay {
    private static final ResourceLocation LOGO_TEXTURE = new ResourceLocation("textures/gui/mojang_logo.png");
    private static final ResourceLocation TEXT_TEXTURE = new ResourceLocation("textures/gui/mojang_text.png");
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
    public void render(int param0, int param1, float param2) {
        int var0 = this.minecraft.getWindow().getGuiScaledWidth();
        int var1 = this.minecraft.getWindow().getGuiScaledHeight();
        RenderSystem.enableTexture();
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.disableAlphaTest();
        RenderSystem.blendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO
        );
        RenderSystem.clearColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.clear(16640, Minecraft.ON_OSX);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
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
                        SoundEvents.AWESOME_INTRO.getLocation(),
                        SoundSource.MASTER,
                        0.25F,
                        1.0F,
                        false,
                        0,
                        SoundInstance.Attenuation.NONE,
                        0.0F,
                        0.0F,
                        0.0F,
                        true
                    );
                    this.minecraft.getSoundManager().play(this.logoSound);
                    this.stage = LogoOverlay.Stage.TEXT;
                }
            } else if (!this.minecraft.getSoundManager().isActive(this.logoSound)) {
                this.minecraft.setOverlay(null);

                try {
                    this.reload.checkExceptions();
                    this.onFinish.accept(Optional.empty());
                } catch (Throwable var161) {
                    this.onFinish.accept(Optional.of(var161));
                }

                if (this.minecraft.screen != null) {
                    this.minecraft.screen.init(this.minecraft, this.minecraft.getWindow().getGuiScaledWidth(), this.minecraft.getWindow().getGuiScaledHeight());
                }

                this.stage = LogoOverlay.Stage.INIT;
            }

            Tesselator var5 = Tesselator.getInstance();
            BufferBuilder var6 = var5.getBuilder();
            var6.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
            int var7 = Mth.clamp((int)(var2 - this.textFadeInStart), 0, 255);
            if (this.textFadeInStart != -1L) {
                this.minecraft.getTextureManager().bind(TEXT_TEXTURE);
                this.blit(var6, var0 / 2, var1 - var1 / 8, 208, 38, var7);
            }

            var5.end();
            RenderSystem.pushMatrix();
            RenderSystem.translatef((float)var0 / 2.0F, (float)var1 / 2.0F, 0.0F);
            switch(this.effect) {
                case CLASSIC:
                    float var8 = 20.0F * this.logoFlyInProgress;
                    float var9 = 100.0F * Mth.sin(this.logoFlyInProgress);
                    RenderSystem.rotatef(var8, 0.0F, 0.0F, 1.0F);
                    RenderSystem.translatef(var9, 0.0F, 0.0F);
                    float var10 = 1.0F / (2.0F * this.logoFlyInProgress + 1.0F);
                    RenderSystem.rotatef(1.5F * this.logoFlyInProgress, 0.0F, 0.0F, 1.0F);
                    RenderSystem.scalef(var10, var10, 1.0F);
                    break;
                case SPRING:
                    float var11 = 40.0F * ((float)Math.exp((double)(this.logoFlyInProgress / 3.0F)) - 1.0F) * Mth.sin(this.logoFlyInProgress);
                    RenderSystem.translatef(var11, 0.0F, 0.0F);
                    break;
                case SLOWDOWN:
                    float var12 = (float)Math.exp((double)this.logoFlyInProgress) - 1.0F;
                    RenderSystem.rotatef(var12, 1.0F, 0.0F, 0.0F);
                    break;
                case REVERSE:
                    float var13 = Mth.cos(this.logoFlyInProgress / 10.0F * (float) Math.PI);
                    RenderSystem.scalef(var13, var13, 1.0F);
                    break;
                case GROW:
                    float var14 = (1.0F - this.logoFlyInProgress / 10.0F) * 0.75F;
                    float var15 = 2.0F * Mth.sin(var14 * (float) Math.PI);
                    RenderSystem.scalef(var15, var15, 1.0F);
            }

            this.minecraft.getTextureManager().bind(LOGO_TEXTURE);
            var6.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
            this.blit(var6, 0, 0, 78, 76, 255);
            var5.end();
            float var16 = this.reload.getActualProgress();
            this.currentProgress = Mth.clamp(this.currentProgress * 0.99F + var16 * 0.00999999F, 0.0F, 1.0F);
            this.drawProgressBar(-39, 38, 39, 48, this.currentProgress != 1.0F ? 1.0F : 0.0F);
            RenderSystem.popMatrix();
        }
    }

    private void blit(BufferBuilder param0, int param1, int param2, int param3, int param4, int param5) {
        int var0 = param3 / 2;
        int var1 = param4 / 2;
        param0.vertex((double)(param1 - var0), (double)(param2 + var1), 0.0).uv(0.0F, 1.0F).color(255, 255, 255, param5).endVertex();
        param0.vertex((double)(param1 + var0), (double)(param2 + var1), 0.0).uv(1.0F, 1.0F).color(255, 255, 255, param5).endVertex();
        param0.vertex((double)(param1 + var0), (double)(param2 - var1), 0.0).uv(1.0F, 0.0F).color(255, 255, 255, param5).endVertex();
        param0.vertex((double)(param1 - var0), (double)(param2 - var1), 0.0).uv(0.0F, 0.0F).color(255, 255, 255, param5).endVertex();
    }

    private void drawProgressBar(int param0, int param1, int param2, int param3, float param4) {
        int var0 = Mth.ceil((float)(param2 - param0 - 1) * this.currentProgress);
        fill(
            param0 - 1,
            param1 - 1,
            param2 + 1,
            param3 + 1,
            0xFF000000 | Math.round((1.0F - param4) * 255.0F) << 16 | Math.round((1.0F - param4) * 255.0F) << 8 | Math.round((1.0F - param4) * 255.0F)
        );
        fill(param0, param1, param2, param3, -1);
        fill(
            param0 + 1,
            param1 + 1,
            param0 + var0,
            param3 - 1,
            0xFF000000
                | (int)Mth.lerp(1.0F - param4, 226.0F, 255.0F) << 16
                | (int)Mth.lerp(1.0F - param4, 40.0F, 255.0F) << 8
                | (int)Mth.lerp(1.0F - param4, 55.0F, 255.0F)
        );
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
