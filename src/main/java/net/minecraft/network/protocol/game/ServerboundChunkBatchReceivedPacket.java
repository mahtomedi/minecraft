package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public record ServerboundChunkBatchReceivedPacket(float desiredChunksPerTick) implements Packet<ServerGamePacketListener> {
    public ServerboundChunkBatchReceivedPacket(FriendlyByteBuf param0) {
        this(param0.readFloat());
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeFloat(this.desiredChunksPerTick);
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handleChunkBatchReceived(this);
    }
}
