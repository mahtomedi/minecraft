package net.minecraft.server.players;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import java.io.File;
import java.util.Objects;

public class UserBanList extends StoredUserList<GameProfile, UserBanListEntry> {
    public UserBanList(File param0) {
        super(param0);
    }

    @Override
    protected StoredUserEntry<GameProfile> createEntry(JsonObject param0) {
        return new UserBanListEntry(param0);
    }

    public boolean isBanned(GameProfile param0) {
        return this.contains(param0);
    }

    @Override
    public String[] getUserList() {
        return this.getEntries()
            .stream()
            .map(StoredUserEntry::getUser)
            .filter(Objects::nonNull)
            .map(GameProfile::getName)
            .toArray(param0 -> new String[param0]);
    }

    protected String getKeyForUser(GameProfile param0) {
        return param0.getId().toString();
    }
}
