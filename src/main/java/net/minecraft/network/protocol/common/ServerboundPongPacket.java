package net.minecraft.network.protocol.common;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundPongPacket implements Packet<ServerCommonPacketListener> {
    private final int id;

    public ServerboundPongPacket(int param0) {
        this.id = param0;
    }

    public ServerboundPongPacket(FriendlyByteBuf param0) {
        this.id = param0.readInt();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeInt(this.id);
    }

    public void handle(ServerCommonPacketListener param0) {
        param0.handlePong(this);
    }

    public int getId() {
        return this.id;
    }
}
