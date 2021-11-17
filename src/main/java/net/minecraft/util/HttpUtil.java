package net.minecraft.util;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.ServerSocket;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import javax.annotation.Nullable;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.network.chat.TranslatableComponent;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HttpUtil {
    private static final Logger LOGGER = LogManager.getLogger();
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

    public static String buildQuery(Map<String, Object> param0) {
        StringBuilder var0 = new StringBuilder();

        for(Entry<String, Object> var1 : param0.entrySet()) {
            if (var0.length() > 0) {
                var0.append('&');
            }

            try {
                var0.append(URLEncoder.encode(var1.getKey(), "UTF-8"));
            } catch (UnsupportedEncodingException var6) {
                var6.printStackTrace();
            }

            if (var1.getValue() != null) {
                var0.append('=');

                try {
                    var0.append(URLEncoder.encode(var1.getValue().toString(), "UTF-8"));
                } catch (UnsupportedEncodingException var5) {
                    var5.printStackTrace();
                }
            }
        }

        return var0.toString();
    }

    public static String performPost(URL param0, Map<String, Object> param1, boolean param2, @Nullable Proxy param3) {
        return performPost(param0, buildQuery(param1), param2, param3);
    }

    private static String performPost(URL param0, String param1, boolean param2, @Nullable Proxy param3) {
        try {
            if (param3 == null) {
                param3 = Proxy.NO_PROXY;
            }

            HttpURLConnection var0 = (HttpURLConnection)param0.openConnection(param3);
            var0.setRequestMethod("POST");
            var0.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            var0.setRequestProperty("Content-Length", param1.getBytes().length + "");
            var0.setRequestProperty("Content-Language", "en-US");
            var0.setUseCaches(false);
            var0.setDoInput(true);
            var0.setDoOutput(true);
            DataOutputStream var1 = new DataOutputStream(var0.getOutputStream());
            var1.writeBytes(param1);
            var1.flush();
            var1.close();
            BufferedReader var2 = new BufferedReader(new InputStreamReader(var0.getInputStream()));
            StringBuilder var3 = new StringBuilder();

            String var4;
            while((var4 = var2.readLine()) != null) {
                var3.append(var4);
                var3.append('\r');
            }

            var2.close();
            return var3.toString();
        } catch (Exception var9) {
            if (!param2) {
                LOGGER.error("Could not post to {}", param0, var9);
            }

            return "";
        }
    }

    public static CompletableFuture<?> downloadTo(
        File param0, String param1, Map<String, String> param2, int param3, @Nullable ProgressListener param4, Proxy param5
    ) {
        return CompletableFuture.supplyAsync(() -> {
            HttpURLConnection var0x = null;
            InputStream var1x = null;
            OutputStream var2x = null;
            if (param4 != null) {
                param4.progressStart(new TranslatableComponent("resourcepack.downloading"));
                param4.progressStage(new TranslatableComponent("resourcepack.requesting"));
            }

            try {
                try {
                    byte[] var3 = new byte[4096];
                    URL var4 = new URL(param1);
                    var0x = (HttpURLConnection)var4.openConnection(param5);
                    var0x.setInstanceFollowRedirects(true);
                    float var5 = 0.0F;
                    float var6 = (float)param2.entrySet().size();

                    for(Entry<String, String> var7 : param2.entrySet()) {
                        var0x.setRequestProperty(var7.getKey(), var7.getValue());
                        if (param4 != null) {
                            param4.progressStagePercentage((int)(++var5 / var6 * 100.0F));
                        }
                    }

                    var1x = var0x.getInputStream();
                    var6 = (float)var0x.getContentLength();
                    int var8 = var0x.getContentLength();
                    if (param4 != null) {
                        param4.progressStage(new TranslatableComponent("resourcepack.progress", String.format(Locale.ROOT, "%.2f", var6 / 1000.0F / 1000.0F)));
                    }

                    if (param0.exists()) {
                        long var9 = param0.length();
                        if (var9 == (long)var8) {
                            if (param4 != null) {
                                param4.stop();
                            }

                            return null;
                        }

                        LOGGER.warn("Deleting {} as it does not match what we currently have ({} vs our {}).", param0, var8, var9);
                        FileUtils.deleteQuietly(param0);
                    } else if (param0.getParentFile() != null) {
                        param0.getParentFile().mkdirs();
                    }

                    var2x = new DataOutputStream(new FileOutputStream(param0));
                    if (param3 > 0 && var6 > (float)param3) {
                        if (param4 != null) {
                            param4.stop();
                        }

                        throw new IOException("Filesize is bigger than maximum allowed (file is " + var5 + ", limit is " + param3 + ")");
                    }

                    int var10;
                    while((var10 = var1x.read(var3)) >= 0) {
                        var5 += (float)var10;
                        if (param4 != null) {
                            param4.progressStagePercentage((int)(var5 / var6 * 100.0F));
                        }

                        if (param3 > 0 && var5 > (float)param3) {
                            if (param4 != null) {
                                param4.stop();
                            }

                            throw new IOException("Filesize was bigger than maximum allowed (got >= " + var5 + ", limit was " + param3 + ")");
                        }

                        if (Thread.interrupted()) {
                            LOGGER.error("INTERRUPTED");
                            if (param4 != null) {
                                param4.stop();
                            }

                            return null;
                        }

                        var2x.write(var3, 0, var10);
                    }

                    if (param4 != null) {
                        param4.stop();
                        return null;
                    }
                } catch (Throwable var22) {
                    var22.printStackTrace();
                    if (var0x != null) {
                        InputStream var12 = var0x.getErrorStream();

                        try {
                            LOGGER.error(IOUtils.toString(var12));
                        } catch (IOException var21) {
                            var21.printStackTrace();
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
