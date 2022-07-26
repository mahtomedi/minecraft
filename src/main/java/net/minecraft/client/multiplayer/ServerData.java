package net.minecraft.client.multiplayer;

import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ServerData {
    public String name;
    public String ip;
    public Component status;
    public Component motd;
    public long ping;
    public int protocol = SharedConstants.getCurrentVersion().getProtocolVersion();
    public Component version = Component.literal(SharedConstants.getCurrentVersion().getName());
    public boolean pinged;
    public List<Component> playerList = Collections.emptyList();
    private ServerData.ServerPackStatus packStatus = ServerData.ServerPackStatus.PROMPT;
    @Nullable
    private String iconB64;
    private boolean lan;
    private boolean enforcesSecureChat;

    public ServerData(String param0, String param1, boolean param2) {
        this.name = param0;
        this.ip = param1;
        this.lan = param2;
    }

    public CompoundTag write() {
        CompoundTag var0 = new CompoundTag();
        var0.putString("name", this.name);
        var0.putString("ip", this.ip);
        if (this.iconB64 != null) {
            var0.putString("icon", this.iconB64);
        }

        if (this.packStatus == ServerData.ServerPackStatus.ENABLED) {
            var0.putBoolean("acceptTextures", true);
        } else if (this.packStatus == ServerData.ServerPackStatus.DISABLED) {
            var0.putBoolean("acceptTextures", false);
        }

        return var0;
    }

    public ServerData.ServerPackStatus getResourcePackStatus() {
        return this.packStatus;
    }

    public void setResourcePackStatus(ServerData.ServerPackStatus param0) {
        this.packStatus = param0;
    }

    public static ServerData read(CompoundTag param0) {
        ServerData var0 = new ServerData(param0.getString("name"), param0.getString("ip"), false);
        if (param0.contains("icon", 8)) {
            var0.setIconB64(param0.getString("icon"));
        }

        if (param0.contains("acceptTextures", 1)) {
            if (param0.getBoolean("acceptTextures")) {
                var0.setResourcePackStatus(ServerData.ServerPackStatus.ENABLED);
            } else {
                var0.setResourcePackStatus(ServerData.ServerPackStatus.DISABLED);
            }
        } else {
            var0.setResourcePackStatus(ServerData.ServerPackStatus.PROMPT);
        }

        return var0;
    }

    @Nullable
    public String getIconB64() {
        return this.iconB64;
    }

    public static String parseFavicon(String param0) throws ParseException {
        if (param0.startsWith("data:image/png;base64,")) {
            return param0.substring("data:image/png;base64,".length());
        } else {
            throw new ParseException("Unknown format", 0);
        }
    }

    public void setIconB64(@Nullable String param0) {
        this.iconB64 = param0;
    }

    public boolean isLan() {
        return this.lan;
    }

    public void setEnforcesSecureChat(boolean param0) {
        this.enforcesSecureChat = param0;
    }

    public boolean enforcesSecureChat() {
        return this.enforcesSecureChat;
    }

    public void copyNameIconFrom(ServerData param0) {
        this.ip = param0.ip;
        this.name = param0.name;
        this.iconB64 = param0.iconB64;
    }

    public void copyFrom(ServerData param0) {
        this.copyNameIconFrom(param0);
        this.setResourcePackStatus(param0.getResourcePackStatus());
        this.lan = param0.lan;
        this.enforcesSecureChat = param0.enforcesSecureChat;
    }

    @OnlyIn(Dist.CLIENT)
    public static enum ServerPackStatus {
        ENABLED("enabled"),
        DISABLED("disabled"),
        PROMPT("prompt");

        private final Component name;

        private ServerPackStatus(String param0) {
            this.name = Component.translatable("addServer.resourcePack." + param0);
        }

        public Component getName() {
            return this.name;
        }
    }
}
