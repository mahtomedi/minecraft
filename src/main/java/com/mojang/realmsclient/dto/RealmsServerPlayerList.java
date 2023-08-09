package com.mojang.realmsclient.dto;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.util.JsonUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsServerPlayerList extends ValueObject {
    private static final Logger LOGGER = LogUtils.getLogger();
    public long serverId;
    public List<UUID> players;

    public static RealmsServerPlayerList parse(JsonObject param0) {
        RealmsServerPlayerList var0 = new RealmsServerPlayerList();

        try {
            var0.serverId = JsonUtils.getLongOr("serverId", param0, -1L);
            String var1 = JsonUtils.getStringOr("playerList", param0, null);
            if (var1 != null) {
                JsonElement var2 = JsonParser.parseString(var1);
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

    private static List<UUID> parsePlayers(JsonArray param0) {
        List<UUID> var0 = new ArrayList<>(param0.size());

        for(JsonElement var1 : param0) {
            if (var1.isJsonObject()) {
                UUID var2 = JsonUtils.getUuidOr("playerId", var1.getAsJsonObject(), null);
                if (var2 != null) {
                    var0.add(var2);
                }
            }
        }

        return var0;
    }
}
