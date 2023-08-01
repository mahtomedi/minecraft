package com.mojang.realmsclient.dto;

import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.util.JsonUtils;
import java.util.Date;
import java.util.UUID;
import net.minecraft.Util;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class PendingInvite extends ValueObject {
    private static final Logger LOGGER = LogUtils.getLogger();
    public String invitationId;
    public String worldName;
    public String worldOwnerName;
    public UUID worldOwnerUuid;
    public Date date;

    public static PendingInvite parse(JsonObject param0) {
        PendingInvite var0 = new PendingInvite();

        try {
            var0.invitationId = JsonUtils.getStringOr("invitationId", param0, "");
            var0.worldName = JsonUtils.getStringOr("worldName", param0, "");
            var0.worldOwnerName = JsonUtils.getStringOr("worldOwnerName", param0, "");
            var0.worldOwnerUuid = JsonUtils.getUuidOr("worldOwnerUuid", param0, Util.NIL_UUID);
            var0.date = JsonUtils.getDateOr("date", param0);
        } catch (Exception var3) {
            LOGGER.error("Could not parse PendingInvite: {}", var3.getMessage());
        }

        return var0;
    }
}
