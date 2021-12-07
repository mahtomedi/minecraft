package com.mojang.realmsclient.client;

import com.google.common.base.Strings;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.realmsclient.util.JsonUtils;
import javax.annotation.Nullable;
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

    @Nullable
    public static RealmsError parse(String param0) {
        if (Strings.isNullOrEmpty(param0)) {
            return null;
        } else {
            try {
                JsonObject var0 = JsonParser.parseString(param0).getAsJsonObject();
                String var1 = JsonUtils.getStringOr("errorMsg", var0, "");
                int var2 = JsonUtils.getIntOr("errorCode", var0, -1);
                return new RealmsError(var1, var2);
            } catch (Exception var4) {
                LOGGER.error("Could not parse RealmsError: {}", var4.getMessage());
                LOGGER.error("The error was: {}", param0);
                return null;
            }
        }
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public int getErrorCode() {
        return this.errorCode;
    }
}
