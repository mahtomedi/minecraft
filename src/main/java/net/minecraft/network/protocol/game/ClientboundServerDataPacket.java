package net.minecraft.network.protocol.game;

import java.util.Optional;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;

public class ClientboundServerDataPacket implements Packet<ClientGamePacketListener> {
    private final Component motd;
    private final Optional<byte[]> iconBytes;
    private final boolean enforcesSecureChat;

    public ClientboundServerDataPacket(Component param0, Optional<byte[]> param1, boolean param2) {
        this.motd = param0;
        this.iconBytes = param1;
        this.enforcesSecureChat = param2;
    }

    public ClientboundServerDataPacket(FriendlyByteBuf param0) {
        this.motd = param0.readComponentTrusted();
        this.iconBytes = param0.readOptional(FriendlyByteBuf::readByteArray);
        this.enforcesSecureChat = param0.readBoolean();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeComponent(this.motd);
        param0.writeOptional(this.iconBytes, FriendlyByteBuf::writeByteArray);
        param0.writeBoolean(this.enforcesSecureChat);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleServerData(this);
    }

    public Component getMotd() {
        return this.motd;
    }

    public Optional<byte[]> getIconBytes() {
        return this.iconBytes;
    }

    public boolean enforcesSecureChat() {
        return this.enforcesSecureChat;
    }
}
