package net.minecraft.server.network;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.mojang.authlib.GameProfile;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.thread.ProcessorMailbox;
import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TextFilterClient implements AutoCloseable {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final AtomicInteger WORKER_COUNT = new AtomicInteger(1);
    private static final ThreadFactory THREAD_FACTORY = param0 -> {
        Thread var0 = new Thread(param0);
        var0.setName("Chat-Filter-Worker-" + WORKER_COUNT.getAndIncrement());
        return var0;
    };
    private final URL chatEndpoint;
    private final URL joinEndpoint;
    private final URL leaveEndpoint;
    private final String authKey;
    private final int ruleId;
    private final String serverId;
    private final TextFilterClient.IgnoreStrategy chatIgnoreStrategy;
    private final ExecutorService workerPool;

    private TextFilterClient(URI param0, String param1, int param2, String param3, TextFilterClient.IgnoreStrategy param4, int param5) throws MalformedURLException {
        this.authKey = param1;
        this.ruleId = param2;
        this.serverId = param3;
        this.chatIgnoreStrategy = param4;
        this.chatEndpoint = param0.resolve("/v1/chat").toURL();
        this.joinEndpoint = param0.resolve("/v1/join").toURL();
        this.leaveEndpoint = param0.resolve("/v1/leave").toURL();
        this.workerPool = Executors.newFixedThreadPool(param5, THREAD_FACTORY);
    }

    @Nullable
    public static TextFilterClient createFromConfig(String param0) {
        if (Strings.isNullOrEmpty(param0)) {
            return null;
        } else {
            try {
                JsonObject var0 = GsonHelper.parse(param0);
                URI var1 = new URI(GsonHelper.getAsString(var0, "apiServer"));
                String var2 = GsonHelper.getAsString(var0, "apiKey");
                if (var2.isEmpty()) {
                    throw new IllegalArgumentException("Missing API key");
                } else {
                    int var3 = GsonHelper.getAsInt(var0, "ruleId", 1);
                    String var4 = GsonHelper.getAsString(var0, "serverId", "");
                    int var5 = GsonHelper.getAsInt(var0, "hashesToDrop", -1);
                    int var6 = GsonHelper.getAsInt(var0, "maxConcurrentRequests", 7);
                    TextFilterClient.IgnoreStrategy var7 = TextFilterClient.IgnoreStrategy.select(var5);
                    return new TextFilterClient(var1, new Base64().encodeToString(var2.getBytes(StandardCharsets.US_ASCII)), var3, var4, var7, var6);
                }
            } catch (Exception var9) {
                LOGGER.warn("Failed to parse chat filter config {}", param0, var9);
                return null;
            }
        }
    }

    private void processJoinOrLeave(GameProfile param0, URL param1, Executor param2) {
        JsonObject var0 = new JsonObject();
        var0.addProperty("server", this.serverId);
        var0.addProperty("room", "Chat");
        var0.addProperty("user_id", param0.getId().toString());
        var0.addProperty("user_display_name", param0.getName());
        param2.execute(() -> {
            try {
                this.processRequest(var0, param1);
            } catch (Exception var5) {
                LOGGER.warn("Failed to send join/leave packet to {} for player {}", param1, param0, var5);
            }

        });
    }

    private CompletableFuture<Optional<String>> requestMessageProcessing(
        GameProfile param0, String param1, TextFilterClient.IgnoreStrategy param2, Executor param3
    ) {
        if (param1.isEmpty()) {
            return CompletableFuture.completedFuture(Optional.of(""));
        } else {
            JsonObject var0 = new JsonObject();
            var0.addProperty("rule", this.ruleId);
            var0.addProperty("server", this.serverId);
            var0.addProperty("room", "Chat");
            var0.addProperty("player", param0.getId().toString());
            var0.addProperty("player_display_name", param0.getName());
            var0.addProperty("text", param1);
            return CompletableFuture.supplyAsync(() -> {
                try {
                    JsonObject var4x = this.processRequestResponse(var0, this.chatEndpoint);
                    boolean var1x = GsonHelper.getAsBoolean(var4x, "response", false);
                    if (var1x) {
                        return Optional.of(param1);
                    } else {
                        String var2x = GsonHelper.getAsString(var4x, "hashed", null);
                        if (var2x == null) {
                            return Optional.empty();
                        } else {
                            int var3x = GsonHelper.getAsJsonArray(var4x, "hashes").size();
                            return param2.shouldIgnore(var2x, var3x) ? Optional.empty() : Optional.of(var2x);
                        }
                    }
                } catch (Exception var8) {
                    LOGGER.warn("Failed to validate message '{}'", param1, var8);
                    return Optional.empty();
                }
            }, param3);
        }
    }

    @Override
    public void close() {
        this.workerPool.shutdownNow();
    }

    private void drainStream(InputStream param0) throws IOException {
        byte[] var0 = new byte[1024];

        while(param0.read(var0) != -1) {
        }

    }

    private JsonObject processRequestResponse(JsonObject param0, URL param1) throws IOException {
        HttpURLConnection var0 = this.makeRequest(param0, param1);

        JsonObject var6;
        try (InputStream var1 = var0.getInputStream()) {
            if (var0.getResponseCode() != 204) {
                try {
                    return Streams.parse(new JsonReader(new InputStreamReader(var1))).getAsJsonObject();
                } finally {
                    this.drainStream(var1);
                }
            }

            var6 = new JsonObject();
        }

        return var6;
    }

    private void processRequest(JsonObject param0, URL param1) throws IOException {
        HttpURLConnection var0 = this.makeRequest(param0, param1);

        try (InputStream var1 = var0.getInputStream()) {
            this.drainStream(var1);
        }

    }

    private HttpURLConnection makeRequest(JsonObject param0, URL param1) throws IOException {
        HttpURLConnection var0 = (HttpURLConnection)param1.openConnection();
        var0.setConnectTimeout(15000);
        var0.setReadTimeout(2000);
        var0.setUseCaches(false);
        var0.setDoOutput(true);
        var0.setDoInput(true);
        var0.setRequestMethod("POST");
        var0.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        var0.setRequestProperty("Accept", "application/json");
        var0.setRequestProperty("Authorization", "Basic " + this.authKey);
        var0.setRequestProperty("User-Agent", "Minecraft server" + SharedConstants.getCurrentVersion().getName());

        try (
            OutputStreamWriter var1 = new OutputStreamWriter(var0.getOutputStream(), StandardCharsets.UTF_8);
            JsonWriter var2 = new JsonWriter(var1);
        ) {
            Streams.write(param0, var2);
        }

        int var3 = var0.getResponseCode();
        if (var3 >= 200 && var3 < 300) {
            return var0;
        } else {
            throw new TextFilterClient.RequestFailedException(var3 + " " + var0.getResponseMessage());
        }
    }

    public TextFilter createContext(GameProfile param0) {
        return new TextFilterClient.PlayerContext(param0);
    }

    @FunctionalInterface
    public interface IgnoreStrategy {
        TextFilterClient.IgnoreStrategy NEVER_IGNORE = (param0, param1) -> false;
        TextFilterClient.IgnoreStrategy IGNORE_FULLY_FILTERED = (param0, param1) -> param0.length() == param1;

        static TextFilterClient.IgnoreStrategy ignoreOverThreshold(int param0) {
            return (param1, param2) -> param2 >= param0;
        }

        static TextFilterClient.IgnoreStrategy select(int param0) {
            switch(param0) {
                case -1:
                    return NEVER_IGNORE;
                case 0:
                    return IGNORE_FULLY_FILTERED;
                default:
                    return ignoreOverThreshold(param0);
            }
        }

        boolean shouldIgnore(String var1, int var2);
    }

    class PlayerContext implements TextFilter {
        private final GameProfile profile;
        private final Executor streamExecutor;

        private PlayerContext(GameProfile param0) {
            this.profile = param0;
            ProcessorMailbox<Runnable> param1 = ProcessorMailbox.create(TextFilterClient.this.workerPool, "chat stream for " + param0.getName());
            this.streamExecutor = param1::tell;
        }

        @Override
        public void join() {
            TextFilterClient.this.processJoinOrLeave(this.profile, TextFilterClient.this.joinEndpoint, this.streamExecutor);
        }

        @Override
        public void leave() {
            TextFilterClient.this.processJoinOrLeave(this.profile, TextFilterClient.this.leaveEndpoint, this.streamExecutor);
        }

        @Override
        public CompletableFuture<Optional<List<String>>> processMessageBundle(List<String> param0) {
            List<CompletableFuture<Optional<String>>> var0 = param0.stream()
                .map(
                    param0x -> TextFilterClient.this.requestMessageProcessing(
                            this.profile, param0x, TextFilterClient.this.chatIgnoreStrategy, this.streamExecutor
                        )
                )
                .collect(ImmutableList.toImmutableList());
            return Util.sequence(var0)
                .thenApply(param0x -> Optional.of(param0x.stream().map(param0xx -> param0xx.orElse("")).collect(ImmutableList.toImmutableList())))
                .exceptionally(param0x -> Optional.empty());
        }

        @Override
        public CompletableFuture<Optional<String>> processStreamMessage(String param0) {
            return TextFilterClient.this.requestMessageProcessing(this.profile, param0, TextFilterClient.this.chatIgnoreStrategy, this.streamExecutor);
        }
    }

    public static class RequestFailedException extends RuntimeException {
        private RequestFailedException(String param0) {
            super(param0);
        }
    }
}
