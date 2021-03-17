package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.VanillaPackResources;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LoadingOverlay extends Overlay {
    private static final ResourceLocation MOJANG_STUDIOS_LOGO_LOCATION = new ResourceLocation("textures/gui/title/mojangstudios.png");
    private static final int BRAND_BACKGROUND = FastColor.ARGB32.color(255, 239, 50, 61);
    private static final int BRAND_BACKGROUND_NO_ALPHA = BRAND_BACKGROUND & 16777215;
    private final Minecraft minecraft;
    private final ReloadInstance reload;
    private final Consumer<Optional<Throwable>> onFinish;
    private final boolean fadeIn;
    private float currentProgress;
    private long fadeOutStart = -1L;
    private long fadeInStart = -1L;

    public LoadingOverlay(Minecraft param0, ReloadInstance param1, Consumer<Optional<Throwable>> param2, boolean param3) {
        this.minecraft = param0;
        this.reload = param1;
        this.onFinish = param2;
        this.fadeIn = param3;
    }

    public static void registerTextures(Minecraft param0) {
        param0.getTextureManager().register(MOJANG_STUDIOS_LOGO_LOCATION, new LoadingOverlay.LogoTexture());
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        int var0 = this.minecraft.getWindow().getGuiScaledWidth();
        int var1 = this.minecraft.getWindow().getGuiScaledHeight();
        long var2 = Util.getMillis();
        if (this.fadeIn && this.fadeInStart == -1L) {
            this.fadeInStart = var2;
        }

        float var3 = this.fadeOutStart > -1L ? (float)(var2 - this.fadeOutStart) / 1000.0F : -1.0F;
        float var4 = this.fadeInStart > -1L ? (float)(var2 - this.fadeInStart) / 500.0F : -1.0F;
        float var6;
        if (var3 >= 1.0F) {
            if (this.minecraft.screen != null) {
                this.minecraft.screen.render(param0, 0, 0, param3);
            }

            int var5 = Mth.ceil((1.0F - Mth.clamp(var3 - 1.0F, 0.0F, 1.0F)) * 255.0F);
            fill(param0, 0, 0, var0, var1, BRAND_BACKGROUND_NO_ALPHA | var5 << 24);
            var6 = 1.0F - Mth.clamp(var3 - 1.0F, 0.0F, 1.0F);
        } else if (this.fadeIn) {
            if (this.minecraft.screen != null && var4 < 1.0F) {
                this.minecraft.screen.render(param0, param1, param2, param3);
            }

            int var7 = Mth.ceil(Mth.clamp((double)var4, 0.15, 1.0) * 255.0);
            fill(param0, 0, 0, var0, var1, BRAND_BACKGROUND_NO_ALPHA | var7 << 24);
            var6 = Mth.clamp(var4, 0.0F, 1.0F);
        } else {
            fill(param0, 0, 0, var0, var1, BRAND_BACKGROUND);
            var6 = 1.0F;
        }

        int var10 = (int)((double)this.minecraft.getWindow().getGuiScaledWidth() * 0.5);
        int var11 = (int)((double)this.minecraft.getWindow().getGuiScaledHeight() * 0.5);
        double var12 = Math.min((double)this.minecraft.getWindow().getGuiScaledWidth() * 0.75, (double)this.minecraft.getWindow().getGuiScaledHeight()) * 0.25;
        int var13 = (int)(var12 * 0.5);
        double var14 = var12 * 4.0;
        int var15 = (int)(var14 * 0.5);
        RenderSystem.setShaderTexture(0, MOJANG_STUDIOS_LOGO_LOCATION);
        RenderSystem.enableBlend();
        RenderSystem.blendEquation(32774);
        RenderSystem.blendFunc(770, 1);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, var6);
        blit(param0, var10 - var15, var11 - var13, var15, (int)var12, -0.0625F, 0.0F, 120, 60, 120, 120);
        blit(param0, var10, var11 - var13, var15, (int)var12, 0.0625F, 60.0F, 120, 60, 120, 120);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        int var16 = (int)((double)this.minecraft.getWindow().getGuiScaledHeight() * 0.8325);
        float var17 = this.reload.getActualProgress();
        this.currentProgress = Mth.clamp(this.currentProgress * 0.95F + var17 * 0.050000012F, 0.0F, 1.0F);
        if (var3 < 1.0F) {
            this.drawProgressBar(param0, var0 / 2 - var15, var16 - 5, var0 / 2 + var15, var16 + 5, 1.0F - Mth.clamp(var3, 0.0F, 1.0F));
        }

        if (var3 >= 2.0F) {
            this.minecraft.setOverlay(null);
        }

        if (this.fadeOutStart == -1L && this.reload.isDone() && (!this.fadeIn || var4 >= 2.0F)) {
            try {
                this.reload.checkExceptions();
                this.onFinish.accept(Optional.empty());
            } catch (Throwable var23) {
                this.onFinish.accept(Optional.of(var23));
            }

            this.fadeOutStart = Util.getMillis();
            if (this.minecraft.screen != null) {
                this.minecraft.screen.init(this.minecraft, this.minecraft.getWindow().getGuiScaledWidth(), this.minecraft.getWindow().getGuiScaledHeight());
            }
        }

    }

    private void drawProgressBar(PoseStack param0, int param1, int param2, int param3, int param4, float param5) {
        int var0 = Mth.ceil((float)(param3 - param1 - 2) * this.currentProgress);
        int var1 = Math.round(param5 * 255.0F);
        int var2 = FastColor.ARGB32.color(var1, 255, 255, 255);
        fill(param0, param1 + 1, param2, param3 - 1, param2 + 1, var2);
        fill(param0, param1 + 1, param4, param3 - 1, param4 - 1, var2);
        fill(param0, param1, param2, param1 + 1, param4, var2);
        fill(param0, param3, param2, param3 - 1, param4, var2);
        fill(param0, param1 + 2, param2 + 2, param1 + var0, param4 - 2, var2);
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    static class LogoTexture extends SimpleTexture {
        public LogoTexture() {
            super(LoadingOverlay.MOJANG_STUDIOS_LOGO_LOCATION);
        }

        @Override
        protected SimpleTexture.TextureImage getTextureImage(ResourceManager param0) {
            Minecraft var0 = Minecraft.getInstance();
            VanillaPackResources var1 = var0.getClientPackSource().getVanillaPack();

            try (InputStream var2 = var1.getResource(PackType.CLIENT_RESOURCES, LoadingOverlay.MOJANG_STUDIOS_LOGO_LOCATION)) {
                return new SimpleTexture.TextureImage(new TextureMetadataSection(true, true), NativeImage.read(var2));
            } catch (IOException var18) {
                return new SimpleTexture.TextureImage(var18);
            }
        }
    }
}
