package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundContainerClosePacket implements Packet<ClientGamePacketListener> {
    private final int containerId;

    public ClientboundContainerClosePacket(int param0) {
        this.containerId = param0;
    }

    public ClientboundContainerClosePacket(FriendlyByteBuf param0) {
        this.containerId = param0.readUnsignedByte();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeByte(this.containerId);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleContainerClose(this);
    }

    public int getContainerId() {
        return this.containerId;
    }
}
