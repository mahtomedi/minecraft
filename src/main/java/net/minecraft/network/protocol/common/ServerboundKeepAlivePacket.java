package net.minecraft.network.protocol.common;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundKeepAlivePacket implements Packet<ServerCommonPacketListener> {
    private final long id;

    public ServerboundKeepAlivePacket(long param0) {
        this.id = param0;
    }

    public void handle(ServerCommonPacketListener param0) {
        param0.handleKeepAlive(this);
    }

    public ServerboundKeepAlivePacket(FriendlyByteBuf param0) {
        this.id = param0.readLong();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeLong(this.id);
    }

    public long getId() {
        return this.id;
    }
}
