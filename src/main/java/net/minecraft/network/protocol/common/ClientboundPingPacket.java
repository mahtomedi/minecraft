package net.minecraft.network.protocol.common;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundPingPacket implements Packet<ClientCommonPacketListener> {
    private final int id;

    public ClientboundPingPacket(int param0) {
        this.id = param0;
    }

    public ClientboundPingPacket(FriendlyByteBuf param0) {
        this.id = param0.readInt();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeInt(this.id);
    }

    public void handle(ClientCommonPacketListener param0) {
        param0.handlePing(this);
    }

    public int getId() {
        return this.id;
    }
}
