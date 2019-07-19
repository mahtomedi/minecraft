package com.mojang.realmsclient.dto;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.realmsclient.util.JsonUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsServerAddress extends ValueObject {
    private static final Logger LOGGER = LogManager.getLogger();
    public String address;
    public String resourcePackUrl;
    public String resourcePackHash;

    public static RealmsServerAddress parse(String param0) {
        JsonParser var0 = new JsonParser();
        RealmsServerAddress var1 = new RealmsServerAddress();

        try {
            JsonObject var2 = var0.parse(param0).getAsJsonObject();
            var1.address = JsonUtils.getStringOr("address", var2, null);
            var1.resourcePackUrl = JsonUtils.getStringOr("resourcePackUrl", var2, null);
            var1.resourcePackHash = JsonUtils.getStringOr("resourcePackHash", var2, null);
        } catch (Exception var4) {
            LOGGER.error("Could not parse RealmsServerAddress: " + var4.getMessage());
        }

        return var1;
    }
}
