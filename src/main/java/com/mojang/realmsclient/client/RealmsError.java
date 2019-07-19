package com.mojang.realmsclient.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.realmsclient.util.JsonUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsError {
    private static final Logger LOGGER = LogManager.getLogger();
    private String errorMessage;
    private int errorCode;

    public RealmsError(String param0) {
        try {
            JsonParser var0 = new JsonParser();
            JsonObject var1 = var0.parse(param0).getAsJsonObject();
            this.errorMessage = JsonUtils.getStringOr("errorMsg", var1, "");
            this.errorCode = JsonUtils.getIntOr("errorCode", var1, -1);
        } catch (Exception var4) {
            LOGGER.error("Could not parse RealmsError: " + var4.getMessage());
            LOGGER.error("The error was: " + param0);
        }

    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public int getErrorCode() {
        return this.errorCode;
    }
}
