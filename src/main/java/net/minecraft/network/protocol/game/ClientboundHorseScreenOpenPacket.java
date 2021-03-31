package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundHorseScreenOpenPacket implements Packet<ClientGamePacketListener> {
    private final int containerId;
    private final int size;
    private final int entityId;

    public ClientboundHorseScreenOpenPacket(int param0, int param1, int param2) {
        this.containerId = param0;
        this.size = param1;
        this.entityId = param2;
    }

    public ClientboundHorseScreenOpenPacket(FriendlyByteBuf param0) {
        this.containerId = param0.readUnsignedByte();
        this.size = param0.readVarInt();
        this.entityId = param0.readInt();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeByte(this.containerId);
        param0.writeVarInt(this.size);
        param0.writeInt(this.entityId);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleHorseScreenOpen(this);
    }

    public int getContainerId() {
        return this.containerId;
    }

    public int getSize() {
        return this.size;
    }

    public int getEntityId() {
        return this.entityId;
    }
}
