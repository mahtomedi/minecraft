package net.minecraft.network.protocol.login;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;

public class ClientboundLoginDisconnectPacket implements Packet<ClientLoginPacketListener> {
    private final Component reason;

    public ClientboundLoginDisconnectPacket(Component param0) {
        this.reason = param0;
    }

    public ClientboundLoginDisconnectPacket(FriendlyByteBuf param0) {
        this.reason = Component.Serializer.fromJsonLenient(param0.readUtf(262144));
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeComponent(this.reason);
    }

    public void handle(ClientLoginPacketListener param0) {
        param0.handleDisconnect(this);
    }

    public Component getReason() {
        return this.reason;
    }
}
