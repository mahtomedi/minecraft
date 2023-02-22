package com.mojang.realmsclient.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class JsonUtils {
    public static <T> T getRequired(String param0, JsonObject param1, Function<JsonObject, T> param2) {
        JsonElement var0 = param1.get(param0);
        if (var0 == null || var0.isJsonNull()) {
            throw new IllegalStateException("Missing required property: " + param0);
        } else if (!var0.isJsonObject()) {
            throw new IllegalStateException("Required property " + param0 + " was not a JsonObject as espected");
        } else {
            return param2.apply(var0.getAsJsonObject());
        }
    }

    public static String getRequiredString(String param0, JsonObject param1) {
        String var0 = getStringOr(param0, param1, null);
        if (var0 == null) {
            throw new IllegalStateException("Missing required property: " + param0);
        } else {
            return var0;
        }
    }

    @Nullable
    public static String getStringOr(String param0, JsonObject param1, @Nullable String param2) {
        JsonElement var0 = param1.get(param0);
        if (var0 != null) {
            return var0.isJsonNull() ? param2 : var0.getAsString();
        } else {
            return param2;
        }
    }

    @Nullable
    public static UUID getUuidOr(String param0, JsonObject param1, @Nullable UUID param2) {
        String var0 = getStringOr(param0, param1, null);
        return var0 == null ? param2 : UUID.fromString(var0);
    }

    public static int getIntOr(String param0, JsonObject param1, int param2) {
        JsonElement var0 = param1.get(param0);
        if (var0 != null) {
            return var0.isJsonNull() ? param2 : var0.getAsInt();
        } else {
            return param2;
        }
    }

    public static long getLongOr(String param0, JsonObject param1, long param2) {
        JsonElement var0 = param1.get(param0);
        if (var0 != null) {
            return var0.isJsonNull() ? param2 : var0.getAsLong();
        } else {
            return param2;
        }
    }

    public static boolean getBooleanOr(String param0, JsonObject param1, boolean param2) {
        JsonElement var0 = param1.get(param0);
        if (var0 != null) {
            return var0.isJsonNull() ? param2 : var0.getAsBoolean();
        } else {
            return param2;
        }
    }

    public static Date getDateOr(String param0, JsonObject param1) {
        JsonElement var0 = param1.get(param0);
        return var0 != null ? new Date(Long.parseLong(var0.getAsString())) : new Date();
    }
}
