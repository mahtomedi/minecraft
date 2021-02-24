package net.minecraft.network.protocol.status;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundStatusRequestPacket implements Packet<ServerStatusPacketListener> {
    public ServerboundStatusRequestPacket() {
    }

    public ServerboundStatusRequestPacket(FriendlyByteBuf param0) {
    }

    @Override
    public void write(FriendlyByteBuf param0) {
    }

    public void handle(ServerStatusPacketListener param0) {
        param0.handleStatusRequest(this);
    }
}
