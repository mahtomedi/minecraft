package com.mojang.realmsclient.dto;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsServerList extends ValueObject {
    private static final Logger LOGGER = LogManager.getLogger();
    public List<RealmsServer> servers;

    public static RealmsServerList parse(String param0) {
        RealmsServerList var0 = new RealmsServerList();
        var0.servers = new ArrayList<>();

        try {
            JsonParser var1 = new JsonParser();
            JsonObject var2 = var1.parse(param0).getAsJsonObject();
            if (var2.get("servers").isJsonArray()) {
                JsonArray var3 = var2.get("servers").getAsJsonArray();
                Iterator<JsonElement> var4 = var3.iterator();

                while(var4.hasNext()) {
                    var0.servers.add(RealmsServer.parse(var4.next().getAsJsonObject()));
                }
            }
        } catch (Exception var6) {
            LOGGER.error("Could not parse McoServerList: " + var6.getMessage());
        }

        return var0;
    }
}
