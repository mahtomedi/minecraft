package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.LastSeenMessages;
import net.minecraft.network.protocol.Packet;

public record ServerboundChatAckPacket(LastSeenMessages.Update lastSeenMessages) implements Packet<ServerGamePacketListener> {
    public ServerboundChatAckPacket(FriendlyByteBuf param0) {
        this(new LastSeenMessages.Update(param0));
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        this.lastSeenMessages.write(param0);
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handleChatAck(this);
    }
}
