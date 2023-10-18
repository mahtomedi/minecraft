package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public record ServerboundContainerSlotStateChangedPacket(int slotId, int containerId, boolean newState) implements Packet<ServerGamePacketListener> {
    public ServerboundContainerSlotStateChangedPacket(FriendlyByteBuf param0) {
        this(param0.readVarInt(), param0.readVarInt(), param0.readBoolean());
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeVarInt(this.slotId);
        param0.writeVarInt(this.containerId);
        param0.writeBoolean(this.newState);
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handleContainerSlotStateChanged(this);
    }
}
