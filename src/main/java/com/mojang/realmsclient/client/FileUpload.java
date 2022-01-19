package com.mojang.realmsclient.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.dto.UploadInfo;
import com.mojang.realmsclient.gui.screens.UploadResult;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.client.User;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.Args;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class FileUpload {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_RETRIES = 5;
    private static final String UPLOAD_PATH = "/upload";
    private final File file;
    private final long worldId;
    private final int slotId;
    private final UploadInfo uploadInfo;
    private final String sessionId;
    private final String username;
    private final String clientVersion;
    private final UploadStatus uploadStatus;
    private final AtomicBoolean cancelled = new AtomicBoolean(false);
    @Nullable
    private CompletableFuture<UploadResult> uploadTask;
    private final RequestConfig requestConfig = RequestConfig.custom()
        .setSocketTimeout((int)TimeUnit.MINUTES.toMillis(10L))
        .setConnectTimeout((int)TimeUnit.SECONDS.toMillis(15L))
        .build();

    public FileUpload(File param0, long param1, int param2, UploadInfo param3, User param4, String param5, UploadStatus param6) {
        this.file = param0;
        this.worldId = param1;
        this.slotId = param2;
        this.uploadInfo = param3;
        this.sessionId = param4.getSessionId();
        this.username = param4.getName();
        this.clientVersion = param5;
        this.uploadStatus = param6;
    }

    public void upload(Consumer<UploadResult> param0) {
        if (this.uploadTask == null) {
            this.uploadTask = CompletableFuture.supplyAsync(() -> this.requestUpload(0));
            this.uploadTask.thenAccept(param0);
        }
    }

    public void cancel() {
        this.cancelled.set(true);
        if (this.uploadTask != null) {
            this.uploadTask.cancel(false);
            this.uploadTask = null;
        }

    }

    private UploadResult requestUpload(int param0) {
        UploadResult.Builder var0 = new UploadResult.Builder();
        if (this.cancelled.get()) {
            return var0.build();
        } else {
            this.uploadStatus.totalBytes = this.file.length();
            HttpPost var1 = new HttpPost(this.uploadInfo.getUploadEndpoint().resolve("/upload/" + this.worldId + "/" + this.slotId));
            CloseableHttpClient var2 = HttpClientBuilder.create().setDefaultRequestConfig(this.requestConfig).build();

            UploadResult var8;
            try {
                this.setupRequest(var1);
                HttpResponse var3 = var2.execute(var1);
                long var4 = this.getRetryDelaySeconds(var3);
                if (!this.shouldRetry(var4, param0)) {
                    this.handleResponse(var3, var0);
                    return var0.build();
                }

                var8 = this.retryUploadAfter(var4, param0);
            } catch (Exception var12) {
                if (!this.cancelled.get()) {
                    LOGGER.error("Caught exception while uploading: ", (Throwable)var12);
                }

                return var0.build();
            } finally {
                this.cleanup(var1, var2);
            }

            return var8;
        }
    }

    private void cleanup(HttpPost param0, @Nullable CloseableHttpClient param1) {
        param0.releaseConnection();
        if (param1 != null) {
            try {
                param1.close();
            } catch (IOException var4) {
                LOGGER.error("Failed to close Realms upload client");
            }
        }

    }

    private void setupRequest(HttpPost param0) throws FileNotFoundException {
        param0.setHeader(
            "Cookie", "sid=" + this.sessionId + ";token=" + this.uploadInfo.getToken() + ";user=" + this.username + ";version=" + this.clientVersion
        );
        FileUpload.CustomInputStreamEntity var0 = new FileUpload.CustomInputStreamEntity(new FileInputStream(this.file), this.file.length(), this.uploadStatus);
        var0.setContentType("application/octet-stream");
        param0.setEntity(var0);
    }

    private void handleResponse(HttpResponse param0, UploadResult.Builder param1) throws IOException {
        int var0 = param0.getStatusLine().getStatusCode();
        if (var0 == 401) {
            LOGGER.debug("Realms server returned 401: {}", param0.getFirstHeader("WWW-Authenticate"));
        }

        param1.withStatusCode(var0);
        if (param0.getEntity() != null) {
            String var1 = EntityUtils.toString(param0.getEntity(), "UTF-8");
            if (var1 != null) {
                try {
                    JsonParser var2 = new JsonParser();
                    JsonElement var3 = var2.parse(var1).getAsJsonObject().get("errorMsg");
                    Optional<String> var4 = Optional.ofNullable(var3).map(JsonElement::getAsString);
                    param1.withErrorMessage(var4.orElse(null));
                } catch (Exception var8) {
                }
            }
        }

    }

    private boolean shouldRetry(long param0, int param1) {
        return param0 > 0L && param1 + 1 < 5;
    }

    private UploadResult retryUploadAfter(long param0, int param1) throws InterruptedException {
        Thread.sleep(Duration.ofSeconds(param0).toMillis());
        return this.requestUpload(param1 + 1);
    }

    private long getRetryDelaySeconds(HttpResponse param0) {
        return Optional.ofNullable(param0.getFirstHeader("Retry-After")).map(NameValuePair::getValue).map(Long::valueOf).orElse(0L);
    }

    public boolean isFinished() {
        return this.uploadTask.isDone() || this.uploadTask.isCancelled();
    }

    @OnlyIn(Dist.CLIENT)
    static class CustomInputStreamEntity extends InputStreamEntity {
        private final long length;
        private final InputStream content;
        private final UploadStatus uploadStatus;

        public CustomInputStreamEntity(InputStream param0, long param1, UploadStatus param2) {
            super(param0);
            this.content = param0;
            this.length = param1;
            this.uploadStatus = param2;
        }

        @Override
        public void writeTo(OutputStream param0) throws IOException {
            Args.notNull(param0, "Output stream");
            InputStream var0 = this.content;

            try {
                byte[] var1 = new byte[4096];
                int var2;
                if (this.length < 0L) {
                    while((var2 = var0.read(var1)) != -1) {
                        param0.write(var1, 0, var2);
                        this.uploadStatus.bytesWritten += (long)var2;
                    }
                } else {
                    long var3 = this.length;

                    while(var3 > 0L) {
                        var2 = var0.read(var1, 0, (int)Math.min(4096L, var3));
                        if (var2 == -1) {
                            break;
                        }

                        param0.write(var1, 0, var2);
                        this.uploadStatus.bytesWritten += (long)var2;
                        var3 -= (long)var2;
                        param0.flush();
                    }
                }
            } finally {
                var0.close();
            }

        }
    }
}
