package com.mojang.realmsclient.dto;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.realmsclient.util.JsonUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class UploadInfo extends ValueObject {
    private static final Logger LOGGER = LogManager.getLogger();
    private boolean worldClosed;
    private String token = "";
    private String uploadEndpoint = "";
    private int port;

    public static UploadInfo parse(String param0) {
        UploadInfo var0 = new UploadInfo();

        try {
            JsonParser var1 = new JsonParser();
            JsonObject var2 = var1.parse(param0).getAsJsonObject();
            var0.worldClosed = JsonUtils.getBooleanOr("worldClosed", var2, false);
            var0.token = JsonUtils.getStringOr("token", var2, null);
            var0.uploadEndpoint = JsonUtils.getStringOr("uploadEndpoint", var2, null);
            var0.port = JsonUtils.getIntOr("port", var2, 8080);
        } catch (Exception var4) {
            LOGGER.error("Could not parse UploadInfo: " + var4.getMessage());
        }

        return var0;
    }

    public String getToken() {
        return this.token;
    }

    public String getUploadEndpoint() {
        return this.uploadEndpoint;
    }

    public boolean isWorldClosed() {
        return this.worldClosed;
    }

    public void setToken(String param0) {
        this.token = param0;
    }

    public int getPort() {
        return this.port;
    }
}
