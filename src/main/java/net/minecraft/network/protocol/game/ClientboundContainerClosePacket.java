package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundContainerClosePacket implements Packet<ClientGamePacketListener> {
    private int containerId;

    public ClientboundContainerClosePacket() {
    }

    public ClientboundContainerClosePacket(int param0) {
        this.containerId = param0;
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleContainerClose(this);
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.containerId = param0.readUnsignedByte();
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeByte(this.containerId);
    }
}
