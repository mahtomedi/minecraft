package net.minecraft.server.players;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import java.io.File;

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
        String[] var0 = new String[this.getEntries().size()];
        int var1 = 0;

        for(StoredUserEntry<GameProfile> var2 : this.getEntries()) {
            var0[var1++] = var2.getUser().getName();
        }

        return var0;
    }

    protected String getKeyForUser(GameProfile param0) {
        return param0.getId().toString();
    }
}
