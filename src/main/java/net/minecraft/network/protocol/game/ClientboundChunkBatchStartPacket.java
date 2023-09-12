package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public record ClientboundChunkBatchStartPacket() implements Packet<ClientGamePacketListener> {
    public ClientboundChunkBatchStartPacket(FriendlyByteBuf param0) {
        this();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleChunkBatchStart(this);
    }
}
