package net.minecraft.network.protocol.common;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;

public class ClientboundDisconnectPacket implements Packet<ClientCommonPacketListener> {
    private final Component reason;

    public ClientboundDisconnectPacket(Component param0) {
        this.reason = param0;
    }

    public ClientboundDisconnectPacket(FriendlyByteBuf param0) {
        this.reason = param0.readComponentTrusted();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeComponent(this.reason);
    }

    public void handle(ClientCommonPacketListener param0) {
        param0.handleDisconnect(this);
    }

    public Component getReason() {
        return this.reason;
    }
}
