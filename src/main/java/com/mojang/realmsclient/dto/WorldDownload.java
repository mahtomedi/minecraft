package com.mojang.realmsclient.dto;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.util.JsonUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class WorldDownload extends ValueObject {
    private static final Logger LOGGER = LogUtils.getLogger();
    public String downloadLink;
    public String resourcePackUrl;
    public String resourcePackHash;

    public static WorldDownload parse(String param0) {
        JsonParser var0 = new JsonParser();
        JsonObject var1 = var0.parse(param0).getAsJsonObject();
        WorldDownload var2 = new WorldDownload();

        try {
            var2.downloadLink = JsonUtils.getStringOr("downloadLink", var1, "");
            var2.resourcePackUrl = JsonUtils.getStringOr("resourcePackUrl", var1, "");
            var2.resourcePackHash = JsonUtils.getStringOr("resourcePackHash", var1, "");
        } catch (Exception var5) {
            LOGGER.error("Could not parse WorldDownload: {}", var5.getMessage());
        }

        return var2;
    }
}
