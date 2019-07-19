package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nullable;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.HttpTextureProcessor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class HttpTexture extends SimpleTexture {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final AtomicInteger UNIQUE_THREAD_ID = new AtomicInteger(0);
    @Nullable
    private final File file;
    private final String urlString;
    @Nullable
    private final HttpTextureProcessor processor;
    @Nullable
    private Thread thread;
    private volatile boolean uploaded;

    public HttpTexture(@Nullable File param0, String param1, ResourceLocation param2, @Nullable HttpTextureProcessor param3) {
        super(param2);
        this.file = param0;
        this.urlString = param1;
        this.processor = param3;
    }

    private void uploadImage(NativeImage param0) {
        TextureUtil.prepareImage(this.getId(), param0.getWidth(), param0.getHeight());
        param0.upload(0, 0, 0, false);
    }

    public void loadCallback(NativeImage param0) {
        if (this.processor != null) {
            this.processor.onTextureDownloaded();
        }

        synchronized(this) {
            this.uploadImage(param0);
            this.uploaded = true;
        }
    }

    @Override
    public void load(ResourceManager param0) throws IOException {
        if (!this.uploaded) {
            synchronized(this) {
                super.load(param0);
                this.uploaded = true;
            }
        }

        if (this.thread == null) {
            if (this.file != null && this.file.isFile()) {
                LOGGER.debug("Loading http texture from local cache ({})", this.file);
                NativeImage var0 = null;

                try {
                    try {
                        var0 = NativeImage.read(new FileInputStream(this.file));
                        if (this.processor != null) {
                            var0 = this.processor.process(var0);
                        }

                        this.loadCallback(var0);
                    } catch (IOException var8) {
                        LOGGER.error("Couldn't load skin {}", this.file, var8);
                        this.startDownloadThread();
                    }

                } finally {
                    if (var0 != null) {
                        var0.close();
                    }

                }
            } else {
                this.startDownloadThread();
            }
        }
    }

    protected void startDownloadThread() {
        this.thread = new Thread("Texture Downloader #" + UNIQUE_THREAD_ID.incrementAndGet()) {
            @Override
            public void run() {
                HttpURLConnection var0 = null;
                HttpTexture.LOGGER.debug("Downloading http texture from {} to {}", HttpTexture.this.urlString, HttpTexture.this.file);

                try {
                    var0 = (HttpURLConnection)new URL(HttpTexture.this.urlString).openConnection(Minecraft.getInstance().getProxy());
                    var0.setDoInput(true);
                    var0.setDoOutput(false);
                    var0.connect();
                    if (var0.getResponseCode() / 100 == 2) {
                        InputStream var1;
                        if (HttpTexture.this.file != null) {
                            FileUtils.copyInputStreamToFile(var0.getInputStream(), HttpTexture.this.file);
                            var1 = new FileInputStream(HttpTexture.this.file);
                        } else {
                            var1 = var0.getInputStream();
                        }

                        Minecraft.getInstance().execute(() -> {
                            NativeImage var0x = null;

                            try {
                                var0x = NativeImage.read(var1);
                                if (HttpTexture.this.processor != null) {
                                    var0x = HttpTexture.this.processor.process(var0x);
                                }

                                HttpTexture.this.loadCallback(var0x);
                            } catch (IOException var7x) {
                                HttpTexture.LOGGER.warn("Error while loading the skin texture", (Throwable)var7x);
                            } finally {
                                if (var0x != null) {
                                    var0x.close();
                                }

                                IOUtils.closeQuietly(var1);
                            }

                        });
                        return;
                    }
                } catch (Exception var6) {
                    HttpTexture.LOGGER.error("Couldn't download http texture", (Throwable)var6);
                    return;
                } finally {
                    if (var0 != null) {
                        var0.disconnect();
                    }

                }

            }
        };
        this.thread.setDaemon(true);
        this.thread.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
        this.thread.start();
    }
}
