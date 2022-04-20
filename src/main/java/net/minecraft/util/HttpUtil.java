package net.minecraft.util;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.logging.LogUtils;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.ServerSocket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import javax.annotation.Nullable;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.network.chat.Component;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

public class HttpUtil {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final ListeningExecutorService DOWNLOAD_EXECUTOR = MoreExecutors.listeningDecorator(
        Executors.newCachedThreadPool(
            new ThreadFactoryBuilder()
                .setDaemon(true)
                .setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER))
                .setNameFormat("Downloader %d")
                .build()
        )
    );

    private HttpUtil() {
    }

    public static CompletableFuture<?> downloadTo(
        File param0, URL param1, Map<String, String> param2, int param3, @Nullable ProgressListener param4, Proxy param5
    ) {
        return CompletableFuture.supplyAsync(() -> {
            HttpURLConnection var0x = null;
            InputStream var1x = null;
            OutputStream var2x = null;
            if (param4 != null) {
                param4.progressStart(Component.translatable("resourcepack.downloading"));
                param4.progressStage(Component.translatable("resourcepack.requesting"));
            }

            try {
                try {
                    byte[] var3 = new byte[4096];
                    var0x = (HttpURLConnection)param1.openConnection(param5);
                    var0x.setInstanceFollowRedirects(true);
                    float var4 = 0.0F;
                    float var5 = (float)param2.entrySet().size();

                    for(Entry<String, String> var6 : param2.entrySet()) {
                        var0x.setRequestProperty(var6.getKey(), var6.getValue());
                        if (param4 != null) {
                            param4.progressStagePercentage((int)(++var4 / var5 * 100.0F));
                        }
                    }

                    var1x = var0x.getInputStream();
                    var5 = (float)var0x.getContentLength();
                    int var7 = var0x.getContentLength();
                    if (param4 != null) {
                        param4.progressStage(Component.translatable("resourcepack.progress", String.format(Locale.ROOT, "%.2f", var5 / 1000.0F / 1000.0F)));
                    }

                    if (param0.exists()) {
                        long var8 = param0.length();
                        if (var8 == (long)var7) {
                            if (param4 != null) {
                                param4.stop();
                            }

                            return null;
                        }

                        LOGGER.warn("Deleting {} as it does not match what we currently have ({} vs our {}).", param0, var7, var8);
                        FileUtils.deleteQuietly(param0);
                    } else if (param0.getParentFile() != null) {
                        param0.getParentFile().mkdirs();
                    }

                    var2x = new DataOutputStream(new FileOutputStream(param0));
                    if (param3 > 0 && var5 > (float)param3) {
                        if (param4 != null) {
                            param4.stop();
                        }

                        throw new IOException("Filesize is bigger than maximum allowed (file is " + var4 + ", limit is " + param3 + ")");
                    }

                    int var9;
                    while((var9 = var1x.read(var3)) >= 0) {
                        var4 += (float)var9;
                        if (param4 != null) {
                            param4.progressStagePercentage((int)(var4 / var5 * 100.0F));
                        }

                        if (param3 > 0 && var4 > (float)param3) {
                            if (param4 != null) {
                                param4.stop();
                            }

                            throw new IOException("Filesize was bigger than maximum allowed (got >= " + var4 + ", limit was " + param3 + ")");
                        }

                        if (Thread.interrupted()) {
                            LOGGER.error("INTERRUPTED");
                            if (param4 != null) {
                                param4.stop();
                            }

                            return null;
                        }

                        var2x.write(var3, 0, var9);
                    }

                    if (param4 != null) {
                        param4.stop();
                        return null;
                    }
                } catch (Throwable var21) {
                    LOGGER.error("Failed to download file", var21);
                    if (var0x != null) {
                        InputStream var11 = var0x.getErrorStream();

                        try {
                            LOGGER.error("HTTP response error: {}", IOUtils.toString(var11, StandardCharsets.UTF_8));
                        } catch (IOException var20) {
                            LOGGER.error("Failed to read response from server");
                        }
                    }

                    if (param4 != null) {
                        param4.stop();
                        return null;
                    }
                }

                return null;
            } finally {
                IOUtils.closeQuietly(var1x);
                IOUtils.closeQuietly(var2x);
            }
        }, DOWNLOAD_EXECUTOR);
    }

    public static int getAvailablePort() {
        try {
            int var11;
            try (ServerSocket var0 = new ServerSocket(0)) {
                var11 = var0.getLocalPort();
            }

            return var11;
        } catch (IOException var5) {
            return 25564;
        }
    }
}
