package net.minecraft.server.players;

import com.google.gson.JsonObject;
import java.util.Date;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

public class IpBanListEntry extends BanListEntry<String> {
    public IpBanListEntry(String param0) {
        this(param0, null, null, null, null);
    }

    public IpBanListEntry(String param0, @Nullable Date param1, @Nullable String param2, @Nullable Date param3, @Nullable String param4) {
        super(param0, param1, param2, param3, param4);
    }

    @Override
    public Component getDisplayName() {
        return new TextComponent(this.getUser());
    }

    public IpBanListEntry(JsonObject param0) {
        super(createIpInfo(param0), param0);
    }

    private static String createIpInfo(JsonObject param0) {
        return param0.has("ip") ? param0.get("ip").getAsString() : null;
    }

    @Override
    protected void serialize(JsonObject param0) {
        if (this.getUser() != null) {
            param0.addProperty("ip", this.getUser());
            super.serialize(param0);
        }
    }
}
