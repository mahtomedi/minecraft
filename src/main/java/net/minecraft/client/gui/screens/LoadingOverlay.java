package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
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
    static final ResourceLocation MOJANG_STUDIOS_LOGO_LOCATION = new ResourceLocation("textures/gui/title/mojangstudios.png");
    private static final int LOGO_BACKGROUND_COLOR = FastColor.ARGB32.color(255, 239, 50, 61);
    private static final int LOGO_BACKGROUND_COLOR_DARK = FastColor.ARGB32.color(255, 0, 0, 0);
    private static final IntSupplier BRAND_BACKGROUND = () -> Minecraft.getInstance().options.darkMojangStudiosBackground().get()
            ? LOGO_BACKGROUND_COLOR_DARK
            : LOGO_BACKGROUND_COLOR;
    private static final int LOGO_SCALE = 240;
    private static final float LOGO_QUARTER_FLOAT = 60.0F;
    private static final int LOGO_QUARTER = 60;
    private static final int LOGO_HALF = 120;
    private static final float LOGO_OVERLAP = 0.0625F;
    private static final float SMOOTHING = 0.95F;
    public static final long FADE_OUT_TIME = 1000L;
    public static final long FADE_IN_TIME = 500L;
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

    private static int replaceAlpha(int param0, int param1) {
        return param0 & 16777215 | param1 << 24;
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
            fill(param0, 0, 0, var0, var1, replaceAlpha(BRAND_BACKGROUND.getAsInt(), var5));
            var6 = 1.0F - Mth.clamp(var3 - 1.0F, 0.0F, 1.0F);
        } else if (this.fadeIn) {
            if (this.minecraft.screen != null && var4 < 1.0F) {
                this.minecraft.screen.render(param0, param1, param2, param3);
            }

            int var7 = Mth.ceil(Mth.clamp((double)var4, 0.15, 1.0) * 255.0);
            fill(param0, 0, 0, var0, var1, replaceAlpha(BRAND_BACKGROUND.getAsInt(), var7));
            var6 = Mth.clamp(var4, 0.0F, 1.0F);
        } else {
            int var9 = BRAND_BACKGROUND.getAsInt();
            float var10 = (float)(var9 >> 16 & 0xFF) / 255.0F;
            float var11 = (float)(var9 >> 8 & 0xFF) / 255.0F;
            float var12 = (float)(var9 & 0xFF) / 255.0F;
            GlStateManager._clearColor(var10, var11, var12, 1.0F);
            GlStateManager._clear(16384, Minecraft.ON_OSX);
            var6 = 1.0F;
        }

        int var14 = (int)((double)this.minecraft.getWindow().getGuiScaledWidth() * 0.5);
        int var15 = (int)((double)this.minecraft.getWindow().getGuiScaledHeight() * 0.5);
        double var16 = Math.min((double)this.minecraft.getWindow().getGuiScaledWidth() * 0.75, (double)this.minecraft.getWindow().getGuiScaledHeight()) * 0.25;
        int var17 = (int)(var16 * 0.5);
        double var18 = var16 * 4.0;
        int var19 = (int)(var18 * 0.5);
        RenderSystem.setShaderTexture(0, MOJANG_STUDIOS_LOGO_LOCATION);
        RenderSystem.enableBlend();
        RenderSystem.blendEquation(32774);
        RenderSystem.blendFunc(770, 1);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, var6);
        blit(param0, var14 - var19, var15 - var17, var19, (int)var16, -0.0625F, 0.0F, 120, 60, 120, 120);
        blit(param0, var14, var15 - var17, var19, (int)var16, 0.0625F, 60.0F, 120, 60, 120, 120);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        int var20 = (int)((double)this.minecraft.getWindow().getGuiScaledHeight() * 0.8325);
        float var21 = this.reload.getActualProgress();
        this.currentProgress = Mth.clamp(this.currentProgress * 0.95F + var21 * 0.050000012F, 0.0F, 1.0F);
        if (var3 < 1.0F) {
            this.drawProgressBar(param0, var0 / 2 - var19, var20 - 5, var0 / 2 + var19, var20 + 5, 1.0F - Mth.clamp(var3, 0.0F, 1.0F));
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
        fill(param0, param1 + 2, param2 + 2, param1 + var0, param4 - 2, var2);
        fill(param0, param1 + 1, param2, param3 - 1, param2 + 1, var2);
        fill(param0, param1 + 1, param4, param3 - 1, param4 - 1, var2);
        fill(param0, param1, param2, param1 + 1, param4, var2);
        fill(param0, param3, param2, param3 - 1, param4, var2);
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

            try {
                SimpleTexture.TextureImage var5;
                try (InputStream var2 = var1.getResource(PackType.CLIENT_RESOURCES, LoadingOverlay.MOJANG_STUDIOS_LOGO_LOCATION)) {
                    var5 = new SimpleTexture.TextureImage(new TextureMetadataSection(true, true), NativeImage.read(var2));
                }

                return var5;
            } catch (IOException var9) {
                return new SimpleTexture.TextureImage(var9);
            }
        }
    }
}
