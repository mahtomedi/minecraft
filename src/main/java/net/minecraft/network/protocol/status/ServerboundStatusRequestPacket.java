package net.minecraft.network.protocol.status;

import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundStatusRequestPacket implements Packet<ServerStatusPacketListener> {
    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
    }

    public void handle(ServerStatusPacketListener param0) {
        param0.handleStatusRequest(this);
    }
}
