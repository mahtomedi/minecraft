package com.mojang.realmsclient.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Date;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class JsonUtils {
    public static String getStringOr(String param0, JsonObject param1, String param2) {
        JsonElement var0 = param1.get(param0);
        if (var0 != null) {
            return var0.isJsonNull() ? param2 : var0.getAsString();
        } else {
            return param2;
        }
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
