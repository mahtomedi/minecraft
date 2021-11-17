package com.mojang.realmsclient.dto;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.Iterator;
import java.util.List;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class PendingInvitesList extends ValueObject {
    private static final Logger LOGGER = LogManager.getLogger();
    public List<PendingInvite> pendingInvites = Lists.newArrayList();

    public static PendingInvitesList parse(String param0) {
        PendingInvitesList var0 = new PendingInvitesList();

        try {
            JsonParser var1 = new JsonParser();
            JsonObject var2 = var1.parse(param0).getAsJsonObject();
            if (var2.get("invites").isJsonArray()) {
                Iterator<JsonElement> var3 = var2.get("invites").getAsJsonArray().iterator();

                while(var3.hasNext()) {
                    var0.pendingInvites.add(PendingInvite.parse(var3.next().getAsJsonObject()));
                }
            }
        } catch (Exception var5) {
            LOGGER.error("Could not parse PendingInvitesList: {}", var5.getMessage());
        }

        return var0;
    }
}
