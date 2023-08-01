package net.minecraft.network.protocol.common;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundKeepAlivePacket implements Packet<ClientCommonPacketListener> {
    private final long id;

    public ClientboundKeepAlivePacket(long param0) {
        this.id = param0;
    }

    public ClientboundKeepAlivePacket(FriendlyByteBuf param0) {
        this.id = param0.readLong();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeLong(this.id);
    }

    public void handle(ClientCommonPacketListener param0) {
        param0.handleKeepAlive(this);
    }

    public long getId() {
        return this.id;
    }
}
