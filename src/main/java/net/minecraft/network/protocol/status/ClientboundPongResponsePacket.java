package net.minecraft.network.protocol.status;

import net.minecraft.network.ClientPongPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundPongResponsePacket implements Packet<ClientPongPacketListener> {
    private final long time;

    public ClientboundPongResponsePacket(long param0) {
        this.time = param0;
    }

    public ClientboundPongResponsePacket(FriendlyByteBuf param0) {
        this.time = param0.readLong();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeLong(this.time);
    }

    public void handle(ClientPongPacketListener param0) {
        param0.handlePongResponse(this);
    }

    public long getTime() {
        return this.time;
    }
}
