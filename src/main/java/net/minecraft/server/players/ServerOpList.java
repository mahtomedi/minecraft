package net.minecraft.server.players;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import java.io.File;

public class ServerOpList extends StoredUserList<GameProfile, ServerOpListEntry> {
    public ServerOpList(File param0) {
        super(param0);
    }

    @Override
    protected StoredUserEntry<GameProfile> createEntry(JsonObject param0) {
        return new ServerOpListEntry(param0);
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

    public boolean canBypassPlayerLimit(GameProfile param0) {
        ServerOpListEntry var0 = this.get(param0);
        return var0 != null ? var0.getBypassesPlayerLimit() : false;
    }

    protected String getKeyForUser(GameProfile param0) {
        return param0.getId().toString();
    }
}
