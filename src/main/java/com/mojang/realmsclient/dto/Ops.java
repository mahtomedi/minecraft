package com.mojang.realmsclient.dto;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.HashSet;
import java.util.Set;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Ops extends ValueObject {
    public Set<String> ops = new HashSet<>();

    public static Ops parse(String param0) {
        Ops var0 = new Ops();
        JsonParser var1 = new JsonParser();

        try {
            JsonElement var2 = var1.parse(param0);
            JsonObject var3 = var2.getAsJsonObject();
            JsonElement var4 = var3.get("ops");
            if (var4.isJsonArray()) {
                for(JsonElement var5 : var4.getAsJsonArray()) {
                    var0.ops.add(var5.getAsString());
                }
            }
        } catch (Exception var8) {
        }

        return var0;
    }
}
