package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public record ServerboundChatAckPacket(int offset) implements Packet<ServerGamePacketListener> {
    public ServerboundChatAckPacket(FriendlyByteBuf param0) {
        this(param0.readVarInt());
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeVarInt(this.offset);
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handleChatAck(this);
    }
}
