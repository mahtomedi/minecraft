package net.minecraft.server.players;

import com.google.gson.JsonObject;
import java.io.File;
import java.net.SocketAddress;
import javax.annotation.Nullable;

public class IpBanList extends StoredUserList<String, IpBanListEntry> {
    public IpBanList(File param0) {
        super(param0);
    }

    @Override
    protected StoredUserEntry<String> createEntry(JsonObject param0) {
        return new IpBanListEntry(param0);
    }

    public boolean isBanned(SocketAddress param0) {
        String var0 = this.getIpFromAddress(param0);
        return this.contains(var0);
    }

    public boolean isBanned(String param0) {
        return this.contains(param0);
    }

    @Nullable
    public IpBanListEntry get(SocketAddress param0) {
        String var0 = this.getIpFromAddress(param0);
        return this.get(var0);
    }

    private String getIpFromAddress(SocketAddress param0) {
        String var0 = param0.toString();
        if (var0.contains("/")) {
            var0 = var0.substring(var0.indexOf(47) + 1);
        }

        if (var0.contains(":")) {
            var0 = var0.substring(0, var0.indexOf(58));
        }

        return var0;
    }
}
