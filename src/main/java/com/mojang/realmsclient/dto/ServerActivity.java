package com.mojang.realmsclient.dto;

import com.google.gson.JsonObject;
import com.mojang.realmsclient.util.JsonUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ServerActivity extends ValueObject {
    public String profileUuid;
    public long joinTime;
    public long leaveTime;

    public static ServerActivity parse(JsonObject param0) {
        ServerActivity var0 = new ServerActivity();

        try {
            var0.profileUuid = JsonUtils.getStringOr("profileUuid", param0, null);
            var0.joinTime = JsonUtils.getLongOr("joinTime", param0, Long.MIN_VALUE);
            var0.leaveTime = JsonUtils.getLongOr("leaveTime", param0, Long.MIN_VALUE);
        } catch (Exception var3) {
        }

        return var0;
    }
}
