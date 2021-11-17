package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class HttpTexture extends SimpleTexture {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final int SKIN_WIDTH = 64;
    private static final int SKIN_HEIGHT = 64;
    private static final int LEGACY_SKIN_HEIGHT = 32;
    @Nullable
    private final File file;
    private final String urlString;
    private final boolean processLegacySkin;
    @Nullable
    private final Runnable onDownloaded;
    @Nullable
    private CompletableFuture<?> future;
    private boolean uploaded;

    public HttpTexture(@Nullable File param0, String param1, ResourceLocation param2, boolean param3, @Nullable Runnable param4) {
        super(param2);
        this.file = param0;
        this.urlString = param1;
        this.processLegacySkin = param3;
        this.onDownloaded = param4;
    }

    private void loadCallback(NativeImage param0) {
        if (this.onDownloaded != null) {
            this.onDownloaded.run();
        }

        Minecraft.getInstance().execute(() -> {
            this.uploaded = true;
            if (!RenderSystem.isOnRenderThread()) {
                RenderSystem.recordRenderCall(() -> this.upload(param0));
            } else {
                this.upload(param0);
            }

        });
    }

    private void upload(NativeImage param0) {
        TextureUtil.prepareImage(this.getId(), param0.getWidth(), param0.getHeight());
        param0.upload(0, 0, 0, true);
    }

    @Override
    public void load(ResourceManager param0) throws IOException {
        Minecraft.getInstance().execute(() -> {
            if (!this.uploaded) {
                try {
                    super.load(param0);
                } catch (IOException var3x) {
                    LOGGER.warn("Failed to load texture: {}", this.location, var3x);
                }

                this.uploaded = true;
            }

        });
        if (this.future == null) {
            NativeImage var1;
            if (this.file != null && this.file.isFile()) {
                LOGGER.debug("Loading http texture from local cache ({})", this.file);
                FileInputStream var0 = new FileInputStream(this.file);
                var1 = this.load(var0);
            } else {
                var1 = null;
            }

            if (var1 != null) {
                this.loadCallback(var1);
            } else {
                this.future = CompletableFuture.runAsync(() -> {
                    HttpURLConnection var0x = null;
                    LOGGER.debug("Downloading http texture from {} to {}", this.urlString, this.file);

                    try {
                        var0x = (HttpURLConnection)new URL(this.urlString).openConnection(Minecraft.getInstance().getProxy());
                        var0x.setDoInput(true);
                        var0x.setDoOutput(false);
                        var0x.connect();
                        if (var0x.getResponseCode() / 100 == 2) {
                            Object var3x;
                            if (this.file != null) {
                                FileUtils.copyInputStreamToFile(var0x.getInputStream(), this.file);
                                var3x = new FileInputStream(this.file);
                            } else {
                                var3x = var0x.getInputStream();
                            }

                            Minecraft.getInstance().execute(() -> {
                                NativeImage var0xx = this.load(var3x);
                                if (var0xx != null) {
                                    this.loadCallback(var0xx);
                                }

                            });
                            return;
                        }
                    } catch (Exception var6) {
                        LOGGER.error("Couldn't download http texture", (Throwable)var6);
                        return;
                    } finally {
                        if (var0x != null) {
                            var0x.disconnect();
                        }

                    }

                }, Util.backgroundExecutor());
            }
        }
    }

    @Nullable
    private NativeImage load(InputStream param0) {
        NativeImage var0 = null;

        try {
            var0 = NativeImage.read(param0);
            if (this.processLegacySkin) {
                var0 = this.processLegacySkin(var0);
            }
        } catch (Exception var4) {
            LOGGER.warn("Error while loading the skin texture", (Throwable)var4);
        }

        return var0;
    }

    @Nullable
    private NativeImage processLegacySkin(NativeImage param0) {
        int var0 = param0.getHeight();
        int var1 = param0.getWidth();
        if (var1 == 64 && (var0 == 32 || var0 == 64)) {
            boolean var2 = var0 == 32;
            if (var2) {
                NativeImage var3 = new NativeImage(64, 64, true);
                var3.copyFrom(param0);
                param0.close();
                param0 = var3;
                var3.fillRect(0, 32, 64, 32, 0);
                var3.copyRect(4, 16, 16, 32, 4, 4, true, false);
                var3.copyRect(8, 16, 16, 32, 4, 4, true, false);
                var3.copyRect(0, 20, 24, 32, 4, 12, true, false);
                var3.copyRect(4, 20, 16, 32, 4, 12, true, false);
                var3.copyRect(8, 20, 8, 32, 4, 12, true, false);
                var3.copyRect(12, 20, 16, 32, 4, 12, true, false);
                var3.copyRect(44, 16, -8, 32, 4, 4, true, false);
                var3.copyRect(48, 16, -8, 32, 4, 4, true, false);
                var3.copyRect(40, 20, 0, 32, 4, 12, true, false);
                var3.copyRect(44, 20, -8, 32, 4, 12, true, false);
                var3.copyRect(48, 20, -16, 32, 4, 12, true, false);
                var3.copyRect(52, 20, -8, 32, 4, 12, true, false);
            }

            setNoAlpha(param0, 0, 0, 32, 16);
            if (var2) {
                doNotchTransparencyHack(param0, 32, 0, 64, 32);
            }

            setNoAlpha(param0, 0, 16, 64, 32);
            setNoAlpha(param0, 16, 48, 48, 64);
            return param0;
        } else {
            param0.close();
            LOGGER.warn("Discarding incorrectly sized ({}x{}) skin texture from {}", var1, var0, this.urlString);
            return null;
        }
    }

    private static void doNotchTransparencyHack(NativeImage param0, int param1, int param2, int param3, int param4) {
        for(int var0 = param1; var0 < param3; ++var0) {
            for(int var1 = param2; var1 < param4; ++var1) {
                int var2 = param0.getPixelRGBA(var0, var1);
                if ((var2 >> 24 & 0xFF) < 128) {
                    return;
                }
            }
        }

        for(int var3 = param1; var3 < param3; ++var3) {
            for(int var4 = param2; var4 < param4; ++var4) {
                param0.setPixelRGBA(var3, var4, param0.getPixelRGBA(var3, var4) & 16777215);
            }
        }

    }

    private static void setNoAlpha(NativeImage param0, int param1, int param2, int param3, int param4) {
        for(int var0 = param1; var0 < param3; ++var0) {
            for(int var1 = param2; var1 < param4; ++var1) {
                param0.setPixelRGBA(var0, var1, param0.getPixelRGBA(var0, var1) | 0xFF000000);
            }
        }

    }
}
