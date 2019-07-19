package net.minecraft.network.protocol.status;

import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundPongResponsePacket implements Packet<ClientStatusPacketListener> {
    private long time;

    public ClientboundPongResponsePacket() {
    }

    public ClientboundPongResponsePacket(long param0) {
        this.time = param0;
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.time = param0.readLong();
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeLong(this.time);
    }

    public void handle(ClientStatusPacketListener param0) {
        param0.handlePongResponse(this);
    }
}
