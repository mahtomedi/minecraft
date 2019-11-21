package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.VanillaPack;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LoadingOverlay extends Overlay {
    private static final ResourceLocation MOJANG_LOGO_LOCATION = new ResourceLocation("textures/gui/title/mojang.png");
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
        param0.getTextureManager().register(MOJANG_LOGO_LOCATION, new LoadingOverlay.LogoTexture());
    }

    @Override
    public void render(int param0, int param1, float param2) {
        int var0 = this.minecraft.getWindow().getGuiScaledWidth();
        int var1 = this.minecraft.getWindow().getGuiScaledHeight();
        long var2 = Util.getMillis();
        if (this.fadeIn && (this.reload.isApplying() || this.minecraft.screen != null) && this.fadeInStart == -1L) {
            this.fadeInStart = var2;
        }

        float var3 = this.fadeOutStart > -1L ? (float)(var2 - this.fadeOutStart) / 1000.0F : -1.0F;
        float var4 = this.fadeInStart > -1L ? (float)(var2 - this.fadeInStart) / 500.0F : -1.0F;
        float var6;
        if (var3 >= 1.0F) {
            if (this.minecraft.screen != null) {
                this.minecraft.screen.render(0, 0, param2);
            }

            int var5 = Mth.ceil((1.0F - Mth.clamp(var3 - 1.0F, 0.0F, 1.0F)) * 255.0F);
            fill(0, 0, var0, var1, 16777215 | var5 << 24);
            var6 = 1.0F - Mth.clamp(var3 - 1.0F, 0.0F, 1.0F);
        } else if (this.fadeIn) {
            if (this.minecraft.screen != null && var4 < 1.0F) {
                this.minecraft.screen.render(param0, param1, param2);
            }

            int var7 = Mth.ceil(Mth.clamp((double)var4, 0.15, 1.0) * 255.0);
            fill(0, 0, var0, var1, 16777215 | var7 << 24);
            var6 = Mth.clamp(var4, 0.0F, 1.0F);
        } else {
            fill(0, 0, var0, var1, -1);
            var6 = 1.0F;
        }

        int var10 = (this.minecraft.getWindow().getGuiScaledWidth() - 256) / 2;
        int var11 = (this.minecraft.getWindow().getGuiScaledHeight() - 256) / 2;
        this.minecraft.getTextureManager().bind(MOJANG_LOGO_LOCATION);
        RenderSystem.enableBlend();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, var6);
        this.blit(var10, var11, 0, 0, 256, 256);
        float var12 = this.reload.getActualProgress();
        this.currentProgress = Mth.clamp(this.currentProgress * 0.95F + var12 * 0.050000012F, 0.0F, 1.0F);
        if (var3 < 1.0F) {
            this.drawProgressBar(var0 / 2 - 150, var1 / 4 * 3, var0 / 2 + 150, var1 / 4 * 3 + 10, 1.0F - Mth.clamp(var3, 0.0F, 1.0F));
        }

        if (var3 >= 2.0F) {
            this.minecraft.setOverlay(null);
        }

        if (this.fadeOutStart == -1L && this.reload.isDone() && (!this.fadeIn || var4 >= 2.0F)) {
            try {
                this.reload.checkExceptions();
                this.onFinish.accept(Optional.empty());
            } catch (Throwable var15) {
                this.onFinish.accept(Optional.of(var15));
            }

            this.fadeOutStart = Util.getMillis();
            if (this.minecraft.screen != null) {
                this.minecraft.screen.init(this.minecraft, this.minecraft.getWindow().getGuiScaledWidth(), this.minecraft.getWindow().getGuiScaledHeight());
            }
        }

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

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    static class LogoTexture extends SimpleTexture {
        public LogoTexture() {
            super(LoadingOverlay.MOJANG_LOGO_LOCATION);
        }

        @Override
        protected SimpleTexture.TextureImage getTextureImage(ResourceManager param0) {
            Minecraft var0 = Minecraft.getInstance();
            VanillaPack var1 = var0.getClientPackSource().getVanillaPack();

            try (InputStream var2 = var1.getResource(PackType.CLIENT_RESOURCES, LoadingOverlay.MOJANG_LOGO_LOCATION)) {
                return new SimpleTexture.TextureImage(null, NativeImage.read(var2));
            } catch (IOException var18) {
                return new SimpleTexture.TextureImage(var18);
            }
        }
    }
}
