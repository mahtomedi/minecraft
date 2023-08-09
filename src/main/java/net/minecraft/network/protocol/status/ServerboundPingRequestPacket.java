package net.minecraft.network.protocol.status;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerPingPacketListener;

public class ServerboundPingRequestPacket implements Packet<ServerPingPacketListener> {
    private final long time;

    public ServerboundPingRequestPacket(long param0) {
        this.time = param0;
    }

    public ServerboundPingRequestPacket(FriendlyByteBuf param0) {
        this.time = param0.readLong();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeLong(this.time);
    }

    public void handle(ServerPingPacketListener param0) {
        param0.handlePingRequest(this);
    }

    public long getTime() {
        return this.time;
    }
}
