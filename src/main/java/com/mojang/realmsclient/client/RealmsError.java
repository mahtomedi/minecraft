package com.mojang.realmsclient.client;

import com.google.common.base.Strings;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.exception.RealmsHttpException;
import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public interface RealmsError {
    Component NO_MESSAGE = Component.translatable("mco.errorMessage.noDetails");
    Logger LOGGER = LogUtils.getLogger();

    int errorCode();

    Component errorMessage();

    String logMessage();

    static RealmsError parse(int param0, String param1) {
        if (param0 == 429) {
            return RealmsError.CustomError.SERVICE_BUSY;
        } else if (Strings.isNullOrEmpty(param1)) {
            return RealmsError.CustomError.noPayload(param0);
        } else {
            try {
                JsonObject var0 = JsonParser.parseString(param1).getAsJsonObject();
                String var1 = GsonHelper.getAsString(var0, "reason", null);
                String var2 = GsonHelper.getAsString(var0, "errorMsg", null);
                int var3 = GsonHelper.getAsInt(var0, "errorCode", -1);
                if (var2 != null || var1 != null || var3 != -1) {
                    return new RealmsError.ErrorWithJsonPayload(param0, var3 != -1 ? var3 : param0, var1, var2);
                }
            } catch (Exception var6) {
                LOGGER.error("Could not parse RealmsError", (Throwable)var6);
            }

            return new RealmsError.ErrorWithRawPayload(param0, param1);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static record AuthenticationError(String message) implements RealmsError {
        public static final int ERROR_CODE = 401;

        @Override
        public int errorCode() {
            return 401;
        }

        @Override
        public Component errorMessage() {
            return Component.literal(this.message);
        }

        @Override
        public String logMessage() {
            return String.format(Locale.ROOT, "Realms authentication error with message '%s'", this.message);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static record CustomError(int httpCode, @Nullable Component payload) implements RealmsError {
        public static final RealmsError.CustomError SERVICE_BUSY = new RealmsError.CustomError(429, Component.translatable("mco.errorMessage.serviceBusy"));
        public static final Component RETRY_MESSAGE = Component.translatable("mco.errorMessage.retry");

        public static RealmsError.CustomError unknownCompatibilityResponse(String param0) {
            return new RealmsError.CustomError(500, Component.translatable("mco.errorMessage.realmsService.unknownCompatibility", param0));
        }

        public static RealmsError.CustomError connectivityError(RealmsHttpException param0) {
            return new RealmsError.CustomError(500, Component.translatable("mco.errorMessage.realmsService.connectivity", param0.getMessage()));
        }

        public static RealmsError.CustomError retry(int param0) {
            return new RealmsError.CustomError(param0, RETRY_MESSAGE);
        }

        public static RealmsError.CustomError noPayload(int param0) {
            return new RealmsError.CustomError(param0, null);
        }

        @Override
        public int errorCode() {
            return this.httpCode;
        }

        @Override
        public Component errorMessage() {
            return this.payload != null ? this.payload : NO_MESSAGE;
        }

        @Override
        public String logMessage() {
            return this.payload != null
                ? String.format(Locale.ROOT, "Realms service error (%d) with message '%s'", this.httpCode, this.payload.getString())
                : String.format(Locale.ROOT, "Realms service error (%d) with no payload", this.httpCode);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static record ErrorWithJsonPayload(int httpCode, int code, @Nullable String reason, @Nullable String message) implements RealmsError {
        @Override
        public int errorCode() {
            return this.code;
        }

        @Override
        public Component errorMessage() {
            String var0 = "mco.errorMessage." + this.code;
            if (I18n.exists(var0)) {
                return Component.translatable(var0);
            } else {
                if (this.reason != null) {
                    String var1 = "mco.errorReason." + this.reason;
                    if (I18n.exists(var1)) {
                        return Component.translatable(var1);
                    }
                }

                return (Component)(this.message != null ? Component.literal(this.message) : NO_MESSAGE);
            }
        }

        @Override
        public String logMessage() {
            return String.format(Locale.ROOT, "Realms service error (%d/%d/%s) with message '%s'", this.httpCode, this.code, this.reason, this.message);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static record ErrorWithRawPayload(int httpCode, String payload) implements RealmsError {
        @Override
        public int errorCode() {
            return this.httpCode;
        }

        @Override
        public Component errorMessage() {
            return Component.literal(this.payload);
        }

        @Override
        public String logMessage() {
            return String.format(Locale.ROOT, "Realms service error (%d) with raw payload '%s'", this.httpCode, this.payload);
        }
    }
}
