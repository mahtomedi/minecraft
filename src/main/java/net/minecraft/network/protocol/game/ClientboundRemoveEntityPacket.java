package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundRemoveEntityPacket implements Packet<ClientGamePacketListener> {
    private final int entityId;

    public ClientboundRemoveEntityPacket(int param0) {
        this.entityId = param0;
    }

    public ClientboundRemoveEntityPacket(FriendlyByteBuf param0) {
        this.entityId = param0.readVarInt();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeVarInt(this.entityId);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleRemoveEntity(this);
    }

    public int getEntityId() {
        return this.entityId;
    }
}
