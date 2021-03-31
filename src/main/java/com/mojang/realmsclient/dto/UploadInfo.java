package com.mojang.realmsclient.dto;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.realmsclient.util.JsonUtils;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class UploadInfo extends ValueObject {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String DEFAULT_SCHEMA = "http://";
    private static final int DEFAULT_PORT = 8080;
    private static final Pattern URI_SCHEMA_PATTERN = Pattern.compile("^[a-zA-Z][-a-zA-Z0-9+.]+:");
    private final boolean worldClosed;
    @Nullable
    private final String token;
    private final URI uploadEndpoint;

    private UploadInfo(boolean param0, @Nullable String param1, URI param2) {
        this.worldClosed = param0;
        this.token = param1;
        this.uploadEndpoint = param2;
    }

    @Nullable
    public static UploadInfo parse(String param0) {
        try {
            JsonParser var0 = new JsonParser();
            JsonObject var1 = var0.parse(param0).getAsJsonObject();
            String var2 = JsonUtils.getStringOr("uploadEndpoint", var1, null);
            if (var2 != null) {
                int var3 = JsonUtils.getIntOr("port", var1, -1);
                URI var4 = assembleUri(var2, var3);
                if (var4 != null) {
                    boolean var5 = JsonUtils.getBooleanOr("worldClosed", var1, false);
                    String var6 = JsonUtils.getStringOr("token", var1, null);
                    return new UploadInfo(var5, var6, var4);
                }
            }
        } catch (Exception var8) {
            LOGGER.error("Could not parse UploadInfo: {}", var8.getMessage());
        }

        return null;
    }

    @Nullable
    @VisibleForTesting
    public static URI assembleUri(String param0, int param1) {
        Matcher var0 = URI_SCHEMA_PATTERN.matcher(param0);
        String var1 = ensureEndpointSchema(param0, var0);

        try {
            URI var2 = new URI(var1);
            int var3 = selectPortOrDefault(param1, var2.getPort());
            return var3 != var2.getPort()
                ? new URI(var2.getScheme(), var2.getUserInfo(), var2.getHost(), var3, var2.getPath(), var2.getQuery(), var2.getFragment())
                : var2;
        } catch (URISyntaxException var6) {
            LOGGER.warn("Failed to parse URI {}", var1, var6);
            return null;
        }
    }

    private static int selectPortOrDefault(int param0, int param1) {
        if (param0 != -1) {
            return param0;
        } else {
            return param1 != -1 ? param1 : 8080;
        }
    }

    private static String ensureEndpointSchema(String param0, Matcher param1) {
        return param1.find() ? param0 : "http://" + param0;
    }

    public static String createRequest(@Nullable String param0) {
        JsonObject var0 = new JsonObject();
        if (param0 != null) {
            var0.addProperty("token", param0);
        }

        return var0.toString();
    }

    @Nullable
    public String getToken() {
        return this.token;
    }

    public URI getUploadEndpoint() {
        return this.uploadEndpoint;
    }

    public boolean isWorldClosed() {
        return this.worldClosed;
    }
}
