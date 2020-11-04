package com.mojang.realmsclient.dto;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.realmsclient.util.JsonUtils;
import java.util.List;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsServerPlayerList extends ValueObject {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final JsonParser JSON_PARSER = new JsonParser();
    public long serverId;
    public List<String> players;

    public static RealmsServerPlayerList parse(JsonObject param0) {
        RealmsServerPlayerList var0 = new RealmsServerPlayerList();

        try {
            var0.serverId = JsonUtils.getLongOr("serverId", param0, -1L);
            String var1 = JsonUtils.getStringOr("playerList", param0, null);
            if (var1 != null) {
                JsonElement var2 = JSON_PARSER.parse(var1);
                if (var2.isJsonArray()) {
                    var0.players = parsePlayers(var2.getAsJsonArray());
                } else {
                    var0.players = Lists.newArrayList();
                }
            } else {
                var0.players = Lists.newArrayList();
            }
        } catch (Exception var4) {
            LOGGER.error("Could not parse RealmsServerPlayerList: {}", var4.getMessage());
        }

        return var0;
    }

    private static List<String> parsePlayers(JsonArray param0) {
        List<String> var0 = Lists.newArrayList();

        for(JsonElement var1 : param0) {
            try {
                var0.add(var1.getAsString());
            } catch (Exception var5) {
            }
        }

        return var0;
    }
}
