package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.ChunkPos;

public record ClientboundForgetLevelChunkPacket(ChunkPos pos) implements Packet<ClientGamePacketListener> {
    public ClientboundForgetLevelChunkPacket(FriendlyByteBuf param0) {
        this(param0.readChunkPos());
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeChunkPos(this.pos);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleForgetLevelChunk(this);
    }
}
