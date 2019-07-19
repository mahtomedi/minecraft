package com.mojang.realmsclient.dto;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.realmsclient.util.JsonUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ServerActivityList extends ValueObject {
    public long periodInMillis;
    public List<ServerActivity> serverActivities = new ArrayList<>();

    public static ServerActivityList parse(String param0) {
        ServerActivityList var0 = new ServerActivityList();
        JsonParser var1 = new JsonParser();

        try {
            JsonElement var2 = var1.parse(param0);
            JsonObject var3 = var2.getAsJsonObject();
            var0.periodInMillis = JsonUtils.getLongOr("periodInMillis", var3, -1L);
            JsonElement var4 = var3.get("playerActivityDto");
            if (var4 != null && var4.isJsonArray()) {
                for(JsonElement var6 : var4.getAsJsonArray()) {
                    ServerActivity var7 = ServerActivity.parse(var6.getAsJsonObject());
                    var0.serverActivities.add(var7);
                }
            }
        } catch (Exception var10) {
        }

        return var0;
    }
}
