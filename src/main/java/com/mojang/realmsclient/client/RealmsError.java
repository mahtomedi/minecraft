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
    private final String errorMessage;
    private final int errorCode;

    private RealmsError(String param0, int param1) {
        this.errorMessage = param0;
        this.errorCode = param1;
    }

    public static RealmsError create(String param0) {
        try {
            JsonParser var0 = new JsonParser();
            JsonObject var1 = var0.parse(param0).getAsJsonObject();
            String var2 = JsonUtils.getStringOr("errorMsg", var1, "");
            int var3 = JsonUtils.getIntOr("errorCode", var1, -1);
            return new RealmsError(var2, var3);
        } catch (Exception var5) {
            LOGGER.error("Could not parse RealmsError: " + var5.getMessage());
            LOGGER.error("The error was: " + param0);
            return new RealmsError("Failed to parse response from server", -1);
        }
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public int getErrorCode() {
        return this.errorCode;
    }
}
