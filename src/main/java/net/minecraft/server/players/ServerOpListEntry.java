package net.minecraft.server.players;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import javax.annotation.Nullable;

public class ServerOpListEntry extends StoredUserEntry<GameProfile> {
    private final int level;
    private final boolean bypassesPlayerLimit;

    public ServerOpListEntry(GameProfile param0, int param1, boolean param2) {
        super(param0);
        this.level = param1;
        this.bypassesPlayerLimit = param2;
    }

    public ServerOpListEntry(JsonObject param0) {
        super(createGameProfile(param0));
        this.level = param0.has("level") ? param0.get("level").getAsInt() : 0;
        this.bypassesPlayerLimit = param0.has("bypassesPlayerLimit") && param0.get("bypassesPlayerLimit").getAsBoolean();
    }

    public int getLevel() {
        return this.level;
    }

    public boolean getBypassesPlayerLimit() {
        return this.bypassesPlayerLimit;
    }

    @Override
    protected void serialize(JsonObject param0) {
        if (this.getUser() != null) {
            param0.addProperty("uuid", this.getUser().getId() == null ? "" : this.getUser().getId().toString());
            param0.addProperty("name", this.getUser().getName());
            param0.addProperty("level", this.level);
            param0.addProperty("bypassesPlayerLimit", this.bypassesPlayerLimit);
        }
    }

    @Nullable
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
