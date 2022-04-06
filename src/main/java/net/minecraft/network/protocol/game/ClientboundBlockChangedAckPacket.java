package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public record ClientboundBlockChangedAckPacket(int sequence) implements Packet<ClientGamePacketListener> {
    public ClientboundBlockChangedAckPacket(FriendlyByteBuf param0) {
        this(param0.readVarInt());
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeVarInt(this.sequence);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleBlockChangedAck(this);
    }
}
