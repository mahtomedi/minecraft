package net.minecraft.server.network;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
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
import org.slf4j.Logger;

public class TextFilterClient implements AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final AtomicInteger WORKER_COUNT = new AtomicInteger(1);
    private static final ThreadFactory THREAD_FACTORY = param0 -> {
        Thread var0 = new Thread(param0);
        var0.setName("Chat-Filter-Worker-" + WORKER_COUNT.getAndIncrement());
        return var0;
    };
    private static final String DEFAULT_ENDPOINT = "v1/chat";
    private final URL chatEndpoint;
    private final TextFilterClient.MessageEncoder chatEncoder;
    final URL joinEndpoint;
    final TextFilterClient.JoinOrLeaveEncoder joinEncoder;
    final URL leaveEndpoint;
    final TextFilterClient.JoinOrLeaveEncoder leaveEncoder;
    private final String authKey;
    final TextFilterClient.IgnoreStrategy chatIgnoreStrategy;
    final ExecutorService workerPool;

    private TextFilterClient(
        URL param0,
        TextFilterClient.MessageEncoder param1,
        URL param2,
        TextFilterClient.JoinOrLeaveEncoder param3,
        URL param4,
        TextFilterClient.JoinOrLeaveEncoder param5,
        String param6,
        TextFilterClient.IgnoreStrategy param7,
        int param8
    ) {
        this.authKey = param6;
        this.chatIgnoreStrategy = param7;
        this.chatEndpoint = param0;
        this.chatEncoder = param1;
        this.joinEndpoint = param2;
        this.joinEncoder = param3;
        this.leaveEndpoint = param4;
        this.leaveEncoder = param5;
        this.workerPool = Executors.newFixedThreadPool(param8, THREAD_FACTORY);
    }

    private static URL getEndpoint(URI param0, @Nullable JsonObject param1, String param2, String param3) throws MalformedURLException {
        String var0 = getEndpointFromConfig(param1, param2, param3);
        return param0.resolve("/" + var0).toURL();
    }

    private static String getEndpointFromConfig(@Nullable JsonObject param0, String param1, String param2) {
        return param0 != null ? GsonHelper.getAsString(param0, param1, param2) : param2;
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
                    String var5 = GsonHelper.getAsString(var0, "roomId", "Java:Chat");
                    int var6 = GsonHelper.getAsInt(var0, "hashesToDrop", -1);
                    int var7 = GsonHelper.getAsInt(var0, "maxConcurrentRequests", 7);
                    JsonObject var8 = GsonHelper.getAsJsonObject(var0, "endpoints", null);
                    String var9 = getEndpointFromConfig(var8, "chat", "v1/chat");
                    boolean var10 = var9.equals("v1/chat");
                    URL var11 = var1.resolve("/" + var9).toURL();
                    URL var12 = getEndpoint(var1, var8, "join", "v1/join");
                    URL var13 = getEndpoint(var1, var8, "leave", "v1/leave");
                    TextFilterClient.JoinOrLeaveEncoder var14 = param2 -> {
                        JsonObject var0x = new JsonObject();
                        var0x.addProperty("server", var4);
                        var0x.addProperty("room", var5);
                        var0x.addProperty("user_id", param2.getId().toString());
                        var0x.addProperty("user_display_name", param2.getName());
                        return var0x;
                    };
                    TextFilterClient.MessageEncoder var15;
                    if (var10) {
                        var15 = (param3, param4) -> {
                            JsonObject var0x = new JsonObject();
                            var0x.addProperty("rule", var3);
                            var0x.addProperty("server", var4);
                            var0x.addProperty("room", var5);
                            var0x.addProperty("player", param3.getId().toString());
                            var0x.addProperty("player_display_name", param3.getName());
                            var0x.addProperty("text", param4);
                            return var0x;
                        };
                    } else {
                        String var16 = String.valueOf(var3);
                        var15 = (param3, param4) -> {
                            JsonObject var0x = new JsonObject();
                            var0x.addProperty("rule_id", var16);
                            var0x.addProperty("category", var4);
                            var0x.addProperty("subcategory", var5);
                            var0x.addProperty("user_id", param3.getId().toString());
                            var0x.addProperty("user_display_name", param3.getName());
                            var0x.addProperty("text", param4);
                            return var0x;
                        };
                    }

                    TextFilterClient.IgnoreStrategy var18 = TextFilterClient.IgnoreStrategy.select(var6);
                    String var19 = Base64.getEncoder().encodeToString(var2.getBytes(StandardCharsets.US_ASCII));
                    return new TextFilterClient(var11, var15, var12, var14, var13, var14, var19, var18, var7);
                }
            } catch (Exception var191) {
                LOGGER.warn("Failed to parse chat filter config {}", param0, var191);
                return null;
            }
        }
    }

    void processJoinOrLeave(GameProfile param0, URL param1, TextFilterClient.JoinOrLeaveEncoder param2, Executor param3) {
        param3.execute(() -> {
            JsonObject var0 = param2.encode(param0);

            try {
                this.processRequest(var0, param1);
            } catch (Exception var6) {
                LOGGER.warn("Failed to send join/leave packet to {} for player {}", param1, param0, var6);
            }

        });
    }

    CompletableFuture<FilteredText<String>> requestMessageProcessing(GameProfile param0, String param1, TextFilterClient.IgnoreStrategy param2, Executor param3) {
        return param1.isEmpty() ? CompletableFuture.completedFuture(FilteredText.EMPTY_STRING) : CompletableFuture.supplyAsync(() -> {
            JsonObject var0 = this.chatEncoder.encode(param0, param1);

            try {
                JsonObject var1 = this.processRequestResponse(var0, this.chatEndpoint);
                boolean var2x = GsonHelper.getAsBoolean(var1, "response", false);
                if (var2x) {
                    return FilteredText.passThrough(param1);
                } else {
                    String var3x = GsonHelper.getAsString(var1, "hashed", null);
                    if (var3x == null) {
                        return FilteredText.fullyFiltered(param1);
                    } else {
                        int var4x = GsonHelper.getAsJsonArray(var1, "hashes").size();
                        return param2.shouldIgnore(var3x, var4x) ? FilteredText.fullyFiltered(param1) : new FilteredText<>(param1, var3x);
                    }
                }
            } catch (Exception var9) {
                LOGGER.warn("Failed to validate message '{}'", param1, var9);
                return FilteredText.fullyFiltered(param1);
            }
        }, param3);
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

        JsonObject var5;
        try (InputStream var1 = var0.getInputStream()) {
            if (var0.getResponseCode() == 204) {
                return new JsonObject();
            }

            try {
                var5 = Streams.parse(new JsonReader(new InputStreamReader(var1, StandardCharsets.UTF_8))).getAsJsonObject();
            } finally {
                this.drainStream(var1);
            }
        }

        return var5;
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
        OutputStreamWriter var1 = new OutputStreamWriter(var0.getOutputStream(), StandardCharsets.UTF_8);

        try (JsonWriter var2 = new JsonWriter(var1)) {
            Streams.write(param0, var2);
        } catch (Throwable var11) {
            try {
                var1.close();
            } catch (Throwable var8) {
                var11.addSuppressed(var8);
            }

            throw var11;
        }

        var1.close();
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
            return switch(param0) {
                case -1 -> NEVER_IGNORE;
                case 0 -> IGNORE_FULLY_FILTERED;
                default -> ignoreOverThreshold(param0);
            };
        }

        boolean shouldIgnore(String var1, int var2);
    }

    @FunctionalInterface
    interface JoinOrLeaveEncoder {
        JsonObject encode(GameProfile var1);
    }

    @FunctionalInterface
    interface MessageEncoder {
        JsonObject encode(GameProfile var1, String var2);
    }

    class PlayerContext implements TextFilter {
        private final GameProfile profile;
        private final Executor streamExecutor;

        PlayerContext(GameProfile param0) {
            this.profile = param0;
            ProcessorMailbox<Runnable> param1 = ProcessorMailbox.create(TextFilterClient.this.workerPool, "chat stream for " + param0.getName());
            this.streamExecutor = param1::tell;
        }

        @Override
        public void join() {
            TextFilterClient.this.processJoinOrLeave(this.profile, TextFilterClient.this.joinEndpoint, TextFilterClient.this.joinEncoder, this.streamExecutor);
        }

        @Override
        public void leave() {
            TextFilterClient.this.processJoinOrLeave(this.profile, TextFilterClient.this.leaveEndpoint, TextFilterClient.this.leaveEncoder, this.streamExecutor);
        }

        @Override
        public CompletableFuture<List<FilteredText<String>>> processMessageBundle(List<String> param0) {
            List<CompletableFuture<FilteredText<String>>> var0 = param0.stream()
                .map(
                    param0x -> TextFilterClient.this.requestMessageProcessing(
                            this.profile, param0x, TextFilterClient.this.chatIgnoreStrategy, this.streamExecutor
                        )
                )
                .collect(ImmutableList.toImmutableList());
            return Util.sequenceFailFast(var0).exceptionally(param0x -> ImmutableList.of());
        }

        @Override
        public CompletableFuture<FilteredText<String>> processStreamMessage(String param0) {
            return TextFilterClient.this.requestMessageProcessing(this.profile, param0, TextFilterClient.this.chatIgnoreStrategy, this.streamExecutor);
        }
    }

    public static class RequestFailedException extends RuntimeException {
        RequestFailedException(String param0) {
            super(param0);
        }
    }
}
