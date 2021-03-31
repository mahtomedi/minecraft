package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundKeepAlivePacket implements Packet<ServerGamePacketListener> {
    private final long id;

    public ServerboundKeepAlivePacket(long param0) {
        this.id = param0;
    }

    public void handle(ServerGamePacketListener param0) {
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
