package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public record ClientboundChunkBatchFinishedPacket(int batchSize) implements Packet<ClientGamePacketListener> {
    public ClientboundChunkBatchFinishedPacket(FriendlyByteBuf param0) {
        this(param0.readVarInt());
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeVarInt(this.batchSize);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleChunkBatchFinished(this);
    }
}
