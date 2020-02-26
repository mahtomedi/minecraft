package net.minecraft.server.players;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import java.util.UUID;

public class UserWhiteListEntry extends StoredUserEntry<GameProfile> {
    public UserWhiteListEntry(GameProfile param0) {
        super(param0);
    }

    public UserWhiteListEntry(JsonObject param0) {
        super(createGameProfile(param0));
    }

    private static GameProfile createGameProfile(JsonObject param0) {
        if (param0.has("uuid") && param0.has("name")) {
            String var0 = param0.get("uuid").getAsString();

            UUID var1;
            try {
                var1 = UUID.fromString(var0);
            } catch (Throwable var4) {
                return null;
            }

            return new GameProfile(var1, param0.get("name").getAsString());
        } else {
            return null;
        }
    }
}
