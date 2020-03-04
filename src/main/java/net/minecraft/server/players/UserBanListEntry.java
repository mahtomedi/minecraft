package net.minecraft.server.players;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

public class UserBanListEntry extends BanListEntry<GameProfile> {
    public UserBanListEntry(GameProfile param0) {
        this(param0, null, null, null, null);
    }

    public UserBanListEntry(GameProfile param0, @Nullable Date param1, @Nullable String param2, @Nullable Date param3, @Nullable String param4) {
        super(param0, param1, param2, param3, param4);
    }

    public UserBanListEntry(JsonObject param0) {
        super(createGameProfile(param0), param0);
    }

    @Override
    protected void serialize(JsonObject param0) {
        if (this.getUser() != null) {
            param0.addProperty("uuid", this.getUser().getId() == null ? "" : this.getUser().getId().toString());
            param0.addProperty("name", this.getUser().getName());
            super.serialize(param0);
        }
    }

    @Override
    public Component getDisplayName() {
        GameProfile var0 = this.getUser();
        return new TextComponent(var0.getName() != null ? var0.getName() : Objects.toString(var0.getId(), "(Unknown)"));
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
