package net.minecraft.client.multiplayer;

import com.mojang.logging.LogUtils;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ServerData {
    private static final Logger LOGGER = LogUtils.getLogger();
    public String name;
    public String ip;
    public Component status;
    public Component motd;
    @Nullable
    public ServerStatus.Players players;
    public long ping;
    public int protocol = SharedConstants.getCurrentVersion().getProtocolVersion();
    public Component version = Component.literal(SharedConstants.getCurrentVersion().getName());
    public boolean pinged;
    public List<Component> playerList = Collections.emptyList();
    private ServerData.ServerPackStatus packStatus = ServerData.ServerPackStatus.PROMPT;
    @Nullable
    private byte[] iconBytes;
    private ServerData.Type type;
    private boolean enforcesSecureChat;

    public ServerData(String param0, String param1, ServerData.Type param2) {
        this.name = param0;
        this.ip = param1;
        this.type = param2;
    }

    public CompoundTag write() {
        CompoundTag var0 = new CompoundTag();
        var0.putString("name", this.name);
        var0.putString("ip", this.ip);
        if (this.iconBytes != null) {
            var0.putString("icon", Base64.getEncoder().encodeToString(this.iconBytes));
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
        ServerData var0 = new ServerData(param0.getString("name"), param0.getString("ip"), ServerData.Type.OTHER);
        if (param0.contains("icon", 8)) {
            try {
                var0.setIconBytes(Base64.getDecoder().decode(param0.getString("icon")));
            } catch (IllegalArgumentException var3) {
                LOGGER.warn("Malformed base64 server icon", (Throwable)var3);
            }
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
    public byte[] getIconBytes() {
        return this.iconBytes;
    }

    public void setIconBytes(@Nullable byte[] param0) {
        this.iconBytes = param0;
    }

    public boolean isLan() {
        return this.type == ServerData.Type.LAN;
    }

    public boolean isRealm() {
        return this.type == ServerData.Type.REALM;
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
        this.iconBytes = param0.iconBytes;
    }

    public void copyFrom(ServerData param0) {
        this.copyNameIconFrom(param0);
        this.setResourcePackStatus(param0.getResourcePackStatus());
        this.type = param0.type;
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

    @OnlyIn(Dist.CLIENT)
    public static enum Type {
        LAN,
        REALM,
        OTHER;
    }
}
