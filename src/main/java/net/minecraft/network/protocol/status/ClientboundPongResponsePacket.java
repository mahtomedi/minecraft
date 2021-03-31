package net.minecraft.network.protocol.status;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundPongResponsePacket implements Packet<ClientStatusPacketListener> {
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

    public void handle(ClientStatusPacketListener param0) {
        param0.handlePongResponse(this);
    }

    public long getTime() {
        return this.time;
    }
}
